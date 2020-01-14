package com.andruid.magic.newsloader.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class News(
    val sourceName: String,
    val title: String,
    val desc: String?,
    val url: String,
    val imageUrl: String?,
    val published: Long
) : Parcelable