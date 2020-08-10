package com.andruid.magic.newsdaily.application

import android.app.Application
import com.andruid.magic.newsdaily.database.repository.DbRepository
import com.andruid.magic.newsdaily.worker.WorkerScheduler
import com.andruid.magic.newsloader.api.NewsRepository
import com.andruid.magic.texttoaudiofile.api.TtsApi
import com.blongho.country_data.World

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        NewsRepository.init(this)
        DbRepository.init(this)
        World.init(this)
        TtsApi.init(this)

        WorkerScheduler.scheduleNewsWorker(this)
    }
}