package com.andruid.magic.newsloader.api

import android.app.Application
import com.andruid.magic.newsloader.data.model.ApiResponse
import com.andruid.magic.newsloader.data.model.Result
import com.andruid.magic.newsloader.data.server.RetrofitClient
import com.andruid.magic.newsloader.data.server.RetrofitService
import com.andruid.magic.newsloader.util.sendNetworkRequest

object NewsRepository {
    private lateinit var service: RetrofitService

    fun init(application: Application) {
        service =
            RetrofitClient.getRetrofitInstance(application).create(RetrofitService::class.java)
    }

    suspend fun loadHeadlines(
        country: String,
        category: String,
        page: Int,
        pageSize: Int
    ): Result<ApiResponse> =
        sendNetworkRequest { service.getHeadlines(country, category, page, pageSize) }

    suspend fun loadArticles(
        language: String,
        query: String,
        page: Int,
        pageSize: Int
    ): Result<ApiResponse> =
        sendNetworkRequest { service.getArticles(language, query, page, pageSize) }
}