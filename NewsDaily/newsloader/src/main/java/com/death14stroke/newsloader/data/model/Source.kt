package com.death14stroke.newsloader.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Data model for the news source
 */
@Serializable
@Parcelize
data class Source(
    val id: String?,
    val name: String
) : Parcelable