package com.death14stroke.newsdaily.data.model

import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.os.bundleOf
import com.death14stroke.newsloader.data.model.News

data class AudioNews(
    val uri: String,
    val news: News
)

fun AudioNews.getMediaDescription(): MediaDescriptionCompat {
    val extras = bundleOf(
        MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI to news.imageUrl,
        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI to news.imageUrl,
        MediaMetadataCompat.METADATA_KEY_ALBUM to news.source.name
    )
    return MediaDescriptionCompat.Builder()
        .setMediaId(uri)
        .setIconUri(Uri.parse(news.imageUrl ?: ""))
        .setTitle(news.title)
        .setDescription(news.desc)
        .setExtras(extras)
        .build()
}

fun AudioNews.buildMetaData(): MediaMetadataCompat {
    return MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, news.imageUrl)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, news.source.name)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, news.title)
        .build()
}

fun News.toAudioNews() = AudioNews(uri = url, news = this)