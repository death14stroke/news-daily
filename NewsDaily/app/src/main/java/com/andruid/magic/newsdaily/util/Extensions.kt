package com.andruid.magic.newsdaily.util

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.andruid.magic.newsdaily.model.AudioNews

class Extensions {
    companion object {
        @JvmStatic
        fun AudioNews.getMediaDescription() : MediaDescriptionCompat {
            val extras = Bundle()
            extras.apply {
                putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, Uri.parse(news.imageUrl))
                putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, Uri.parse(news.imageUrl))
            }
            return MediaDescriptionCompat.Builder()
                    .setMediaId(uri)
                    .setIconUri(Uri.parse(news.imageUrl))
                    .setTitle(news.title)
                    .setDescription(news.desc)
                    .setExtras(extras)
                    .build()
        }

        @JvmStatic
        fun AudioNews.buildMetaData() : MediaMetadataCompat {
            return MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, news.imageUrl)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, news.sourceName)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, news.title)
                    .build()
        }
    }
}