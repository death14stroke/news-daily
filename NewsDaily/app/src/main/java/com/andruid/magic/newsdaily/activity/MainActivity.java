package com.andruid.magic.newsdaily.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.adapter.NewsAdapter;
import com.andruid.magic.newsdaily.databinding.ActivityMainBinding;
import com.andruid.magic.newsdaily.eventbus.NewsEvent;
import com.andruid.magic.newsdaily.headlines.NewsViewModel;
import com.andruid.magic.newsdaily.headlines.NewsViewModelFactory;
import com.andruid.magic.newsdaily.util.AssetsUtil;
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

import java.io.IOException;
import java.util.List;

import timber.log.Timber;

import static com.andruid.magic.newsdaily.data.Constants.ACTION_OPEN_URL;
import static com.andruid.magic.newsdaily.data.Constants.ACTION_SHARE_NEWS;
import static com.andruid.magic.newsdaily.data.Constants.EXTRA_NEWS_URL;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {
    private ActivityMainBinding binding;
    private List<String> categories;
    private NewsViewModel newsViewModel;
    private NewsAdapter newsAdapter;
    private CardStackLayoutManager cardStackLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        newsAdapter = new NewsAdapter();
        newsViewModel = new ViewModelProvider(this, new NewsViewModelFactory(
                "in")).get(NewsViewModel.class);
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
        if(item.getItemId() == R.id.menu_search)
            startActivity(new Intent(this, SearchActivity.class));
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
    }

    @Override
    public void onItemClicked(int position) {
        String category = categories.get(position);
        loadNews(category);
    }

    private void loadNews(String category) {
        newsViewModel.getNewsForCategory(category).observe(this, news -> {
            Timber.d("news submitted: %d", news.size());
            newsAdapter.submitList(news);
        });
    }

    private void loadCategories() {
        try {
            categories = AssetsUtil.readCategories(getAssets());
            loadNews(categories.get(0));
            Timber.d("categories try %d", categories.size());
        } catch (IOException e) {
            Timber.d("categories catch");
            e.printStackTrace();
        }
    }
}