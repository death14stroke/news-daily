package com.andruid.magic.newsloader.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @Expose
    @SerializedName("news")
    val newsList: List<News>,
    val hasMore: Boolean
)