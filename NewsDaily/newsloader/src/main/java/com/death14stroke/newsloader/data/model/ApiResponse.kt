package com.death14stroke.newsloader.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model for response returned from the server
 * @property newsList news objects fetched in the current page
 * @property totalResults count of total news available for particular query
 */
@Serializable
data class ApiResponse(
    @SerialName("articles")
    val newsList: List<News>,
    val totalResults: Int
)