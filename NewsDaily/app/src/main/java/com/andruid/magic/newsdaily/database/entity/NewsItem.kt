package com.andruid.magic.newsdaily.database.entity

import androidx.room.Entity
import com.andruid.magic.newsloader.data.model.News

@Entity(tableName = "offline_news", primaryKeys = ["url", "category"])
data class NewsItem(
    val url: String,
    val title: String,
    val sourceName: String,
    val desc: String?,
    val imageUrl: String?,
    val published: Long,
    val country: String,
    val category: String
)

fun News.toNewsItem(country: String = "global", category: String = "search"): NewsItem {
    return NewsItem(
        title = title,
        sourceName = sourceName,
        desc = desc,
        url = url,
        imageUrl = imageUrl,
        published = published,
        category = category,
        country = country
    )
}