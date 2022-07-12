package com.death14stroke.newsdaily.ui.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.ACTION_NOTI_CLICK
import com.death14stroke.newsdaily.data.EXTRA_CATEGORY
import com.death14stroke.newsdaily.ui.activity.HomeActivity
import com.death14stroke.newsloader.data.model.Category

const val AUDIO_CHANNEL_NAME = "AudioNews"
const val AUDIO_CHANNEL_ID = "audio_news_channel"

fun Context.buildLoadingNotification(category: Category): NotificationCompat.Builder {
    val notificationManager = getSystemService<NotificationManager>()!!
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val notificationChannel = NotificationChannel(
        AUDIO_CHANNEL_ID, AUDIO_CHANNEL_NAME, importance
    ).apply {
        enableLights(true)
        lightColor = Color.GREEN
    }
    notificationManager.createNotificationChannel(notificationChannel)

    return NotificationCompat.Builder(this, AUDIO_CHANNEL_ID).apply {
        setSmallIcon(R.drawable.logo_round)

        priority = NotificationCompat.PRIORITY_DEFAULT
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setAutoCancel(true)
        setOnlyAlertOnce(true)
        setProgress(100, 100, true)

        setContentIntent(buildPendingIntentForHomePage(category))
        setContentTitle(getString(R.string.loading))
        setContentText(getString(R.string.fetching_news_for, category.value))
        setShowWhen(true)
    }
}

fun Context.buildPendingIntentForHomePage(category: Category): PendingIntent {
    val intent = Intent(this, HomeActivity::class.java)
        .setAction(ACTION_NOTI_CLICK)
        .setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        .putExtra(EXTRA_CATEGORY, category)
    return PendingIntent.getActivity(
        this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
}