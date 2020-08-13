package com.andruid.magic.newsdaily.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.getSystemService
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.ACTION_PREPARE_AUDIO
import com.andruid.magic.newsdaily.data.EXTRA_CATEGORY
import com.andruid.magic.newsdaily.data.PAGE_SIZE
import com.andruid.magic.newsdaily.data.model.AudioNews
import com.andruid.magic.newsdaily.data.model.buildMetaData
import com.andruid.magic.newsdaily.data.model.getMediaDescription
import com.andruid.magic.newsdaily.data.model.toAudioNews
import com.andruid.magic.newsdaily.database.repository.DbRepository
import com.andruid.magic.newsdaily.util.buildNotification
import com.andruid.magic.newsdaily.util.getDefaultCountry
import com.andruid.magic.newsdaily.util.toast
import com.andruid.magic.texttoaudiofile.api.TtsApi
import com.andruid.magic.texttoaudiofile.data.model.TtsResult
import com.andruid.magic.texttoaudiofile.util.FileUtils.getUtteranceId
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class AudioNewsService : MediaBrowserServiceCompat(), CoroutineScope, Player.EventListener {
    companion object {
        private val TAG = "${AudioNewsService::class.java.simpleName}Log"

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

    private val mediaSessionCompat by lazy {
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON).setClass(
            this,
            MediaButtonReceiver::class.java
        )
        val pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0)
        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)
        MediaSessionCompat(
            applicationContext,
            MEDIA_SERVICE, mediaButtonReceiver, pendingIntent
        )
    }
    private val exoPlayer by lazy {
        SimpleExoPlayer.Builder(this)
            .build()
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
    private val mediaSessionCallback = MediaSessionCallback()
    private val audioNewsList = mutableListOf<AudioNews>()

    private var page = 0

    private lateinit var category: String

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "service created")

        job.start()
        initExoPlayer()
        initMediaSession()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ACTION_PREPARE_AUDIO == intent.action) {
            category = intent.extras?.getString(EXTRA_CATEGORY) ?: "general"
            if (TtsApi.isReady)
                audioNewsHandler.sendEmptyMessage(MSG_INIT_NEWS)
            else
                toast("Text to speech not available")
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
        mediaSessionConnector.setPlayer(null)
        exoPlayer.release()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            if (mediaSessionCompat.controller.metadata == null)
                return
            val icon =
                if (playWhenReady) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

            val message = audioNewsHandler.obtainMessage(
                MSG_SHOW_NOTI, icon,
                if (playWhenReady) 1 else 0,
                category
            )
            audioNewsHandler.sendMessage(message).also {
                Log.d(TAG, "sending message noti show in state change")
            }
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
            reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION ||
            reason == Player.DISCONTINUITY_REASON_INTERNAL
        ) {
            val pos = exoPlayer.currentWindowIndex
            if (pos == audioNewsList.size - NEWS_FETCH_DISTANCE) {
                launch {
                    Log.d("audioLog", "reached news fetch distance pos = $pos")
                    loadNews()
                }
            }
            val audioNews = audioNewsList[pos]
            val mediaMetadataCompat = audioNews.buildMetaData()
            mediaSessionCompat.setMetadata(mediaMetadataCompat)

            val message = audioNewsHandler.obtainMessage(
                MSG_SHOW_NOTI,
                android.R.drawable.ic_media_pause,
                1,
                category
            )
            audioNewsHandler.sendMessage(message).also {
                Log.d(TAG, "sending message noti show in pos discontinuity")
            }
        }
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?) =
        BrowserRoot("root", null)

    override fun onLoadChildren(parentMediaId: String, result: Result<List<MediaItem?>?>) {
        result.sendResult(ArrayList())
    }

    private suspend fun loadNews() {
        Log.d("audioLog", "load news called page =  $page, page size = $PAGE_SIZE")
        withContext(Dispatchers.IO) {
            DbRepository.getNewsForPage(
                getDefaultCountry(),
                category,
                page++,
                PAGE_SIZE
            )
        }
            .map { news -> news.toAudioNews() }
            .forEach { news ->
                val pos = audioNewsList.size
                Log.d("audioLog", "news = ${news.news.title}")
                val text =
                    if (news.news.desc?.isNotEmpty() == true) news.news.desc else "No description available"

                audioNewsList.add(news)
                addAudioToQueue(text, pos.toString())
            }
    }

    private suspend fun addAudioToQueue(text: String, id: String) {
        val result = TtsApi.convertToAudioFile(text, id)

        if (result is TtsResult.Success) {
            val file = result.file
            val utteranceId = getUtteranceId(file.name).also {
                Log.d(TAG, "Utterance done $it")
            }

            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(file))
            concatenatingMediaSource.addMediaSource(mediaSource)

            if (utteranceId == "0") {
                audioNewsHandler.sendEmptyMessage(MSG_UPDATE_SOURCE)
                mediaSessionCallback.onPlay()
            } else {
                audioNewsHandler.sendEmptyMessageDelayed(
                    MSG_UPDATE_SOURCE,
                    WAIT_QUEUE_TIMEOUT_MS.toLong()
                )
            }
        }
    }

    private fun initExoPlayer() {
        exoPlayer.apply {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build()
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            setWakeMode(C.WAKE_MODE_LOCAL)
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
                override fun getMediaDescription(player: Player, windowIndex: Int) =
                    audioNewsList[windowIndex].getMediaDescription()
            })
            setPlayer(exoPlayer)
        }
    }

    private fun setMediaPlaybackState(state: Int, pos: Int) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setActiveQueueItemId(pos.toLong())

        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS || state == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT
        ) {
            playbackStateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_STOP
            )
        } else {
            playbackStateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_STOP
            )
        }

        if (state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_PLAYING) {
            try {
                playbackStateBuilder.setState(state, exoPlayer.currentPosition, 0f)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                toast(e.message ?: "IllegalStateException")
            }
        } else
            playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)

        mediaSessionCompat.setPlaybackState(playbackStateBuilder.build())
    }

    private fun setCurrentAudio(pos: Int) {
        if (pos >= audioNewsList.size || pos < 0)
            return
        mediaSessionCompat.setMetadata(audioNewsList[pos].buildMetaData())
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            exoPlayer.playWhenReady = true
            mediaSessionCompat.isActive = true

            val pos = exoPlayer.currentWindowIndex
            setCurrentAudio(pos)
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

                    launch {
                        loadNews()
                    }
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
                    val metadataCompat = mediaSessionCompat.controller.metadata
                    launch {
                        val notificationBuilder = buildNotification(
                            icon,
                            category,
                            metadataCompat,
                            mediaSessionCompat.sessionToken
                        )
                        if (playWhenReady)
                            startForeground(MEDIA_NOTI_ID, notificationBuilder.build())
                        else {
                            getSystemService<NotificationManager>()?.notify(
                                MEDIA_NOTI_ID,
                                notificationBuilder.build()
                            )
                            stopForeground(false)
                        }
                    }
                }
            }

            return false
        }
    }
}