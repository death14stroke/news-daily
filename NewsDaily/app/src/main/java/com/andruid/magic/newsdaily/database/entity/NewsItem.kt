package com.andruid.magic.newsdaily.database.entity

import androidx.room.Entity

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