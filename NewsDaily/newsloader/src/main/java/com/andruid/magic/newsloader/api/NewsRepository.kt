package com.andruid.magic.newsloader.api

import android.app.Application
import android.util.Log
import com.andruid.magic.newsloader.server.RetrofitClient
import com.andruid.magic.newsloader.server.RetrofitService

class NewsRepository {
    companion object {
        private val TAG = NewsRepository::class.java.simpleName

        private lateinit var service: RetrofitService
        private lateinit var INSTANCE: NewsRepository
        private val LOCK = Any()

        @JvmStatic
        fun init(application: Application) {
            service =
                RetrofitClient.getRetrofitInstance(application).create(RetrofitService::class.java)
        }

        @JvmStatic
        fun getInstance(): NewsRepository {
            if (!::INSTANCE.isInitialized) {
                synchronized(LOCK) {
                    Log.d(TAG, "Created news repository instance")
                    INSTANCE = NewsRepository()
                }
            }
            return INSTANCE
        }
    }

    suspend fun loadHeadlines(country: String, category: String, page: Int, pageSize: Int) =
        service.getHeadlines(country, category, page, pageSize)

    suspend fun loadArticles(language: String, query: String, page: Int, pageSize: Int) =
        service.getArticles(language, query, page, pageSize)
}