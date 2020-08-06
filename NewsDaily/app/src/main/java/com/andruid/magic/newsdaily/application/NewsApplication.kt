package com.andruid.magic.newsdaily.application

import android.app.Application
import com.andruid.magic.newsdaily.database.repository.DbRepository
import com.andruid.magic.newsdaily.worker.WorkerScheduler
import com.andruid.magic.newsloader.data.api.NewsRepository

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        NewsRepository.init(this)
        DbRepository.init(this)

        WorkerScheduler.scheduleNewsWorker(this)
    }
}