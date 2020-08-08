package com.andruid.magic.newsdaily.database.repository

import android.app.Application
import androidx.paging.PagingSource
import com.andruid.magic.newsdaily.database.NewsDatabase
import com.andruid.magic.newsdaily.database.entity.NewsItem

object DbRepository {
    private lateinit var database: NewsDatabase

    fun init(application: Application) {
        database = NewsDatabase.getInstance(application.applicationContext)
    }

    suspend fun insertAll(newsList: List<NewsItem>) {
        database.newsDao().insert(newsList)
    }

    fun getNews(country: String, category: String): PagingSource<Int, NewsItem> =
        database.newsDao().getNewsForCategory(country, category)
}