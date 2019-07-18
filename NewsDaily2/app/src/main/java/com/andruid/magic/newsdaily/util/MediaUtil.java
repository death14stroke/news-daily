package com.andruid.magic.newsdaily.util;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.andruid.magic.newsdaily.model.AudioNews;

public class MediaUtil {
    public static MediaDescriptionCompat getMediaDescription(AudioNews audioNews){
        Bundle extras = new Bundle();
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                Uri.parse(audioNews.getNews().getImageUrl()));
        extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                Uri.parse(audioNews.getNews().getImageUrl()));
        return new MediaDescriptionCompat.Builder()
                .setMediaId(audioNews.getUri())
                .setIconUri(Uri.parse(audioNews.getNews().getImageUrl()))
                .setTitle(audioNews.getNews().getTitle())
                .setDescription(audioNews.getNews().getDesc())
                .setExtras(extras)
                .build();
    }

    public static MediaMetadataCompat buildMetaData(AudioNews audioNews) {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                        audioNews.getNews().getImageUrl())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audioNews.getNews().getSourceName())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audioNews.getNews().getTitle())
                .build();
    }
}