package com.death14stroke.newsloader.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Source(
    val id: String?,
    val name: String
): Parcelable
