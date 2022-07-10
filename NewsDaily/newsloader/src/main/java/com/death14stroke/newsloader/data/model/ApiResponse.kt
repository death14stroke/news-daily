package com.death14stroke.newsloader.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    @SerialName("articles")
    val newsOnlineList: List<News>,
    val totalResults: Int
)