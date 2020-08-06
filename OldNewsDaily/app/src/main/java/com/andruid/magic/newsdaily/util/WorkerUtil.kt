package com.andruid.magic.newsdaily.util

import android.content.Context
import androidx.work.*
import com.andruid.magic.newsdaily.worker.NewsWorker
import java.util.concurrent.TimeUnit

object WorkerUtil {
    @JvmStatic
    fun scheduleWorker(context: Context) {
        val request = PeriodicWorkRequestBuilder<NewsWorker>(
            repeatInterval = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
            repeatIntervalTimeUnit = TimeUnit.MILLISECONDS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()
        WorkManager.getInstance(context).enqueue(request)
    }
}