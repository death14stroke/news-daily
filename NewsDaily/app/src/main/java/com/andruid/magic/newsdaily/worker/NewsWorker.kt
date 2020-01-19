package com.andruid.magic.newsdaily.worker

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.database.DbRepository
import com.andruid.magic.newsdaily.database.entity.toCatNews
import com.andruid.magic.newsdaily.database.entity.toNews
import com.andruid.magic.newsdaily.util.PrefUtil
import com.andruid.magic.newsloader.api.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    companion object {
        private val TAG = NewsWorker::class.java.simpleName
    }
    override suspend fun doWork(): Result {
        Log.d(TAG, "worker started")
        val categories = arrayOf(
            "general", "business", "entertainment", "health",
            "science", "sports", "technology"
        )
        for (category in categories) {
            val country = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString(
                    applicationContext.getString(R.string.pref_country),
                    PrefUtil.getDefaultCountry(applicationContext)
                )
            withContext(Dispatchers.IO) {
                val resp = NewsRepository.getInstance().loadHeadlines(
                    country!!, category,
                    0, 100
                )
                if (resp.isSuccessful) {
                    resp.body()?.let {
                        val news = it.newsOnlineList.map { newsOnline -> newsOnline.toNews() }
                        DbRepository.getInstance().insertAll(news)
                        val catNews = news.map { n -> n.toCatNews(category) }
                        DbRepository.getInstance().insertCat(catNews)
                    }
                }
            }
        }
        return Result.success()
    }
}