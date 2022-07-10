package com.death14stroke.newsloader.api

import com.death14stroke.newsloader.util.sendNetworkRequest

class NetworkNewsHelperImpl(private val service: RetrofitService) : NetworkNewsHelper {
    override suspend fun loadHeadlines(
        country: String,
        category: String,
        page: Int,
        pageSize: Int
    ) = sendNetworkRequest { service.getHeadlines(country, category, page, pageSize) }

    override suspend fun loadArticles(
        language: String,
        query: String,
        page: Int,
        pageSize: Int
    ) = sendNetworkRequest { service.getArticles(language, query, page, pageSize) }
}