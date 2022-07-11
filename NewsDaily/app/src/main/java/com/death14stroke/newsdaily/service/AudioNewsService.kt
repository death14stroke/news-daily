package com.death14stroke.newsdaily.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.ACTION_PREPARE_AUDIO
import com.death14stroke.newsdaily.data.EXTRA_CATEGORY
import com.death14stroke.newsdaily.data.PAGE_SIZE
import com.death14stroke.newsdaily.data.model.*
import com.death14stroke.newsdaily.data.preferences.PreferenceHelper
import com.death14stroke.newsdaily.data.repository.MainRepository
import com.death14stroke.newsdaily.ui.util.buildNotification
import com.death14stroke.newsdaily.ui.util.sendRequest
import com.death14stroke.newsdaily.ui.util.toast
import com.death14stroke.newsloader.data.model.Category
import com.death14stroke.newsloader.data.model.News
import com.death14stroke.texttoaudiofile.api.TtsHelper
import com.death14stroke.texttoaudiofile.util.FileUtils.getUtteranceId
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class AudioNewsService : MediaBrowserServiceCompat(), CoroutineScope, Player.Listener {
    companion object {
        private const val MEDIA_SERVICE = "AudioNewsService"
        private const val WAIT_QUEUE_TIMEOUT_MS = 5000
        private const val NEWS_FETCH_DISTANCE = 3
        private const val MEDIA_NOTI_ID = 1
    }

    private val repository by inject<MainRepository>()
    private val preferenceHelper by inject<PreferenceHelper>()
    private val ttsHelper by inject<TtsHelper>()

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val mediaSessionCompat by lazy {
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON).setClass(
            this,
            MediaButtonReceiver::class.java
        )
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE)
        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)

        MediaSessionCompat(applicationContext, MEDIA_SERVICE, mediaButtonReceiver, pendingIntent)
    }
    private val exoPlayer by lazy {
        ExoPlayer.Builder(this)
            .setLooper(Looper.getMainLooper())
            .build()
    }
    private val dataSourceFactory by lazy {
        DefaultDataSource.Factory(applicationContext) {
            DefaultDataSource(
                applicationContext,
                Util.getUserAgent(this@AudioNewsService, getString(R.string.app_name)),
                false
            )
        }
    }
    private val mediaSessionConnector by lazy { MediaSessionConnector(mediaSessionCompat) }
    private val audioNewsHandler by lazy { Handler(Looper.getMainLooper(), AudioNewsHandler()) }
    private val concatenatingMediaSource = ConcatenatingMediaSource()
    private val mediaSessionCallback = MediaSessionCallback()

    private var page = 0

    private lateinit var category: Category

    override fun onCreate() {
        super.onCreate()
        Timber.i("onCreate AudioNewsService")
        job.start()
        initExoPlayer()
        initMediaSession()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ACTION_PREPARE_AUDIO == intent.action) {
            category = intent.extras?.getParcelable(EXTRA_CATEGORY) ?: Category.GENERAL
            ttsHelper.initialize { success ->
                Timber.d("service tts initialize = $success")
                if (success)
                    audioNewsHandler.sendEmptyMessage(HandlerMessage.MSG_INIT_NEWS.code)
                else
                    toast("Text to speech not initialized")
            }
        } else {
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent)
        }

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        audioNewsHandler.sendEmptyMessage(HandlerMessage.MSG_STOP_SERVICE.code)
    }

    override fun onDestroy() {
        job.cancel()
        mediaSessionCompat.release()
        mediaSessionConnector.setPlayer(null)
        exoPlayer.release()
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if (mediaSessionCompat.controller.metadata == null)
            return
        val icon =
            if (playWhenReady) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        val message = audioNewsHandler.obtainMessage(
            HandlerMessage.MSG_SHOW_NOTI.code, icon,
            if (playWhenReady) 1 else 0,
            category
        )
        audioNewsHandler.sendMessage(message)
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        Timber.d("MediaItem transition")
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
        if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
            reason == Player.DISCONTINUITY_REASON_SEEK ||
            reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION ||
            reason == Player.DISCONTINUITY_REASON_INTERNAL
        ) {
            val pos = exoPlayer.currentMediaItemIndex
            if (pos == exoPlayer.mediaItemCount - NEWS_FETCH_DISTANCE) {
                launch {
                    loadNews()
                }
            }
            val mediaMetadataCompat = exoPlayer.currentMediaItem?.toAudioNews()?.buildMetaData()
            mediaSessionCompat.setMetadata(mediaMetadataCompat)

            val message = audioNewsHandler.obtainMessage(
                HandlerMessage.MSG_SHOW_NOTI.code,
                android.R.drawable.ic_media_pause,
                1,
                category
            )

            audioNewsHandler.sendMessage(message)
        }
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?) =
        BrowserRoot("root", null)

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(mutableListOf())
    }

    private suspend fun loadNews() {
        withContext(Dispatchers.IO) {
            repository.loadHeadlines(
                preferenceHelper.getSelectedCountry(),
                category,
                page++,
                PAGE_SIZE
            ).onSuccess { response ->
                //val text = news.desc ?: "No description available"
                addAudioToQueue(
                    response?.newsList.orEmpty()
                )
                //withContext(Dispatchers.Main) { exoPlayer.mediaItemCount.toString() })
            }
        }
    }

    // TODO: call tts for all list items in parallel with async/await
    private suspend fun addAudioToQueue(newsList: List<News>) {
        val count = withContext(Dispatchers.Main) { exoPlayer.mediaItemCount }
        val deferredResults = newsList.mapIndexed { pos, news ->
            coroutineScope {
                async {
                    sendRequest {
                        ttsHelper.convertToAudioFile(
                            news.desc ?: "No description available", "${count + pos}"
                        )
                    }
                }
            }
        }
        deferredResults.awaitAll().forEachIndexed { index, result ->
            if (result is com.death14stroke.newsdaily.data.model.Result.Success) {
                val file = result.data
                val utteranceId = getUtteranceId(file.name)
                val mediaItem = MediaItem.Builder()
                    .setUri(file.toUri())
                    .setTag(AudioNews(file.toUri().toString(), newsList[index]))
                    .build()
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
                concatenatingMediaSource.addMediaSource(mediaSource)

                Timber.d("$utteranceId converted success")

                if (utteranceId == "0") {
                    audioNewsHandler.sendEmptyMessage(HandlerMessage.MSG_UPDATE_SOURCE.code)
                    mediaSessionCallback.onPlay()
                } else {
                    audioNewsHandler.sendEmptyMessageDelayed(
                        HandlerMessage.MSG_UPDATE_SOURCE.code,
                        WAIT_QUEUE_TIMEOUT_MS.toLong()
                    )
                }
            }
        }
    }

    private fun initExoPlayer() {
        exoPlayer.apply {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
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
                    player.getMediaItemAt(windowIndex).toAudioNews()?.getMediaDescription()
                        ?: MediaDescriptionCompat.Builder().build()
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
        mediaSessionCompat.setMetadata(exoPlayer.getMediaItemAt(pos).toAudioNews()?.buildMetaData())
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            audioNewsHandler.post {
                exoPlayer.playWhenReady = true
                mediaSessionCompat.isActive = true

                val pos = exoPlayer.currentMediaItemIndex
                setCurrentAudio(pos)
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING, pos)
            }
        }

        override fun onPause() {
            super.onPause()
            audioNewsHandler.post {
                exoPlayer.playWhenReady = false
                setMediaPlaybackState(
                    PlaybackStateCompat.STATE_PAUSED,
                    exoPlayer.currentMediaItemIndex
                )
            }
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            audioNewsHandler.post {
                if (exoPlayer.hasNextMediaItem())
                    exoPlayer.seekToNextMediaItem()
            }
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            audioNewsHandler.post {
                if (exoPlayer.hasPreviousMediaItem())
                    exoPlayer.seekToPreviousMediaItem()
            }
        }
    }

    private inner class AudioNewsHandler : Handler.Callback {
        override fun handleMessage(message: Message): Boolean {

            when (HandlerMessage.from(message.what)) {
                HandlerMessage.MSG_INIT_NEWS -> {
                    audioNewsHandler.removeCallbacksAndMessages(null)
                    concatenatingMediaSource.clear()
                    Timber.i("Message init received")
                    launch { loadNews() }
                }

                HandlerMessage.MSG_STOP_SERVICE -> {
                    audioNewsHandler.removeCallbacksAndMessages(null)
                    stopForeground(true)
                    stopSelf()
                }

                HandlerMessage.MSG_UPDATE_SOURCE -> {
                    audioNewsHandler.removeMessages(HandlerMessage.MSG_UPDATE_SOURCE.code)
                    Timber.i("MSG_UPDATE_SOURCE received")
                    exoPlayer.setMediaSource(concatenatingMediaSource, false)
                    exoPlayer.prepare()
                }

                HandlerMessage.MSG_SHOW_NOTI -> {
                    val icon = message.arg1
                    val category = message.obj as Category
                    val playWhenReady = message.arg2 == 1
                    val metadataCompat = mediaSessionCompat.controller.metadata

                    launch {
                        val notificationBuilder = buildNotification(
                            icon,
                            category.value,
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
                else -> {}
            }

            return false
        }
    }
}

private fun MediaItem.toAudioNews() = localConfiguration?.tag as AudioNews?