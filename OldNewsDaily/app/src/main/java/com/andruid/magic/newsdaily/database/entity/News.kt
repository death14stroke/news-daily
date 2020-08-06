package com.andruid.magic.newsdaily.database.entity

import android.os.Parcelable
import androidx.room.Entity
import com.andruid.magic.newsloader.model.NewsOnline
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "offline_news", primaryKeys = ["sourceName", "title", "published"])
@Parcelize
data class News(
    val sourceName: String,
    val title: String,
    val desc: String?,
    val url: String,
    val imageUrl: String?,
    val published: Long
) : Parcelable

fun NewsOnline.toNews(): News {
    return News(
        sourceName = sourceName,
        title = title,
        desc = desc,
        url = url,
        imageUrl = imageUrl,
        published = published
    )
}

fun News.toCatNews(category: String): CategorizedNews {
    return CategorizedNews(
        sourceName = sourceName,
        title = title,
        category = category,
        published = published
    )
}