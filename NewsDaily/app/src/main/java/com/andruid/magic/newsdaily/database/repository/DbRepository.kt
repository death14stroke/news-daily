package com.andruid.magic.newsdaily.database.repository

import android.app.Application
import android.util.Log
import androidx.paging.PagingSource
import com.andruid.magic.newsdaily.database.NewsDatabase
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.database.entity.ReadNews
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

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

    suspend fun getNewsForPage(
        country: String,
        category: String,
        page: Int,
        pageSize: Int
    ): List<NewsItem> {
        Log.d("audioLog", "offset = ${page * pageSize}, pageSize = $pageSize")
        return database.newsDao()
            .getNewsForCategoryPage(country, category, page * pageSize, pageSize)
    }

    suspend fun insertReadNews(category: String, url: String) {
        Log.d("readLog", "marking news as read $url in $category")
        database.newsDao().insertReadNews(ReadNews(url = url, category = category))
    }

    suspend fun isUnread(newsItem: NewsItem) =
        database.newsDao().findRead(newsItem.category, newsItem.url) == 0

    fun countUnread(category: String): Flow<Int> {
        val totalFlow = database.newsDao().countTotal(category)
        val readFlow = database.newsDao().countRead(category)

        return totalFlow.combine(readFlow) { total, read -> total - read }
    }
}