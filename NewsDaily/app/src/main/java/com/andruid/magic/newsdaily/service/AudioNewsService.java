package com.andruid.magic.newsdaily.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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
import com.andruid.magic.newsdaily.util.Extensions;
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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.andruid.magic.newsdaily.data.Constants.ACTION_PREPARE_AUDIO;
import static com.andruid.magic.newsdaily.data.Constants.EXTRA_CATEGORY;
import static com.andruid.magic.newsloader.data.Constants.FIRST_PAGE;
import static com.andruid.magic.newsloader.data.Constants.PAGE_SIZE;

public class AudioNewsService extends MediaBrowserServiceCompat implements Player.EventListener,
        NewsRepository.NewsLoadedListener, TtsApi.AudioConversionListener {
    private static final String MEDIA_SERVICE = "AudioNewsService";
    public static final int NEWS_FETCH_DISTANCE = 3, MEDIA_NOTI_ID = 1;
    private static final int MSG_INIT_NEWS = 0, MSG_STOP_SERVICE = 1, MSG_UPDATE_SOURCE = 2, MSG_SHOW_NOTI = 3;
    private static final int WAIT_QUEUE_TIMEOUT_MS = 5000;
    private MediaSessionCompat mediaSessionCompat;
    private MediaSessionCallback mediaSessionCallback;
    private SimpleExoPlayer exoPlayer;
    private MediaSessionConnector mediaSessionConnector;
    private List<AudioNews> audioNewsList = new ArrayList<>();
    private DataSource.Factory dataSourceFactory;
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
    private Handler audioNewsHandler;
    private HandlerThread audioNewsHandlerThread;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("service created");
        audioNewsHandlerThread = new HandlerThread("HandlerThread");
        audioNewsHandlerThread.start();
        audioNewsHandler = new Handler(audioNewsHandlerThread.getLooper(), new AudioNewsHandler());
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
                return Extensions.getMediaDescription(audioNewsList.get(windowIndex));
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
        mediaSessionCompat.setMetadata(Extensions.buildMetaData(audioNews));
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
        if(ACTION_PREPARE_AUDIO.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if(extras != null)
                category = extras.getString(EXTRA_CATEGORY);
            if(TtsApi.getInstance().isReady())
                audioNewsHandler.sendEmptyMessage(MSG_INIT_NEWS);
            else
                Toast.makeText(getApplicationContext(), "Text to speech not available", Toast.LENGTH_SHORT).show();
            registerReceiver(mNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }
        else
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        audioNewsHandler.sendEmptyMessage(MSG_STOP_SERVICE);
    }

    @Override
    public void onDestroy() {
        mediaSessionCompat.release();
        mediaSessionConnector.setPlayer(null, null);
        try {
            unregisterReceiver(mNoisyReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        exoPlayer.release();
        audioNewsHandlerThread.quit();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if(playbackState == Player.STATE_READY) {
            if (mediaButtonIntent != null) {
                MediaButtonReceiver.handleIntent(mediaSessionCompat, mediaButtonIntent);
                mediaButtonIntent = null;
            }
            int icon = android.R.drawable.ic_media_play;
            if (playWhenReady)
                icon = android.R.drawable.ic_media_pause;
            MediaMetadataCompat metadataCompat = mediaSessionCompat.getController().getMetadata();
            if (metadataCompat == null || category == null)
                return;
            Timber.d("sending message noti show in state change");
            Message message = audioNewsHandler.obtainMessage(MSG_SHOW_NOTI, icon, playWhenReady ? 1 : 0, category);
            audioNewsHandler.sendMessage(message);
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
            MediaMetadataCompat mediaMetadataCompat = Extensions.buildMetaData(audioNews);
            mediaSessionCompat.setMetadata(mediaMetadataCompat);
            Timber.d("sending message noti show in pos discontinuity");
            Message message = audioNewsHandler.obtainMessage(MSG_SHOW_NOTI, android.R.drawable.ic_media_pause,
                    1, category);
            audioNewsHandler.sendMessage(message);
        }
    }

    @Override
    public void onSuccess(List<News> newsList, boolean hasMore) {
        int pos = audioNewsList.size();
        for(int i=0; i<newsList.size(); i++){
            News news = newsList.get(i);
            String text = news.getDesc();
            audioNewsList.add(new AudioNews(news.getUrl(), news));
            if(text.isEmpty())
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
        Timber.d("Utterance done %s", utteranceId);
        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(file));
        concatenatingMediaSource.addMediaSource(mediaSource);
        audioNewsHandler.sendEmptyMessageDelayed(MSG_UPDATE_SOURCE, WAIT_QUEUE_TIMEOUT_MS);
        if(utteranceId.equals("0")) {
            audioNewsHandler.sendEmptyMessage(MSG_UPDATE_SOURCE);
            mediaSessionCallback.onPlay();
        }
    }

    @Override
    public void onFailure(@NotNull String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            super.onPlay();
            if(audioNewsList == null)
                return;
            exoPlayer.setPlayWhenReady(true);
            int pos = exoPlayer.getCurrentWindowIndex();
            setCurrentAudio(pos);
            mediaSessionCompat.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING, pos);
        }

        @Override
        public void onPause() {
            super.onPause();
            exoPlayer.setPlayWhenReady(false);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED, exoPlayer.getCurrentWindowIndex());
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            if(exoPlayer.hasNext())
                exoPlayer.next();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            if(exoPlayer.hasPrevious())
                exoPlayer.previous();
        }
    }

    private class AudioNewsHandler implements Handler.Callback {

        @Override
        public boolean handleMessage(@NonNull Message message) {
            Timber.d("handle message: %d", message.what);
            switch (message.what){
                case MSG_INIT_NEWS:
                    audioNewsHandler.removeCallbacksAndMessages(null);
                    audioNewsList.clear();
                    concatenatingMediaSource.clear();
                    page = FIRST_PAGE;
                    loadNews();
                    break;
                case MSG_STOP_SERVICE:
                    audioNewsHandler.removeCallbacksAndMessages(null);
                    stopForeground(true);
                    stopSelf();
                    break;
                case MSG_UPDATE_SOURCE:
                    audioNewsHandler.removeMessages(MSG_UPDATE_SOURCE);
                    exoPlayer.prepare(concatenatingMediaSource, false, false);
                    break;
                case MSG_SHOW_NOTI:
                    int icon = message.arg1;
                    String category = (String) message.obj;
                    boolean playWhenReady = (message.arg2 == 1);
                    MediaMetadataCompat metadataCompat = mediaSessionCompat.getController().getMetadata();
                    NotificationCompat.Builder notificationBuilder = NotificationUtil.buildNotification(AudioNewsService.this,
                            icon, category, metadataCompat, mediaSessionCompat.getSessionToken());
                    if(playWhenReady)
                        startForeground(MEDIA_NOTI_ID, notificationBuilder.build());
                    else {
                        NotificationManagerCompat.from(AudioNewsService.this).notify(MEDIA_NOTI_ID,
                                notificationBuilder.build());
                        stopForeground(false);
                    }
                    break;
            }
            return false;
        }
    }
}