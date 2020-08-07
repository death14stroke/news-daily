package com.andruid.magic.newsdaily.database.entity

import androidx.room.Entity
import com.andruid.magic.newsloader.data.model.News

@Entity(tableName = "offline_news", primaryKeys = ["title", "category"])
data class NewsItem(
    val title: String,
    val sourceName: String,
    val desc: String?,
    val url: String,
    val imageUrl: String?,
    val published: Long,
    val category: String
)

fun News.toNewsItem(category: String): NewsItem {
    return NewsItem(
        title = title,
        sourceName = sourceName,
        desc = desc,
        url = url,
        imageUrl = imageUrl,
        published = published,
        category = category
    )
}