package com.andruid.magic.newsdaily.model

import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.os.bundleOf
import com.andruid.magic.newsloader.model.NewsOnline

data class AudioNews(
    val uri: String,
    val newsOnline: NewsOnline
)

fun AudioNews.getMediaDescription(): MediaDescriptionCompat {
    val extras = bundleOf(
        MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI to newsOnline.imageUrl,
        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI to newsOnline.imageUrl,
        MediaMetadataCompat.METADATA_KEY_ALBUM to newsOnline.sourceName
    )
    return MediaDescriptionCompat.Builder()
        .setMediaId(uri)
        .setIconUri(Uri.parse(newsOnline.imageUrl ?: ""))
        .setTitle(newsOnline.title)
        .setDescription(newsOnline.desc)
        .setExtras(extras)
        .build()
}

fun AudioNews.buildMetaData(): MediaMetadataCompat {
    return MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, newsOnline.imageUrl)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, newsOnline.sourceName)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, newsOnline.title)
        .build()
}