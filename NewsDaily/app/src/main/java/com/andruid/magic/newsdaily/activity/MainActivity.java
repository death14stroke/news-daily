package com.andruid.magic.newsdaily.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.adapter.NewsAdapter;
import com.andruid.magic.newsdaily.databinding.ActivityMainBinding;
import com.andruid.magic.newsdaily.eventbus.NewsEvent;
import com.andruid.magic.newsdaily.headlines.NewsViewModel;
import com.andruid.magic.newsdaily.headlines.NewsViewModelFactory;
import com.andruid.magic.newsdaily.service.AudioNewsService;
import com.andruid.magic.newsdaily.util.CategoryUtil;
import com.andruid.magic.newsdaily.util.PrefUtil;
import com.andruid.magic.newsloader.model.News;
import com.cleveroad.loopbar.widget.OnItemClickListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import timber.log.Timber;

import static com.andruid.magic.newsdaily.data.Constants.ACTION_OPEN_URL;
import static com.andruid.magic.newsdaily.data.Constants.ACTION_PREPARE_AUDIO;
import static com.andruid.magic.newsdaily.data.Constants.ACTION_SHARE_NEWS;
import static com.andruid.magic.newsdaily.data.Constants.EXTRA_CATEGORY;
import static com.andruid.magic.newsdaily.data.Constants.EXTRA_NEWS_URL;

public class MainActivity extends AppCompatActivity implements OnItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int MY_DATA_CHECK_CODE = 0;
    private String category;
    private ActivityMainBinding binding;
    private List<String> categories;
    private NewsViewModel newsViewModel;
    private NewsAdapter newsAdapter;
    private CardStackLayoutManager cardStackLayoutManager;
    private MediaBrowserCompat mediaBrowserCompat;
    private MediaControllerCompat mediaControllerCompat;
    private MediaControllerCallback mediaControllerCallback;
    private boolean syncWithUI;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        newsAdapter = new NewsAdapter();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        syncWithUI = preferences.getBoolean(getString(R.string.pref_ui_sync), false);
        String country = preferences.getString(getString(R.string.pref_country), PrefUtil.getDefaultCountry(this));
        newsViewModel = new ViewModelProvider(this, new NewsViewModelFactory(country))
                .get(NewsViewModel.class);
        cardStackLayoutManager = new CardStackLayoutManager(this, new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {}
            @Override
            public void onCardSwiped(Direction direction) {}
            @Override
            public void onCardRewound() {}
            @Override
            public void onCardCanceled() {}
            @Override
            public void onCardAppeared(View view, int position) {}
            @Override
            public void onCardDisappeared(View view, int position) {}
        });
        loadCategories();
        setUpCardStackView();
        binding.loopBar.addOnItemClickListener(this);
        binding.speakBtn.setOnClickListener(v -> {
            Intent checkTTSIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        });
//        ActionBar actionBar = getSupportActionBar();
        binding.cardStackView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(MainActivity.this,
                    new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if(binding.loopBar.getVisibility() == View.VISIBLE) {
                        binding.loopBar.setVisibility(View.GONE);
                        /*if (actionBar != null)
                            actionBar.hide();*/
                    }
                    else {
                        binding.loopBar.setVisibility(View.VISIBLE);
                        /*if (actionBar != null)
                            actionBar.show();*/
                    }
                    return true;
                }
            });

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
        MBConnectionCallback mbConnectionCallback = new MBConnectionCallback();
        mediaControllerCallback = new MediaControllerCallback();
        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this,
                AudioNewsService.class), mbConnectionCallback, null);
        mediaBrowserCompat.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Intent intent = new Intent(this, AudioNewsService.class);
                intent.setAction(ACTION_PREPARE_AUDIO);
                intent.putExtra(EXTRA_CATEGORY, category);
                startService(intent);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewsEvent(NewsEvent newsEvent){
        String action = newsEvent.getAction();
        if(ACTION_SHARE_NEWS.equals(action))
            shareNews(newsEvent.getNews());
        else if(ACTION_OPEN_URL.equals(action))
            loadUrl(newsEvent.getNews().getUrl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_search:
                startActivity(new Intent(this, SearchActivity.class));
                break;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.menu_help:
                startActivity(new Intent(this, IntroActivity.class));
                break;
        }
        return true;
    }

    private void loadUrl(String url) {
        Intent intent = new Intent(this, WebViewActivity.class)
                .putExtra(EXTRA_NEWS_URL, url);
        startActivity(intent);
    }

    private void shareNews(News news) {
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, news.getTitle())
                .putExtra(Intent.EXTRA_TEXT, news.getUrl());
        startActivity(Intent.createChooser(intent, "Share news via..."));
    }

    private void setUpCardStackView() {
        SwipeAnimationSetting swipeSetting = new SwipeAnimationSetting.Builder()
                .setDirection(Direction.Bottom)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(Duration.Normal.duration)
                .build();
        cardStackLayoutManager.setSwipeAnimationSetting(swipeSetting);
        cardStackLayoutManager.setCanScrollHorizontal(false);
        cardStackLayoutManager.setDirections(Direction.VERTICAL);
        cardStackLayoutManager.setStackFrom(StackFrom.Bottom);
        binding.cardStackView.setLayoutManager(cardStackLayoutManager);
        binding.cardStackView.setAdapter(newsAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.unbind();
        if(mediaControllerCompat!=null)
            mediaControllerCompat.unregisterCallback(mediaControllerCallback);
        if(mediaBrowserCompat!=null)
            mediaBrowserCompat.disconnect();
    }

    @Override
    public void onItemClicked(int position) {
        category = categories.get(position);
        loadNews(category);
    }

    private void loadNews(String category) {
        newsViewModel.getNewsForCategory(category).observe(this, news -> {
            Timber.d("news submitted: %d", news.size());
            newsAdapter.submitList(news);
        });
    }

    private void loadCategories() {
        categories = CategoryUtil.getCategories();
        category = categories.get(0);
        loadNews(category);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(getString(R.string.pref_country).equals(s)) {
            String country = sharedPreferences.getString(s, PrefUtil.getDefaultCountry(this));
            newsViewModel.setCountry(country);
        }
        else if(getString(R.string.pref_ui_sync).equals(s))
            syncWithUI = sharedPreferences.getBoolean(s, false);
    }

    private void scrollToCurrentNews(String title) {
        List<News> newsList = newsAdapter.getNewsList();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            OptionalInt optionalInt = IntStream.range(0, newsList.size())
                    .filter(pos -> newsList.get(pos).getTitle().equals(title))
                    .findFirst();
            if(optionalInt.isPresent()){
                int pos = optionalInt.getAsInt();
                cardStackLayoutManager.scrollToPosition(pos);
            }
        }
        else{
            com.annimon.stream.OptionalInt optionalInt = com.annimon.stream.IntStream.range(0, newsList.size())
                    .filter(pos -> newsList.get(pos).getTitle().equals(title))
                    .findFirst();
            if(optionalInt.isPresent()){
                int pos = optionalInt.getAsInt();
                cardStackLayoutManager.scrollToPosition(pos);
            }
        }
    }

    private class MBConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mediaControllerCompat = new MediaControllerCompat(MainActivity.this,
                        mediaBrowserCompat.getSessionToken());
                mediaControllerCompat.registerCallback(mediaControllerCallback);
                MediaControllerCompat.setMediaController(MainActivity.this, mediaControllerCompat);
                MediaMetadataCompat metadata = mediaControllerCompat.getMetadata();
                if(metadata != null)
                    mediaControllerCallback.onMetadataChanged(metadata);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            if(syncWithUI)
                scrollToCurrentNews(title);
        }
    }
}