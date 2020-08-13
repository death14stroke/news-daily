package com.andruid.magic.newsdaily.util

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
import androidx.media.session.MediaButtonReceiver
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.ACTION_NOTI_CLICK
import com.andruid.magic.newsdaily.data.EXTRA_CATEGORY
import com.andruid.magic.newsdaily.ui.activity.HomeActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

const val AUDIO_CHANNEL_NAME = "AudioNews"
const val AUDIO_CHANNEL_ID = "audio_news_channel"

const val NEW_CONTENT_CHANNEL_NAME = "New Content"
const val NEW_CONTENT_CHANNEL_ID = "new_content_channel"

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
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        setContentTitle(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
        setContentText(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
        setSubText(category.capitalize())
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
            val bitmap = withContext(Dispatchers.IO) { Picasso.get().load(albumArtUri).get() }
            setLargeIcon(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

fun Context.buildNewContentNotification(count: Int): NotificationCompat.Builder {
    val intent = Intent(this, HomeActivity::class.java)
        .setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)

    val notificationManager = getSystemService<NotificationManager>()!!
    var importance = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        importance = NotificationManager.IMPORTANCE_DEFAULT

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel =
            NotificationChannel(
                NEW_CONTENT_CHANNEL_ID,
                NEW_CONTENT_CHANNEL_NAME,
                importance
            ).apply {
                enableLights(true)
                lightColor = Color.GREEN
            }
        notificationManager.createNotificationChannel(notificationChannel)
    }

    return NotificationCompat.Builder(this, NEW_CONTENT_CHANNEL_ID).apply {
        setSmallIcon(R.drawable.logo_round)

        priority = NotificationCompat.PRIORITY_DEFAULT
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setAutoCancel(true)
        setOnlyAlertOnce(true)

        setContentIntent(
            PendingIntent.getActivity(
                this@buildNewContentNotification,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        setContentTitle(getString(R.string.new_content_title))
        setContentText(getString(R.string.new_content_desc, count))
        setShowWhen(true)
    }
}