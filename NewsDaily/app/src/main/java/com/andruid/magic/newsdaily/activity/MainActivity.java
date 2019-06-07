package com.andruid.magic.newsdaily.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.adapter.NewsAdapter;
import com.andruid.magic.newsdaily.databinding.ActivityMainBinding;
import com.andruid.magic.newsdaily.service.AudioNewsService;
import com.andruid.magic.newsdaily.viewholder.NewsViewHolder;
import com.andruid.magic.newsloader.model.News;
import com.andruid.magic.newsloader.paging.NewsViewModel;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.RewindAnimationSetting;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import timber.log.Timber;

import static com.andruid.magic.newsdaily.data.Constants.INTENT_NOTI_CLICK;
import static com.andruid.magic.newsdaily.data.Constants.INTENT_PREPARE_AUDIO;
import static com.andruid.magic.newsdaily.data.Constants.MY_DATA_CHECK_CODE;
import static com.andruid.magic.newsdaily.data.Constants.NEWS_TITLE;
import static com.andruid.magic.newsdaily.data.Constants.NEWS_URL;

public class MainActivity extends AppCompatActivity implements NewsViewHolder.CardControlsListener {
    private ActivityMainBinding binding;
    private NewsViewModel newsViewModel;
    private NewsAdapter newsAdapter;
    private CardStackLayoutManager cardStackLayoutManager;
    private MediaBrowserCompat mediaBrowserCompat;
    private MBConnectionCallback mbConnectionCallback;
    private MediaControllerCompat mediaControllerCompat;
    private MediaControllerCallback mediaControllerCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        newsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        newsAdapter = new NewsAdapter(this);
        mbConnectionCallback = new MBConnectionCallback();
        mediaControllerCallback = new MediaControllerCallback();
        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this,
                AudioNewsService.class), mbConnectionCallback, null);
        mediaBrowserCompat.connect();
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
        setUpCardStackView();
        loadHeadlines();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(INTENT_NOTI_CLICK.equals(getIntent().getAction())){
            Bundle extras = getIntent().getExtras();
            if(extras != null){
                String title = extras.getString(NEWS_TITLE);
                scrollToCurrentNews(title);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_speak) {
            Timber.tag("dlog").d("selected menu speak");
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaControllerCompat!=null)
            mediaControllerCompat.unregisterCallback(mediaControllerCallback);
        if(mediaBrowserCompat!=null)
            mediaBrowserCompat.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Intent intent = new Intent(this, AudioNewsService.class);
                intent.setAction(INTENT_PREPARE_AUDIO);
                startService(intent);
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    private void setUpCardStackView() {
        RewindAnimationSetting rewindSetting = new RewindAnimationSetting.Builder()
                .setDirection(Direction.Bottom)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(Duration.Normal.duration)
                .build();
        cardStackLayoutManager.setRewindAnimationSetting(rewindSetting);
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
    }

    private void loadHeadlines() {
        newsViewModel.getPagedListLiveData().observe(this, pagedList ->
                newsAdapter.submitList(pagedList)
        );
        binding.cardStackView.setAdapter(newsAdapter);
    }

    @Override
    public void onLoadUrl(String url) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(NEWS_URL, url);
        startActivity(intent);
    }

    @Override
    public void onShareNews(News news) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, news.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, news.getUrl());
        startActivity(Intent.createChooser(intent, "Share news via..."));
    }

    private class MBConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mediaControllerCompat = new MediaControllerCompat(MainActivity.this,
                        mediaBrowserCompat.getSessionToken());
                mediaControllerCompat.registerCallback(mediaControllerCallback);
                MediaControllerCompat.setMediaController(MainActivity.this,
                        mediaControllerCompat);
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
            scrollToCurrentNews(title);
        }
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
}