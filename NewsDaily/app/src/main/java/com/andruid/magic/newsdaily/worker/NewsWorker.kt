package com.andruid.magic.newsdaily.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.PAGE_SIZE_WORKER
import com.andruid.magic.newsdaily.database.entity.toNewsItem
import com.andruid.magic.newsdaily.database.repository.DbRepository
import com.andruid.magic.newsdaily.util.*
import com.andruid.magic.newsloader.api.NewsRepository
import com.andruid.magic.newsloader.data.model.Result.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        logi("running news worker")

        val latestNewsTime = DbRepository.getLatestNewsTime() ?: 0
        var total = 0
        applicationContext.resources.getStringArray(R.array.categories).forEach { category ->
            total += fetchNews(category, latestNewsTime)
        }

        logi("fetched $total news")
        if (total > 0) {
            val builder = applicationContext.buildNewContentNotification(total)
            applicationContext.getSystemService<NotificationManager>()?.notify(2, builder.build())
        }
        return Result.success()
    }

    private suspend fun fetchNews(category: String, latestNewsTime: Long): Int {
        var hasMore = true
        var page = 1
        val country = applicationContext.getSelectedCountry()
        var total = 0

        while (hasMore) {
            logi("fetching $category news for $country")
            val result = withContext(Dispatchers.IO) {
                NewsRepository.loadHeadlines(
                    country,
                    category,
                    page++,
                    PAGE_SIZE_WORKER
                )
            }

            if (result is Success && result.data != null) {
                val news =
                    result.data!!.newsOnlineList.map { news -> news.toNewsItem(country, category) }
                        .also { logd("$category news loaded from server") }
                hasMore = result.data!!.hasMore

                DbRepository.insertAll(news)
                val count = news.count { newsItem -> newsItem.published >= latestNewsTime }
                total += count

                logd("fetched = ${news.size}, new = $count, total new = $total")

                if (count != PAGE_SIZE_WORKER)
                    break
            } else {
                loge("could not fetch $category news from server")
                break
            }
        }

        return total
    }
}