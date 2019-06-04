package com.andruid.magic.newsdaily.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.util.NotificationUtil;
import com.andruid.magic.newsloader.api.NewsLoader;
import com.andruid.magic.newsloader.model.News;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
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

public class TtsService extends Service implements NewsLoader.NewsLoadedListener, TextToSpeech.OnInitListener {
    private static final int PAGE_SIZE = 10;
    private static final int FIRST_PAGE = 1;
    private static final String DIR_TTS = "ttsAudio";
    private static final String MEDIA_SERVICE_TAG = "media_service";
    private static final int MEDIA_NOTI_ID = 1;
    private IBinder iBinder = new ServiceBinder();
    private NewsLoader newsLoader = new NewsLoader();
    private String country = "in";
    private TextToSpeech tts;
    private List<News> newsList = new ArrayList<>();
    private ExoPlayer exoPlayer;
    private DataSource.Factory dataSourceFactory;
    private MediaSessionCompat mediaSessionCompat;
    private NotificationCompat.Builder notificationBuilder;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

    };

    public TtsService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(this, this);
        initExoPlayer();
        initMediaSession();
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                File dir = new File(getCacheDir(), DIR_TTS);
                File file = new File(dir, "news_"+utteranceId+".wav");
                Timber.tag("ttslog").d("Utterance done %s", utteranceId);
                MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.fromFile(file));
                concatenatingMediaSource.addMediaSource(mediaSource);
                exoPlayer.prepare(concatenatingMediaSource, false, false);
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
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                if(reason==Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
                        reason==Player.DISCONTINUITY_REASON_PERIOD_TRANSITION || reason==Player.DISCONTINUITY_REASON_INTERNAL){
                    News news = newsList.get(exoPlayer.getCurrentWindowIndex());
                    MediaMetadataCompat mediaMetadataCompat = new MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, news.getImageUrl())
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, news.getSourceName())
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, news.getAuthor())
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, news.getTitle())
                            .build();
                    new Thread(() -> {
                        notificationBuilder = NotificationUtil.buildNotification(TtsService.this,
                                mediaMetadataCompat, mediaSessionCompat.getSessionToken());
                        Notification notification = Objects.requireNonNull(notificationBuilder).build();
                        startForeground(MEDIA_NOTI_ID,notification);
                    }).start();
                }
            }
        });
        concatenatingMediaSource = new ConcatenatingMediaSource();
        exoPlayer.prepare(concatenatingMediaSource);
        exoPlayer.setPlayWhenReady(true);
    }

    private void initMediaSession() {
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), MEDIA_SERVICE_TAG, mediaButtonReceiver, pendingIntent);
        mediaSessionCompat.setCallback(mediaSessionCallback);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
    }


    private void loadNews(int page){
        newsLoader.loadHeadlines(country, page, PAGE_SIZE, this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onSuccess(List<News> news, boolean hasMore) {
        int pos = newsList.size();
        this.newsList.addAll(news);
        File dir = new File(getCacheDir(), DIR_TTS);
        if(!dir.exists()) {
            boolean res = dir.mkdir();
            Timber.tag("ttslog").d("dir created %s", res);
        }
        for(int i=pos; i<newsList.size(); i++){
            News n = newsList.get(i);
            //tts.speak(n.getDesc(), TextToSpeech.QUEUE_ADD, null, String.valueOf(i));
            File file = new File(dir, "news_"+i+".wav");
            tts.synthesizeToFile(n.getDesc(), null, file, String.valueOf(i));
        }
    }

    @Override
    public void onFailure(Throwable t) {

    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(Locale.getDefault());
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                Toast.makeText(this, "Text to speech not available", Toast.LENGTH_SHORT).show();
            else {
                newsList.clear();
                loadNews(FIRST_PAGE);
            }
        }
    }

    public class ServiceBinder extends Binder{
        public TtsService getService(){
            return TtsService.this;
        }
    }
}