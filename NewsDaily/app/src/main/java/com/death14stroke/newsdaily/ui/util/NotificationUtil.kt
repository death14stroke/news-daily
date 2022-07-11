package com.death14stroke.newsdaily.ui.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.media.session.MediaButtonReceiver
import coil.imageLoader
import coil.request.ImageRequest
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.ACTION_NOTI_CLICK
import com.death14stroke.newsdaily.data.EXTRA_CATEGORY
import com.death14stroke.newsdaily.ui.activity.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

private const val AUDIO_CHANNEL_NAME = "AudioNews"
private const val AUDIO_CHANNEL_ID = "audio_news_channel"

/**
 * Build notification for media controls
 */
@Suppress("BlockingMethodInNonBlockingContext")
@SuppressLint("DefaultLocale")
suspend fun Context.buildNotification(
    icon: Int,
    category: String,
    metadataCompat: MediaMetadataCompat,
    token: MediaSessionCompat.Token
): NotificationCompat.Builder {
    val intent = Intent(this, HomeActivity::class.java)
        .setAction(ACTION_NOTI_CLICK)
        .setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        .putExtra(EXTRA_CATEGORY, category)

    val notificationManager = getSystemService<NotificationManager>()!!
    var importance = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        importance = NotificationManager.IMPORTANCE_HIGH

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            AUDIO_CHANNEL_ID,
            AUDIO_CHANNEL_NAME, importance
        ).apply {
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(notificationChannel)
    }

    return NotificationCompat.Builder(this, AUDIO_CHANNEL_ID).apply {
        setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true)
        )
        setSmallIcon(R.drawable.logo_round)

        priority = NotificationCompat.PRIORITY_MAX
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setOnlyAlertOnce(true)

        setContentIntent(
            PendingIntent.getActivity(
                this@buildNotification,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        )
        setContentTitle(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
        setContentText(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
        setSubText(category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
        setShowWhen(true)

        var pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            this@buildNotification,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
        addAction(android.R.drawable.ic_media_previous, "previous", pendingIntent)

        pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            this@buildNotification,
            PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
        addAction(icon, "play", pendingIntent)

        pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            this@buildNotification,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )
        addAction(android.R.drawable.ic_media_next, "next", pendingIntent)

        val albumArtUri =
            metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
        try {
            val request = ImageRequest.Builder(this@buildNotification)
                .data(albumArtUri)
                .build()
            val bitmap =
                withContext(Dispatchers.IO) { imageLoader.execute(request).drawable?.toBitmap() }
            setLargeIcon(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}