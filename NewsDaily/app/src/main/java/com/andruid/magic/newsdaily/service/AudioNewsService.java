package com.andruid.magic.newsdaily.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;
import androidx.preference.PreferenceManager;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.model.AudioNews;
import com.andruid.magic.newsdaily.util.MediaUtil;
import com.andruid.magic.newsdaily.util.NotificationUtil;
import com.andruid.magic.newsdaily.util.PrefUtil;
import com.andruid.magic.newsloader.api.NewsRepository;
import com.andruid.magic.newsloader.model.News;
import com.andruid.magic.texttoaudiofile.api.TtsApi;
import com.andruid.magic.texttoaudiofile.util.FileUtils;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

import static com.andruid.magic.newsdaily.data.Constants.ACTION_PREPARE_AUDIO;
import static com.andruid.magic.newsdaily.data.Constants.EXTRA_CATEGORY;
import static com.andruid.magic.newsloader.data.Constants.FIRST_PAGE;
import static com.andruid.magic.newsloader.data.Constants.PAGE_SIZE;

public class AudioNewsService extends MediaBrowserServiceCompat implements Player.EventListener,
        NewsRepository.NewsLoadedListener, TtsApi.AudioConversionListener {
    private static final String MEDIA_SERVICE = "AudioNewsService";
    public static final int NEWS_FETCH_DISTANCE = 3;
    public static final int MEDIA_NOTI_ID = 1;
    private MediaSessionCompat mediaSessionCompat;
    private MediaSessionCallback mediaSessionCallback;
    private SimpleExoPlayer exoPlayer;
    private MediaSessionConnector mediaSessionConnector;
    private List<AudioNews> audioNewsList = new ArrayList<>();
    private DataSource.Factory dataSourceFactory;
    private NotificationCompat.Builder notificationBuilder;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private Intent mediaButtonIntent;
    private int page = FIRST_PAGE;
    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaSessionCallback.onPause();
        }
    };
    private String category;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("service created");
        initMediaSession();
        initExoPlayer();
    }

    private void initExoPlayer() {
        final TrackSelector trackSelector = new DefaultTrackSelector();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                Util.getUserAgent(this, getString(R.string.app_name)));
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build();
        exoPlayer.setAudioAttributes(audioAttributes,true);
        exoPlayer.addListener(this);
        concatenatingMediaSource = new ConcatenatingMediaSource();
    }

    private void initMediaSession() {
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                mediaButtonIntent, 0);
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(),
                MediaButtonReceiver.class);
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), MEDIA_SERVICE,
                mediaButtonReceiver, pendingIntent);
        setSessionToken(mediaSessionCompat.getSessionToken());
        mediaSessionCallback = new MediaSessionCallback();
        mediaSessionCompat.setCallback(mediaSessionCallback);
        mediaSessionCompat.setActive(true);
        mediaSessionConnector = new MediaSessionConnector(mediaSessionCompat);
        mediaSessionConnector.setQueueNavigator(new TimelineQueueNavigator(mediaSessionCompat) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                return MediaUtil.getMediaDescription(audioNewsList.get(windowIndex));
            }
        });
        mediaSessionConnector.setPlayer(exoPlayer,null);
    }

    private void loadNews(){
        String country = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.pref_country), PrefUtil.getDefaultCountry(this));
        NewsRepository.getInstance().loadHeadlines(country, category, page, PAGE_SIZE, this);
    }

    private void setCurrentAudio(int pos) {
        if(pos >= audioNewsList.size() || pos < 0)
            return;
        AudioNews audioNews = audioNewsList.get(pos);
        mediaSessionCompat.setMetadata(MediaUtil.buildMetaData(audioNews));
    }

    private void setMediaPlaybackState(int state, int pos) {
        PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder();
        playbackStateBuilder.setActiveQueueItemId(pos);
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS || state == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_STOP);
        else
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_STOP);
        if (state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_PLAYING) {
            try {
                playbackStateBuilder.setState(state, exoPlayer.getCurrentPosition(), 0);
            }
            catch (IllegalStateException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        } else
            playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mediaSessionCompat.setPlaybackState(playbackStateBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.tag("medialog").d("on start command");
        if(ACTION_PREPARE_AUDIO.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if(extras != null)
                category = extras.getString(EXTRA_CATEGORY);
            if(TtsApi.getInstance().isReady()) {
                audioNewsList.clear();
                concatenatingMediaSource.clear();
                loadNews();
            }
            else
                Toast.makeText(getApplicationContext(), "Text to speech not available", Toast.LENGTH_SHORT).show();
        }
        else
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        mediaSessionCallback.onStop();
    }

    @Override
    public void onDestroy() {
        mediaSessionCompat.release();
        mediaSessionConnector.setPlayer(null, null);
        exoPlayer.release();
        exoPlayer = null;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if(playbackState==Player.STATE_READY){
            if(mediaButtonIntent!=null){
                MediaButtonReceiver.handleIntent(mediaSessionCompat,mediaButtonIntent);
                mediaButtonIntent = null;
            }
        }
        int icon = android.R.drawable.ic_media_play;
        if(playWhenReady)
            icon = android.R.drawable.ic_media_pause;
        MediaMetadataCompat metadataCompat = mediaSessionCompat.getController().getMetadata();
        if(metadataCompat == null || category == null)
            return;
        notificationBuilder = NotificationUtil.buildNotification(this, icon, category,
                metadataCompat, mediaSessionCompat.getSessionToken());
        Notification notification = Objects.requireNonNull(notificationBuilder).build();
        if(playWhenReady)
            startForeground(MEDIA_NOTI_ID, notification);
        else {
            NotificationManagerCompat.from(this).notify(MEDIA_NOTI_ID, notification);
            stopForeground(false);
        }
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        if(reason==Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
                reason==Player.DISCONTINUITY_REASON_PERIOD_TRANSITION || reason==Player.DISCONTINUITY_REASON_INTERNAL){
            int pos = exoPlayer.getCurrentWindowIndex();
            if(pos == audioNewsList.size() - NEWS_FETCH_DISTANCE) {
                page++;
                loadNews();
            }
            AudioNews audioNews = audioNewsList.get(pos);
            MediaMetadataCompat mediaMetadataCompat = MediaUtil.buildMetaData(audioNews);
            mediaSessionCompat.setMetadata(mediaMetadataCompat);
            new Thread(() -> {
                notificationBuilder = NotificationUtil.buildNotification(this,
                        android.R.drawable.ic_media_pause, category, mediaMetadataCompat,
                        mediaSessionCompat.getSessionToken());
                Notification notification = Objects.requireNonNull(notificationBuilder).build();
                startForeground(MEDIA_NOTI_ID, notification);
            }).start();
        }
    }

    @Override
    public void onSuccess(List<News> newsList, boolean hasMore) {
        int pos = audioNewsList.size();
        for(int i=0; i<newsList.size(); i++){
            News news = newsList.get(i);
            String text = news.getDesc();
            audioNewsList.add(new AudioNews("", news));
            if(text == null || text.isEmpty())
                text = "No description available";
            TtsApi.getInstance().convertToAudioFile(text, String.valueOf(pos+i), this);
        }
    }

    @Override
    public void onFailure(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result) {
        result.sendResult(new ArrayList<>());
    }

    @Override
    public void onAudioCreated(File file) {
        String utteranceId = FileUtils.getUtteranceId(file.getName());
        Timber.tag("ttslog").d("Utterance done %s", utteranceId);
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(file));
        concatenatingMediaSource.addMediaSource(mediaSource);
        exoPlayer.prepare(concatenatingMediaSource, false, false);
        if(utteranceId.equals("0"))
            mediaSessionCallback.onPlay();
    }

    @Override
    public void onFailure(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            Timber.tag("medialog").d("play");
            super.onPlay();
            if(audioNewsList == null)
                return;
            exoPlayer.setPlayWhenReady(true);
            registerReceiver(mNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            int pos = exoPlayer.getCurrentWindowIndex();
            setCurrentAudio(pos);
            mediaSessionCompat.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING, pos);
        }

        @Override
        public void onPause() {
            Timber.tag("medialog").d("pause");
            super.onPause();
            exoPlayer.setPlayWhenReady(false);
            unregisterReceiver(mNoisyReceiver);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED, exoPlayer.getCurrentWindowIndex());
        }

        @Override
        public void onStop() {
            super.onStop();
            stopForeground(true);
            stopSelf();
        }

        @Override
        public void onSkipToNext() {
            Timber.tag("medialog").d("next");
            super.onSkipToNext();
            if(exoPlayer.hasNext())
                exoPlayer.next();
        }

        @Override
        public void onSkipToPrevious() {
            Timber.tag("medialog").d("prev");
            super.onSkipToPrevious();
            if(exoPlayer.hasPrevious())
                exoPlayer.previous();
        }
    }
}