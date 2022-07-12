package com.death14stroke.newsdaily.service

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import coil.imageLoader
import coil.request.ImageRequest
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.ACTION_PREPARE_AUDIO
import com.death14stroke.newsdaily.data.EXTRA_CATEGORY
import com.death14stroke.newsdaily.data.PAGE_SIZE
import com.death14stroke.newsdaily.data.model.*
import com.death14stroke.newsdaily.data.repository.MainRepository
import com.death14stroke.newsdaily.ui.util.*
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
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

/**
 * Audio service for listening the news using TextToSpeech
 */
class AudioNewsService : MediaBrowserServiceCompat(), CoroutineScope {
    companion object {
        private const val MEDIA_SERVICE = "AudioNewsService"
        private const val WAIT_QUEUE_TIMEOUT_MS = 5000L
        private const val NEWS_FETCH_DISTANCE = 3
        private const val MEDIA_NOTI_ID = 1
    }

    private val repository by inject<MainRepository>()
    private val ttsHelper by inject<TtsHelper>()

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val mediaSessionCompat by lazy {
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON).setClass(
            this,
            MediaButtonReceiver::class.java
        )
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)
        MediaSessionCompat(applicationContext, MEDIA_SERVICE, mediaButtonReceiver, pendingIntent)
    }
    private val exoPlayer by lazy {
        ExoPlayer.Builder(this)
            .setLooper(Looper.getMainLooper())
            .build()
    }
    private val playerNotificationManager by lazy {
        PlayerNotificationManager.Builder(this, MEDIA_NOTI_ID, AUDIO_CHANNEL_ID)
            .setMediaDescriptionAdapter(DescriptionAdapter())
            .setNotificationListener(NotificationListener())
            .build().apply {
                setUseChronometer(true)
                setUseFastForwardAction(false)
                setUseRewindAction(false)
            }
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
        Timber.d("onCreate() called")

        job.start()
        initExoPlayer()
        initMediaSession()
        initNotificationManager()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ACTION_PREPARE_AUDIO == intent.action) {
            category = intent.getSerializableExtra(EXTRA_CATEGORY) as Category? ?: Category.GENERAL
            startForeground(MEDIA_NOTI_ID, buildLoadingNotification(category).build())
            ttsHelper.initialize { success ->
                if (success)
                    audioNewsHandler.sendEmptyMessage(HandlerMessage.MSG_INIT_NEWS.code)
                else {
                    Timber.e("Could not initialize text to speech. Stopping the service")
                    toast(getString(R.string.tts_init_error))
                    audioNewsHandler.sendEmptyMessage(HandlerMessage.MSG_STOP_SERVICE.code)
                }
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
        playerNotificationManager.setPlayer(null)
        exoPlayer.release()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?) =
        BrowserRoot(getString(R.string.app_name), null)

    override fun onLoadChildren(
        parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(mutableListOf())
    }

    private suspend fun loadNews() {
        repository.loadHeadlines(category = category, page = page++, pageSize = PAGE_SIZE)
            .onSuccess { response -> addAudioToQueue(response?.newsList.orEmpty()) }
    }

    private suspend fun addAudioToQueue(newsList: List<News>) {
        val count = withContext(Dispatchers.Main) { exoPlayer.mediaItemCount }
        val deferredResults = newsList.mapIndexed { pos, news ->
            coroutineScope {
                async {
                    sendRequest {
                        ttsHelper.convertToAudioFile(
                            news.desc ?: getString(R.string.description_not_found), "${count + pos}"
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
                        WAIT_QUEUE_TIMEOUT_MS
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
            addListener(PlayerEventListener())
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
                    player.getMediaItemAt(windowIndex).getMediaDescription()
            })
            setPlayer(exoPlayer)
        }
    }

    private fun initNotificationManager() {
        playerNotificationManager.setPlayer(exoPlayer)
        playerNotificationManager.setMediaSessionToken(mediaSessionCompat.sessionToken)
    }

    private fun setMediaPlaybackState(state: Int, pos: Int) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setActiveQueueItemId(pos.toLong())

        when (state) {
            PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING, PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS, PlaybackStateCompat.STATE_SKIPPING_TO_NEXT -> {
                playbackStateBuilder.setActions(
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_STOP
                )
            }
            else -> {
                playbackStateBuilder.setActions(
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_STOP
                )
            }
        }

        when (state) {
            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_PLAYING -> {
                try {
                    playbackStateBuilder.setState(state, exoPlayer.currentPosition, 0f)
                } catch (e: IllegalStateException) {
                    Timber.e("Invalid exoplayer state", e)
                }
            }
            else -> {
                playbackStateBuilder.setState(
                    state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f
                )
            }
        }

        mediaSessionCompat.setPlaybackState(playbackStateBuilder.build())
    }

    private inner class PlayerEventListener : Player.Listener {
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            when (reason) {
                Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT, Player.DISCONTINUITY_REASON_SEEK,
                Player.DISCONTINUITY_REASON_AUTO_TRANSITION, Player.DISCONTINUITY_REASON_INTERNAL -> {
                    val pos = exoPlayer.currentMediaItemIndex
                    if (pos == exoPlayer.mediaItemCount - NEWS_FETCH_DISTANCE)
                        launch { loadNews() }
                }
            }
        }
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            launch {
                exoPlayer.playWhenReady = true
                mediaSessionCompat.isActive = true
                setMediaPlaybackState(
                    PlaybackStateCompat.STATE_PLAYING, exoPlayer.currentMediaItemIndex
                )
            }
        }

        override fun onPause() {
            super.onPause()
            launch {
                exoPlayer.playWhenReady = false
                setMediaPlaybackState(
                    PlaybackStateCompat.STATE_PAUSED, exoPlayer.currentMediaItemIndex
                )
            }
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            launch {
                if (exoPlayer.hasNextMediaItem())
                    exoPlayer.seekToNextMediaItem()
            }
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            launch {
                if (exoPlayer.hasPreviousMediaItem())
                    exoPlayer.seekToPreviousMediaItem()
            }
        }
    }

    private inner class AudioNewsHandler : Handler.Callback {
        override fun handleMessage(message: Message): Boolean {
            val messageCode = HandlerMessage.from(message.what).also {
                Timber.i("Handler message received = $it")
            }

            when (messageCode) {
                HandlerMessage.MSG_INIT_NEWS -> {
                    audioNewsHandler.removeCallbacksAndMessages(null)
                    concatenatingMediaSource.clear()
                    launch { loadNews() }
                }

                HandlerMessage.MSG_STOP_SERVICE -> {
                    audioNewsHandler.removeCallbacksAndMessages(null)
                    stopForeground(true)
                    stopSelf()
                }

                HandlerMessage.MSG_UPDATE_SOURCE -> {
                    audioNewsHandler.removeMessages(HandlerMessage.MSG_UPDATE_SOURCE.code)
                    exoPlayer.setMediaSource(concatenatingMediaSource, false)
                    exoPlayer.prepare()
                }
            }

            return false
        }
    }

    private inner class DescriptionAdapter : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player) =
            player.currentMediaItem?.getTitle() ?: getString(R.string.title_not_found)

        override fun createCurrentContentIntent(player: Player) =
            buildPendingIntentForHomePage(category)

        override fun getCurrentContentText(player: Player) =
            player.currentMediaItem?.getSource() ?: getString(R.string.no_author_found)

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val albumArtUri = player.currentMediaItem?.getAlbumArtUri()
            val request = ImageRequest.Builder(this@AudioNewsService)
                .data(albumArtUri)
                .target { drawable -> callback.onBitmap(drawable.toBitmap()) }
                .build()
            imageLoader.enqueue(request)

            return null
        }
    }

    private inner class NotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int, notification: Notification, ongoing: Boolean
        ) {
            super.onNotificationPosted(notificationId, notification, ongoing)
            startForeground(notificationId, notification)
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            super.onNotificationCancelled(notificationId, dismissedByUser)
            stopForeground(false)
        }
    }
}