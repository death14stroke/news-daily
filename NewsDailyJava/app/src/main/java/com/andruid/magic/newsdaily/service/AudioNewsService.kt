package com.andruid.magic.newsdaily.service

import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import androidx.preference.PreferenceManager
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.AppConstants
import com.andruid.magic.newsdaily.model.AudioNews
import com.andruid.magic.newsdaily.model.buildMetaData
import com.andruid.magic.newsdaily.model.getMediaDescription
import com.andruid.magic.newsdaily.util.NotificationUtil.Companion.buildNotification
import com.andruid.magic.newsdaily.util.PrefUtil.Companion.getDefaultCountry
import com.andruid.magic.newsloader.api.NewsRepository
import com.andruid.magic.newsloader.api.NewsRepository.NewsLoadedListener
import com.andruid.magic.newsloader.data.Constants
import com.andruid.magic.newsloader.model.News
import com.andruid.magic.texttoaudiofile.api.TtsApi
import com.andruid.magic.texttoaudiofile.api.TtsApi.AudioConversionListener
import com.andruid.magic.texttoaudiofile.util.FileUtils.Companion.getUtteranceId
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber
import java.io.File
import java.util.*

class AudioNewsService : MediaBrowserServiceCompat(), Player.EventListener, NewsLoadedListener,
        AudioConversionListener {
    companion object {
        private const val MEDIA_SERVICE = "AudioNewsService"
        private const val MSG_INIT_NEWS = 0
        private const val MSG_STOP_SERVICE = 1
        private const val MSG_UPDATE_SOURCE = 2
        private const val MSG_SHOW_NOTI = 3
        private const val WAIT_QUEUE_TIMEOUT_MS = 5000

        const val NEWS_FETCH_DISTANCE = 3
        const val MEDIA_NOTI_ID = 1
    }

    private lateinit var mediaSessionCompat: MediaSessionCompat
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var category: String
    private lateinit var audioNewsHandlerThread: HandlerThread
    private lateinit var audioNewsHandler: Handler

    private val concatenatingMediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()
    private var mediaSessionCallback: MediaSessionCallback = MediaSessionCallback()
    private val audioNewsList = mutableListOf<AudioNews>()
    private var page = Constants.FIRST_PAGE
    private val mNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mediaSessionCallback.onPause()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("service created")
        audioNewsHandlerThread = HandlerThread("HandlerThread")
        audioNewsHandlerThread.start()
        audioNewsHandler = Handler(audioNewsHandlerThread.looper, AudioNewsHandler())
        initExoPlayer()
        initMediaSession()
    }

    private fun initExoPlayer() {
        dataSourceFactory = DefaultDataSourceFactory(applicationContext,
                Util.getUserAgent(this, getString(R.string.app_name)))
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector()).apply {
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build()
            setAudioAttributes(audioAttributes, true)
            addListener(this@AudioNewsService)
        }
    }

    private fun initMediaSession() {
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
                .setClass(this, MediaButtonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0)
        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)
        mediaSessionCompat = MediaSessionCompat(applicationContext, MEDIA_SERVICE, mediaButtonReceiver,
                pendingIntent).apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }
        sessionToken = mediaSessionCompat.sessionToken
        mediaSessionConnector = MediaSessionConnector(mediaSessionCompat).apply {
            setQueueNavigator(object : TimelineQueueNavigator(mediaSessionCompat) {
                override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
                    return audioNewsList[windowIndex].getMediaDescription()
                }
            })
            setPlayer(exoPlayer, null)
        }
    }

    private fun loadNews() {
        val country = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.pref_country), getDefaultCountry(this))
        NewsRepository.getInstance().loadHeadlines(country!!, category, page, Constants.PAGE_SIZE, this)
    }

    private fun setCurrentAudio(pos: Int) {
        if (pos >= audioNewsList.size || pos < 0) return
        mediaSessionCompat.setMetadata(audioNewsList[pos].buildMetaData())
    }

    private fun setMediaPlaybackState(state: Int, pos: Int) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
        playbackStateBuilder.setActiveQueueItemId(pos.toLong())
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS ||
                state == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_STOP)
        else
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_STOP)
        if (state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_PLAYING) {
            try {
                playbackStateBuilder.setState(state, exoPlayer.currentPosition, 0f)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        else
            playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    0f)
        mediaSessionCompat.setPlaybackState(playbackStateBuilder.build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (AppConstants.ACTION_PREPARE_AUDIO == intent.action) {
            intent.extras?.apply {
                category = getString(AppConstants.EXTRA_CATEGORY) ?: "general"
            }
            if (TtsApi.getInstance().isReady())
                audioNewsHandler.sendEmptyMessage(MSG_INIT_NEWS)
            else
                Toast.makeText(applicationContext, "Text to speech not available",
                        Toast.LENGTH_SHORT).show()
            registerReceiver(mNoisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        }
        else
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent)
        return Service.START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        audioNewsHandler.sendEmptyMessage(MSG_STOP_SERVICE)
    }

    override fun onDestroy() {
        mediaSessionCompat.release()
        mediaSessionConnector.setPlayer(null, null)
        try {
            unregisterReceiver(mNoisyReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        exoPlayer.release()
        audioNewsHandlerThread.quit()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            var icon = android.R.drawable.ic_media_play
            if (playWhenReady) icon = android.R.drawable.ic_media_pause
            if(mediaSessionCompat.controller.metadata == null)
                return
            Timber.d("sending message noti show in state change")
            val message = audioNewsHandler.obtainMessage(MSG_SHOW_NOTI, icon,
                    if (playWhenReady) 1 else 0,
                    category)
            audioNewsHandler.sendMessage(message)
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
                reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION ||
                reason == Player.DISCONTINUITY_REASON_INTERNAL) {
            val pos = exoPlayer.currentWindowIndex
            if (pos == audioNewsList.size - NEWS_FETCH_DISTANCE) {
                page++
                loadNews()
            }
            val audioNews = audioNewsList[pos]
            val mediaMetadataCompat: MediaMetadataCompat = audioNews.buildMetaData()
            mediaSessionCompat.setMetadata(mediaMetadataCompat)
            Timber.d("sending message noti show in pos discontinuity")
            val message = audioNewsHandler.obtainMessage(MSG_SHOW_NOTI, android.R.drawable.ic_media_pause,
                    1, category)
            audioNewsHandler.sendMessage(message)
        }
    }

    override fun onSuccess(newsList: List<News>, hasMore: Boolean) {
        val pos = audioNewsList.size
        for (i in newsList.indices) {
            val news = newsList[i]
            var text = news.desc
            audioNewsList.add(AudioNews(news.url, news))
            if (text?.isEmpty() != false)
                text = "No description available"
            TtsApi.getInstance().convertToAudioFile(text, (pos + i).toString(), this)
        }
    }

    override fun onFailure(t: Throwable?) {
        t?.printStackTrace()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(parentMediaId: String,
                                result: Result<List<MediaBrowserCompat.MediaItem?>?>) {
        result.sendResult(ArrayList())
    }

    override fun onAudioCreated(file: File) {
        val utteranceId = getUtteranceId(file.name)
        Timber.d("Utterance done %s", utteranceId)
        val mediaSource: MediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(file))
        concatenatingMediaSource.addMediaSource(mediaSource)
        audioNewsHandler.sendEmptyMessageDelayed(MSG_UPDATE_SOURCE, WAIT_QUEUE_TIMEOUT_MS.toLong())
        if (utteranceId == "0") {
            audioNewsHandler.sendEmptyMessage(MSG_UPDATE_SOURCE)
            mediaSessionCallback.onPlay()
        }
    }

    override fun onFailure(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            exoPlayer.playWhenReady = true
            val pos: Int = exoPlayer.currentWindowIndex
            setCurrentAudio(pos)
            mediaSessionCompat.isActive = true
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING, pos)
        }

        override fun onPause() {
            super.onPause()
            exoPlayer.playWhenReady = false
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED, exoPlayer.currentWindowIndex)
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            if (exoPlayer.hasNext())
                exoPlayer.next()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            if (exoPlayer.hasPrevious())
                exoPlayer.previous()
        }
    }

    private inner class AudioNewsHandler : Handler.Callback {
        override fun handleMessage(message: Message): Boolean {
            Timber.d("handle message: %d", message.what)
            when (message.what) {
                MSG_INIT_NEWS -> {
                    audioNewsHandler.removeCallbacksAndMessages(null)
                    audioNewsList.clear()
                    concatenatingMediaSource.clear()
                    page = Constants.FIRST_PAGE
                    loadNews()
                }
                MSG_STOP_SERVICE -> {
                    audioNewsHandler.removeCallbacksAndMessages(null)
                    stopForeground(true)
                    stopSelf()
                }
                MSG_UPDATE_SOURCE -> {
                    audioNewsHandler.removeMessages(MSG_UPDATE_SOURCE)
                    exoPlayer.prepare(concatenatingMediaSource, false, false)
                }
                MSG_SHOW_NOTI -> {
                    val icon = message.arg1
                    val category = message.obj as String
                    val playWhenReady = message.arg2 == 1
                    val metadataCompat: MediaMetadataCompat = mediaSessionCompat.controller.metadata
                    val notificationBuilder = buildNotification(this@AudioNewsService,
                            icon, category, metadataCompat, mediaSessionCompat.sessionToken)
                    if (playWhenReady) startForeground(MEDIA_NOTI_ID, notificationBuilder.build()) else {
                        NotificationManagerCompat.from(this@AudioNewsService).notify(MEDIA_NOTI_ID,
                                notificationBuilder.build())
                        stopForeground(false)
                    }
                }
            }
            return false
        }
    }
}