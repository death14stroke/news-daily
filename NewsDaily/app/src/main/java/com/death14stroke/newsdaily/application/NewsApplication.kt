package com.death14stroke.newsdaily.application

import android.app.Application
import com.death14stroke.newsdaily.BuildConfig
import com.death14stroke.newsdaily.di.repoModule
import com.death14stroke.newsdaily.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // logging setup
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // koin dependency injection
        startKoin {
            androidContext(this@NewsApplication)
            modules(listOf(repoModule, viewModelModule))
        }
    }
}