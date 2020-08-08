package com.andruid.magic.newsdaily.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.andruid.magic.newsdaily.worker.WorkerScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED)
            WorkerScheduler.scheduleNewsWorker(context)
    }
}
