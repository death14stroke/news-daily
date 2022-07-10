package com.death14stroke.newsloader.api

import com.death14stroke.newsloader.data.model.ApiResponse
import com.death14stroke.newsloader.data.model.Result

sealed interface NetworkNewsHelper {
    suspend fun loadHeadlines(
        country: String,
        category: String,
        page: Int,
        pageSize: Int
    ): Result<ApiResponse>

    suspend fun loadArticles(
        language: String,
        query: String,
        page: Int,
        pageSize: Int
    ): Result<ApiResponse>
}