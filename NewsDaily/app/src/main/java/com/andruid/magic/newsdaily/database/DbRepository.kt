package com.andruid.magic.newsdaily.database

import android.app.Application
import com.andruid.magic.newsdaily.database.entity.CategorizedNews
import com.andruid.magic.newsdaily.database.entity.News

class DbRepository {
    companion object {
        private lateinit var database: NewsDatabase
        private lateinit var INSTANCE: DbRepository

        @JvmStatic
        fun init(application: Application) {
            database = NewsDatabase.getInstance(application.applicationContext)
            INSTANCE = DbRepository()
        }

        @JvmStatic
        fun getInstance(): DbRepository {
            if (!::INSTANCE.isInitialized)
                throw Exception("must call init() first in application class")
            return INSTANCE
        }
    }

    fun insertAll(newsList: List<News>) = database.newsDao().insertAll(newsList)

    fun getNews(category: String) = database.newsDao().getNews(category)

    fun insertCat(catNewsList: List<CategorizedNews>) =
        database.catNewsDao().insertAll(catNewsList)
}