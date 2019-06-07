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
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.model.AudioNews;
import com.andruid.magic.newsdaily.util.MediaUtil;
import com.andruid.magic.newsdaily.util.NotificationUtil;
import com.andruid.magic.newsloader.api.NewsLoader;
import com.andruid.magic.newsloader.model.News;
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
import java.util.Locale;
import java.util.Objects;

import timber.log.Timber;

import static com.andruid.magic.newsdaily.data.Constants.DEFAULT_COUNTRY;
import static com.andruid.magic.newsdaily.data.Constants.DIR_TTS;
import static com.andruid.magic.newsdaily.data.Constants.INTENT_PREPARE_AUDIO;
import static com.andruid.magic.newsdaily.data.Constants.MEDIA_NOTI_ID;
import static com.andruid.magic.newsdaily.data.Constants.MEDIA_SERVICE;
import static com.andruid.magic.newsdaily.data.Constants.NEWS_FETCH_DISTANCE;
import static com.andruid.magic.newsloader.data.Constants.FIRST_PAGE;
import static com.andruid.magic.newsloader.data.Constants.PAGE_SIZE;

public class AudioNewsService extends MediaBrowserServiceCompat implements Player.EventListener, TextToSpeech.OnInitListener, NewsLoader.NewsLoadedListener {
    private MediaSessionCompat mediaSessionCompat;
    private MediaSessionCallback mediaSessionCallback;
    private SimpleExoPlayer exoPlayer;
    private MediaSessionConnector mediaSessionConnector;
    private List<AudioNews> audioNewsList = new ArrayList<>();
    private DataSource.Factory dataSourceFactory;
    private NotificationCompat.Builder notificationBuilder;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private TextToSpeech tts;
    private File dir;
    private NewsLoader newsLoader = new NewsLoader();
    private Intent mediaButtonIntent;
    private int page = FIRST_PAGE;
    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaSessionCallback.onPause();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.tag("dlog").d("service created");
        initMediaSession();
        initExoPlayer();
    }

    private void initTTS() {
        tts = new TextToSpeech(this, this);
        dir = new File(getCacheDir(), DIR_TTS);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {
                File file = new File(dir, MediaUtil.getFileName(utteranceId));
                Timber.tag("ttslog").d("Utterance done %s", utteranceId);
                MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.fromFile(file));
                concatenatingMediaSource.addMediaSource(mediaSource);
                exoPlayer.prepare(concatenatingMediaSource, false, false);
                if(utteranceId.equals("0"))
                    mediaSessionCallback.onPlay();
            }

            @Override
            public void onError(String utteranceId) {
                Timber.tag("ttslog").d("error in %s", utteranceId);
            }
        });
    }

    private void initExoPlayer() {
        final TrackSelector trackSelector = new DefaultTrackSelector();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this,trackSelector);
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
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
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
        TelephonyManager telephoneManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = telephoneManager.getNetworkCountryIso();
        Toast.makeText(getApplicationContext(), "country: "+countryCode, Toast.LENGTH_SHORT).show();
        newsLoader.loadHeadlines(DEFAULT_COUNTRY, page, PAGE_SIZE, this);
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
        if(INTENT_PREPARE_AUDIO.equals(intent.getAction()))
            initTTS();
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
        if(metadataCompat==null)
            return;
        notificationBuilder = NotificationUtil.buildNotification(this, icon,
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
                        android.R.drawable.ic_media_pause, mediaMetadataCompat,
                        mediaSessionCompat.getSessionToken());
                Notification notification = Objects.requireNonNull(notificationBuilder).build();
                startForeground(MEDIA_NOTI_ID, notification);
            }).start();
        }
    }

    @Override
    public void onSuccess(List<News> newsList, boolean hasMore) {
        int pos = audioNewsList.size();
        if(!dir.exists()) {
            boolean res = dir.mkdir();
            Timber.tag("ttslog").d("dir created %s", res);
        }
        for(int i=0; i<newsList.size(); i++){
            News news = newsList.get(i);
            audioNewsList.add(new AudioNews("", news));
            File file = new File(dir, MediaUtil.getFileName(String.valueOf(pos+i)));
            tts.synthesizeToFile(news.getDesc(), null, file, String.valueOf(pos+i));
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
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(Locale.getDefault());
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                Toast.makeText(this, "Text to speech not available", Toast.LENGTH_SHORT).show();
            else {
                audioNewsList.clear();
                loadNews();
            }
        }
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