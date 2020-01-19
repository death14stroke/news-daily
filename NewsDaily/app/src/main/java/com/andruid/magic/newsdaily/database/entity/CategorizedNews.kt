package com.andruid.magic.newsdaily.database.entity

import androidx.room.Entity

@Entity(tableName = "categorized_news", primaryKeys = ["sourceName", "title", "published"])
data class CategorizedNews(
    val sourceName: String,
    val title: String,
    val category: String,
    val published: Long
)