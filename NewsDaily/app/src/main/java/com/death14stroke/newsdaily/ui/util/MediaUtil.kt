package com.death14stroke.newsdaily.ui.util

import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.os.bundleOf
import com.death14stroke.newsdaily.data.model.AudioNews
import com.google.android.exoplayer2.MediaItem

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

private fun MediaItem.toAudioNews() = localConfiguration?.tag as AudioNews?

fun MediaItem.getMediaDescription(): MediaDescriptionCompat =
    toAudioNews()?.getMediaDescription() ?: MediaDescriptionCompat.Builder().build()

fun MediaItem.getTitle() = toAudioNews()?.news?.title

fun MediaItem.getSource() = toAudioNews()?.news?.source?.name

fun MediaItem.getAlbumArtUri() = toAudioNews()?.news?.imageUrl