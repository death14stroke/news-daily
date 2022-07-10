package com.death14stroke.newsloader.api

import com.death14stroke.newsloader.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

sealed interface RetrofitService {
    @GET("/highlights")
    suspend fun getHeadlines(
        @Query("country") country: String, @Query("category") category: String,
        @Query("page") page: Int, @Query("page_size") pageSize: Int
    ): Response<ApiResponse>

    @GET("/search")
    suspend fun getArticles(
        @Query("language") language: String, @Query("query") query: String,
        @Query("page") page: Int, @Query("pageSize") pageSize: Int
    ): Response<ApiResponse>
}