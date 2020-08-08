package com.andruid.magic.newsdaily.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.database.entity.toNewsItem
import com.andruid.magic.newsdaily.database.repository.DbRepository
import com.andruid.magic.newsdaily.util.getSelectedCountry
import com.andruid.magic.newsloader.api.NewsRepository
import com.andruid.magic.newsloader.data.model.Result.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        Log.d("newsLog", "running news worker")

        applicationContext.resources.getStringArray(R.array.categories).forEach { category ->
            fetchNews(category)
        }

        return Result.success()
    }

    private suspend fun fetchNews(category: String) {
        var hasMore = true
        var page = 1
        val country = applicationContext.getSelectedCountry()

        while (hasMore) {
            Log.d("newsLog", "fetching $category news for $country")
            val result = withContext(Dispatchers.IO) {
                NewsRepository.loadHeadlines(
                    country,
                    category,
                    page++,
                    50
                )
            }

            if (result is Success && result.data != null) {
                val news = result.data!!.newsOnlineList.map { news -> news.toNewsItem(country, category) }
                hasMore = result.data!!.hasMore

                DbRepository.insertAll(news)
            } else
                break
        }
    }
}