package com.death14stroke.newsloader.api

import com.death14stroke.newsloader.data.model.Category
import com.death14stroke.newsloader.util.makeRetrofitCall

class NetworkNewsHelperImpl(private val service: RetrofitService) : NetworkNewsHelper {
    override suspend fun loadHeadlines(
        country: String,
        category: Category,
        page: Int,
        pageSize: Int
    ) = makeRetrofitCall { service.getHeadlines(country, category.value, page, pageSize) }

    override suspend fun loadArticles(
        language: String,
        query: String,
        page: Int,
        pageSize: Int
    ) = makeRetrofitCall { service.getArticles(language, query, page, pageSize) }
}