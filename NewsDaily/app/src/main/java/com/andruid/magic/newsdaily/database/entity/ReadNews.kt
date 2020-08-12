package com.andruid.magic.newsdaily.database.entity

import androidx.room.Entity

@Entity(tableName = "read_news", primaryKeys = ["url", "category"])
data class ReadNews(
    val url: String,
    val category: String
)