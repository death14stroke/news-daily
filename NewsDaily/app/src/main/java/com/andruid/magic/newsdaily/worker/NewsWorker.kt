package com.andruid.magic.newsdaily.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.database.repository.DbRepository
import com.andruid.magic.newsloader.data.api.NewsRepository
import com.andruid.magic.newsloader.data.model.News
import com.andruid.magic.newsloader.data.model.Result.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        applicationContext.resources.getStringArray(R.array.categories).forEach { category ->
            fetchNews(category)
        }

        return Result.success()
    }

    private suspend fun fetchNews(category: String) {
        var hasMore = true
        var page = 1

        while (hasMore) {
            Log.d("newsLog", "fetching $category news")
            val result = withContext(Dispatchers.IO) { NewsRepository.loadHeadlines("in", category, page++, 50) }
            Log.d("newsLog", "status = ${result.status}, data = ${result.data ?: "null"}")
            if (result.status == Status.SUCCESS && result.data != null) {
                val news = result.data!!.newsOnlineList.map { news -> news.toNewsItem(category) }
                hasMore = result.data!!.hasMore

                DbRepository.insertAll(news)
            } else
                break
        }
    }

    private fun News.toNewsItem(category: String): NewsItem {
        return NewsItem(
            title = title,
            sourceName = sourceName,
            desc = desc,
            url = url,
            imageUrl = imageUrl,
            published = published,
            category = category
        )
    }
}