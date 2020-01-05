package com.andruid.magic.newsdaily.articles

import androidx.paging.PageKeyedDataSource
import com.andruid.magic.newsloader.api.NewsRepository.Companion.getInstance
import com.andruid.magic.newsloader.api.NewsRepository.NewsLoadedListener
import com.andruid.magic.newsloader.data.Constants
import com.andruid.magic.newsloader.model.News

class ArticlesDataSource(private val language : String, private val query : String) : PageKeyedDataSource<Int, News>() {

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, News>) {
        getInstance().loadArticles(language, query, Constants.FIRST_PAGE, Constants.PAGE_SIZE, object : NewsLoadedListener {
            override fun onSuccess(newsList: List<News>, hasMore: Boolean) {
                callback.onResult(newsList, null, Constants.FIRST_PAGE + 1)
            }

            override fun onFailure(t: Throwable?) {
                t!!.printStackTrace()
            }
        })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, News>) {
        getInstance().loadArticles(language, query, params.key, Constants.PAGE_SIZE, object : NewsLoadedListener {
            override fun onSuccess(newsList: List<News>, hasMore: Boolean) {
                val key = if (hasMore) params.key + 1 else null
                callback.onResult(newsList, key)
            }

            override fun onFailure(t: Throwable?) {
                t!!.printStackTrace()
            }
        })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, News>) {
        getInstance().loadArticles(language, query, params.key, Constants.PAGE_SIZE, object : NewsLoadedListener {
            override fun onSuccess(newsList: List<News>, hasMore: Boolean) {
                val adjacentKey = if (params.key > Constants.FIRST_PAGE) params.key - 1 else null
                callback.onResult(newsList, adjacentKey)
            }

            override fun onFailure(t: Throwable?) {
                t!!.printStackTrace()
            }
        })
    }
}