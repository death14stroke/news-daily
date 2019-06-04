package com.andruid.magic.newsdaily.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.activity.MainActivity;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class NotificationUtil {
    private static final String CHANNEL_ID = "channel_headlines";
    private static final String CHANNEL_NAME = "Read Headlines";

    public static NotificationCompat.Builder buildNotification(Context context, MediaMetadataCompat
            metadataCompat, MediaSessionCompat.Token token){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager==null)
            return null;
        int importance = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            importance = NotificationManager.IMPORTANCE_HIGH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.CYAN);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,CHANNEL_ID);
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(token)
                    .setShowActionsInCompactView(0,1,2)
                    .setShowCancelButton(true))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(PendingIntent.getActivity(context,0,new Intent(context, MainActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .setContentText(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
                .setSubText(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
                .setShowWhen(false);
        String albumArtUri = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        try {
            Bitmap bitmap = Picasso.get().load(albumArtUri).get();
            builder.setLargeIcon(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PendingIntent pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        builder.addAction(android.R.drawable.ic_media_previous,"previous",pendingIntent);

        pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context,PlaybackStateCompat.ACTION_PLAY_PAUSE);
        builder.addAction(android.R.drawable.ic_media_play,"play",pendingIntent);

        pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context,PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        builder.addAction(android.R.drawable.ic_media_next,"next",pendingIntent);

        return builder;
    }
}