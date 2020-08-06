package com.andruid.magic.newsloader.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("news")
    val newsOnlineList: List<News>,
    val hasMore: Boolean
)