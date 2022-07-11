package com.death14stroke.newsloader.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model for news fetched from the network
 * @property desc short content summary of the news
 * @property url url of the news article
 * @property imageUrl url of the image associated with the news
 * @property published string containing date and time for when the article was published
 */
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