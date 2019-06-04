package com.andruid.magic.newsdaily.activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
import com.andruid.magic.newsdaily.paging.NewsViewModel;
import com.andruid.magic.newsdaily.service.TtsService;
import com.andruid.magic.newsdaily.viewholder.NewsViewHolder;
import com.andruid.magic.newsloader.model.News;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.RewindAnimationSetting;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

public class MainActivity extends AppCompatActivity implements NewsViewHolder.CardControlsListener {
    protected static final String NEWS_URL = "news_url";
    private int MY_DATA_CHECK_CODE = 0;
    private ActivityMainBinding binding;
    private NewsViewModel newsViewModel;
    private NewsAdapter newsAdapter;
    private CardStackLayoutManager cardStackLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        newsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        newsAdapter = new NewsAdapter(this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_speak) {
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                startService(new Intent(this, TtsService.class));
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
}