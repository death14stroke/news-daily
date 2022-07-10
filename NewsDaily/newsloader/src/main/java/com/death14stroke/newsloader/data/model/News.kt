package com.death14stroke.newsloader.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class News(
    @SerialName("source")
    val source: Source,
    val title: String,
    @SerialName("description")
    val desc: String?,
    val url: String,
    @SerialName("urlToImage")
    val imageUrl: String?,
    @SerialName("publishedAt")
    val published: String
) : Parcelable