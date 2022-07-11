package com.death14stroke.newsloader.api

import com.death14stroke.newsloader.data.model.ApiResponse
import com.death14stroke.newsloader.data.model.Category

/**
 * Helper class to fetch news from a remote source
 */
sealed interface NetworkNewsHelper {
    /**
     * Fetch news headlines for selected [country] and [category]
     * @param page current page for pagination
     * @param pageSize max limit for items in a page
     */
    suspend fun loadHeadlines(
        country: String,
        category: Category,
        page: Int,
        pageSize: Int
    ): ApiResponse

    /**
     * Fetch articles for a search [query] in selected [language]
     * @param page current page for pagination
     * @param pageSize max limit for items in a page
     */
    suspend fun loadArticles(
        language: String,
        query: String,
        page: Int,
        pageSize: Int
    ): ApiResponse
}