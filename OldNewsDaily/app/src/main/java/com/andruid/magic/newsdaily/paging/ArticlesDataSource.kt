package com.andruid.magic.newsdaily.paging

import android.util.Log
import androidx.paging.PageKeyedDataSource
import com.andruid.magic.newsdaily.database.entity.News
import com.andruid.magic.newsdaily.database.entity.toNews
import com.andruid.magic.newsloader.api.NewsRepository
import com.andruid.magic.newsloader.data.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ArticlesDataSource(
    private val scope: CoroutineScope,
    private val language: String,
    private val query: String
) : PageKeyedDataSource<Int, News>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, News>
    ) {
        scope.launch {
            try {
                val response = NewsRepository.getInstance().loadArticles(
                    language = language,
                    query = query,
                    page = Constants.FIRST_PAGE,
                    pageSize = Constants.PAGE_SIZE
                )
                when {
                    response.isSuccessful -> {
                        val newsList =
                            response.body()?.newsOnlineList?.map { newsOnline -> newsOnline.toNews() }
                        Log.d("searchLog", "searchLog: found = ${newsList?.size}");
                        val hasMore = response.body()?.hasMore
                        callback.onResult(
                            newsList ?: listOf(), null,
                            if (hasMore!!) Constants.FIRST_PAGE + 1 else null
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, News>) {
        scope.launch {
            try {
                val response = NewsRepository.getInstance().loadArticles(
                    language = language,
                    query = query,
                    page = params.key,
                    pageSize = Constants.PAGE_SIZE
                )
                when {
                    response.isSuccessful -> {
                        val newsList =
                            response.body()?.newsOnlineList?.map { newsOnline -> newsOnline.toNews() }
                        val hasMore = response.body()?.hasMore
                        val key = if (hasMore!!) params.key + 1 else null
                        callback.onResult(newsList ?: listOf(), key)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, News>) {
        scope.launch {
            try {
                val response = NewsRepository.getInstance().loadArticles(
                    language = language,
                    query = query,
                    page = params.key,
                    pageSize = Constants.PAGE_SIZE
                )
                when {
                    response.isSuccessful -> {
                        val newsList =
                            response.body()?.newsOnlineList?.map { newsOnline -> newsOnline.toNews() }
                        val adjacentKey = if (params.key > Constants.FIRST_PAGE)
                            params.key - 1
                        else null
                        callback.onResult(newsList ?: listOf(), adjacentKey)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}