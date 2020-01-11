package com.andruid.magic.newsdaily.application

import android.app.Application
import com.andruid.magic.newsloader.api.NewsRepository

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NewsRepository.init(this)
    }
}