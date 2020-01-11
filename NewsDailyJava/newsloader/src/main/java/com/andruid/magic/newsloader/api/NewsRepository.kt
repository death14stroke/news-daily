package com.andruid.magic.newsloader.api

import android.app.Application
import com.andruid.magic.newsloader.model.ApiResponse
import com.andruid.magic.newsloader.model.News
import com.andruid.magic.newsloader.server.RetrofitClient
import com.andruid.magic.newsloader.server.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class NewsRepository {

    companion object {
        private lateinit var service : RetrofitService
        private lateinit var INSTANCE : NewsRepository
        private val LOCK = Any()

        @JvmStatic
        fun init(application : Application) {
            service = RetrofitClient.getRetrofitInstance(application).create(RetrofitService::class.java)
        }

        @JvmStatic
        fun getInstance(): NewsRepository {
            if (!::INSTANCE.isInitialized) {
                synchronized(LOCK) {
                    Timber.d("Created news repository instance")
                    INSTANCE = NewsRepository()
                }
            }
            return INSTANCE
        }
    }

    //TODO: use coroutines and deferred
    fun loadHeadlines(country: String, category: String, page: Int, pageSize: Int,
                      mListener: NewsLoadedListener) {
        service.getHeadlines(country, category, page, pageSize).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.body() == null)
                    return
                Timber.d("page: %d, size: %d", page, response.body()!!.newsList.size)
                val newsList = response.body()!!.newsList
                mListener.onSuccess(newsList, response.body()!!.hasMore)
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                mListener.onFailure(t)
            }
        })
    }

    //TODO: use coroutines and deferred
    fun loadArticles(language: String, query: String, page: Int, pageSize: Int,
                     mListener: NewsLoadedListener) {
        service.getArticles(language, query, page, pageSize).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.body() == null)
                    return
                Timber.d("page: %d, size: %d", page, response.body()!!.newsList.size)
                val newsList = response.body()!!.newsList
                mListener.onSuccess(newsList, response.body()!!.hasMore)
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                mListener.onFailure(t)
            }
        })
    }

    interface NewsLoadedListener {
        fun onSuccess(newsList: List<News>, hasMore: Boolean)
        fun onFailure(t: Throwable?)
    }
}