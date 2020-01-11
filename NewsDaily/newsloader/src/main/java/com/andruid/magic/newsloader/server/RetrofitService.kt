package com.andruid.magic.newsloader.server

import com.andruid.magic.newsloader.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {
    @GET("/headlines")
    suspend fun getHeadlines(
        @Query("country") country: String, @Query("category") category: String,
        @Query("page") page: Int, @Query("page_size") pageSize: Int
    ): Response<ApiResponse>

    @GET("/articles")
    suspend fun getArticles(
        @Query("language") language: String, @Query("query") query: String,
        @Query("page") page: Int, @Query("page_size") pageSize: Int
    ): Response<ApiResponse>
}