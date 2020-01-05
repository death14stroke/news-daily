package com.andruid.magic.newsdaily.application

import android.app.Application
import com.andruid.magic.newsdaily.BuildConfig
import com.andruid.magic.newsloader.api.NewsRepository
import com.andruid.magic.texttoaudiofile.api.TtsApi
import com.andruid.magic.texttoaudiofile.api.TtsApi.Companion.release
import com.blongho.country_data.World
import timber.log.Timber
import timber.log.Timber.DebugTree

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG)
            Timber.plant(DebugTree())
        World.init(this)
        NewsRepository.init(this)
        TtsApi.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        release()
    }
}