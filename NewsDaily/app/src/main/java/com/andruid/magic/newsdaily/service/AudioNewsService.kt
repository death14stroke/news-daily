package com.andruid.magic.newsdaily.service

import android.app.PendingIntent
import android.content.*
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import androidx.preference.PreferenceManager
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.Constants
import com.andruid.magic.newsdaily.model.AudioNews
import com.andruid.magic.newsdaily.model.buildMetaData
import com.andruid.magic.newsdaily.model.getMediaDescription
import com.andruid.magic.newsdaily.util.NotificationUtil.buildNotification
import com.andruid.magic.newsdaily.util.PrefUtil.getDefaultCountry
import com.andruid.magic.newsloader.api.NewsRepository
import com.andruid.magic.newsloader.data.Constants.FIRST_PAGE
import com.andruid.magic.newsloader.data.Constants.PAGE_SIZE
import com.andruid.magic.texttoaudiofile.api.TtsApi
import com.andruid.magic.texttoaudiofile.util.FileUtils.getUtteranceId
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

class AudioNewsService : MediaBrowserServiceCompat(), CoroutineScope, Player.EventListener,
    TtsApi.AudioConversionListener {
    companion object {
        private val TAG = AudioNewsService::class.java.simpleName

        private const val MEDIA_SERVICE = "AudioNewsService"
        private const val MSG_INIT_NEWS = 0
        private const val MSG_STOP_SERVICE = 1
        private const val MSG_UPDATE_SOURCE = 2
        private const val MSG_SHOW_NOTI = 3
        private const val WAIT_QUEUE_TIMEOUT_MS = 5000

        private const val NEWS_FETCH_DISTANCE = 3
        private const val MEDIA_NOTI_ID = 1
    }

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var category: String

    private var page = FIRST_PAGE

    private val mediaSessionCompat by lazy {
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON).setClass(
            this,
            MediaButtonReceiver::class.java
        )
        val pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0)
        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)
        MediaSessionCompat(applicationContext, MEDIA_SERVICE, mediaButtonReceiver, pendingIntent)
    }
    private val exoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(
            this,
            DefaultTrackSelector()
        )
    }
    private val dataSourceFactory by lazy {
        DefaultDataSourceFactory(
            applicationContext,
            Util.getUserAgent(this, getString(R.string.app_name))
        )
    }
    private val mediaSessionConnector by lazy { MediaSessionConnector(mediaSessionCompat) }

    private val audioNewsHandler = Handler(AudioNewsHandler())
    private val concatenatingMediaSource = ConcatenatingMediaSource()
    private var mediaSessionCallback = MediaSessionCallback()
    private val audioNewsList = mutableListOf<AudioNews>()

    private val mNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mediaSessionCallback.onPause()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "service created")
        job.start()
        initExoPlayer()
        initMediaSession()
    }

    private fun initExoPlayer() {
        exoPlayer.apply {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build()
            setAudioAttributes(audioAttributes, true)
            addListener(this@AudioNewsService)
        }
    }

    private fun initMediaSession() {
        mediaSessionCompat.apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }
        sessionToken = mediaSessionCompat.sessionToken
        mediaSessionConnector.apply {
            setQueueNavigator(object : TimelineQueueNavigator(mediaSessionCompat) {
                override fun getMediaDescription(
                    player: Player,
                    windowIndex: Int
                ): MediaDescriptionCompat {
                    return audioNewsList[windowIndex].getMediaDescription()
                }
            })
            setPlayer(exoPlayer, null)
        }
    }

    private fun loadNews() {
        val country = PreferenceManager.getDefaultSharedPreferences(this).getString(
            getString(R.string.pref_country), getDefaultCountry(this)
        )
        launch {
            val resp = withContext(Dispatchers.IO) {
                NewsRepository.getInstance().loadHeadlines(country!!, category, page, PAGE_SIZE)
            }
            val newsList = resp.body()?.newsList
            newsList?.let {
                val pos = audioNewsList.size
                for (i in newsList.indices) {
                    val news = newsList[i]
                    var text = news.desc
                    audioNewsList.add(AudioNews(news.url, news))
                    if (text?.isEmpty() != false)
                        text = "No description available"
                    TtsApi.getInstance().convertToAudioFile(
                        text, (pos + i).toString(),
                        this@AudioNewsService
                    )
                }
            }
        }
    }

    private fun setCurrentAudio(pos: Int) {
        if (pos >= audioNewsList.size || pos < 0)
            return
        mediaSessionCompat.setMetadata(audioNewsList[pos].buildMetaData())
    }

    private fun setMediaPlaybackState(state: Int, pos: Int) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
        playbackStateBuilder.setActiveQueueItemId(pos.toLong())
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS ||
            state == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT
        )
            playbackStateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_STOP
            )
        else
            playbackStateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_STOP
            )
        if (state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_PLAYING) {
            try {
                playbackStateBuilder.setState(state, exoPlayer.currentPosition, 0f)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        } else
            playbackStateBuilder.setState(
                state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                0f
            )
        mediaSessionCompat.setPlaybackState(playbackStateBuilder.build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Constants.ACTION_PREPARE_AUDIO == intent.action) {
            category = intent.extras?.getString(Constants.EXTRA_CATEGORY) ?: "general"
            if (TtsApi.getInstance().isReady())
                audioNewsHandler.sendEmptyMessage(MSG_INIT_NEWS)
            else
                Toast.makeText(
                    applicationContext, "Text to speech not available",
                    Toast.LENGTH_SHORT
                ).show()
            registerReceiver(mNoisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        } else
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent)
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        audioNewsHandler.sendEmptyMessage(MSG_STOP_SERVICE)
    }

    override fun onDestroy() {
        job.cancel()
        mediaSessionCompat.release()
        mediaSessionConnector.setPlayer(null, null)
        try {
            unregisterReceiver(mNoisyReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        exoPlayer.release()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            var icon = android.R.drawable.ic_media_play
            if (playWhenReady) icon = android.R.drawable.ic_media_pause
            if (mediaSessionCompat.controller.metadata == null)
                return
            Log.d(TAG, "sending message noti show in state change")
            val message = audioNewsHandler.obtainMessage(
                MSG_SHOW_NOTI, icon,
                if (playWhenReady) 1 else 0,
                category
            )
            audioNewsHandler.sendMessage(message)
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
            reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION ||
            reason == Player.DISCONTINUITY_REASON_INTERNAL
        ) {
            val pos = exoPlayer.currentWindowIndex
            if (pos == audioNewsList.size - NEWS_FETCH_DISTANCE) {
                page++
                loadNews()
            }
            val audioNews = audioNewsList[pos]
            val mediaMetadataCompat: MediaMetadataCompat = audioNews.buildMetaData()
            mediaSessionCompat.setMetadata(mediaMetadataCompat)
            Log.d(TAG, "sending message noti show in pos discontinuity")
            val message = audioNewsHandler.obtainMessage(
                MSG_SHOW_NOTI, android.R.drawable.ic_media_pause,
                1, category
            )
            audioNewsHandler.sendMessage(message)
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(parentMediaId: String, result: Result<List<MediaItem?>?>) {
        result.sendResult(ArrayList())
    }

    override fun onAudioCreated(file: File) {
        val utteranceId = getUtteranceId(file.name)
        Log.d(TAG, "Utterance done $utteranceId")
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
            Log.d(TAG, "handle message: ${message.what}")
            when (message.what) {
                MSG_INIT_NEWS -> {
                    audioNewsHandler.removeCallbacksAndMessages(null)
                    audioNewsList.clear()
                    concatenatingMediaSource.clear()
                    page = FIRST_PAGE
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
                    launch {
                        val notificationBuilder = buildNotification(
                            this@AudioNewsService,
                            icon, category, metadataCompat, mediaSessionCompat.sessionToken
                        )
                        if (playWhenReady)
                            startForeground(MEDIA_NOTI_ID, notificationBuilder.build())
                        else {
                            NotificationManagerCompat.from(this@AudioNewsService).notify(MEDIA_NOTI_ID,
                                notificationBuilder.build())
                            stopForeground(false)
                        }
                    }
                }
            }
            return false
        }
    }
}