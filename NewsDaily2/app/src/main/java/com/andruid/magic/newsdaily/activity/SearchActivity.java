package com.andruid.magic.newsdaily.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.adapter.NewsAdapter;
import com.andruid.magic.newsdaily.articles.ArticlesViewModel;
import com.andruid.magic.newsdaily.articles.ArticlesViewModelFactory;
import com.andruid.magic.newsdaily.databinding.ActivitySearchBinding;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import timber.log.Timber;

import static com.andruid.magic.newsdaily.data.Constants.KEY_POSITION;
import static com.andruid.magic.newsdaily.data.Constants.KEY_SEARCH;

public class SearchActivity extends AppCompatActivity {
    private ActivitySearchBinding binding;
    private ArticlesViewModel articlesViewModel;
    private NewsAdapter newsAdapter;
    private CardStackLayoutManager cardStackLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        newsAdapter = new NewsAdapter();
        articlesViewModel = ViewModelProviders.of(this,
                new ArticlesViewModelFactory("en")).get(ArticlesViewModel.class);
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
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        setUpCardStackView();
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String query = extras.getString(KEY_SEARCH);
            loadArticles(query, savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, cardStackLayoutManager.getTopPosition());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.unbind();
    }

    private void loadArticles(String query, Bundle savedInstanceState) {
        articlesViewModel.loadArticles(query).observe(this, pagedList -> {
            Timber.d("articles search loaded");
            newsAdapter.submitList(pagedList);
            if(savedInstanceState != null){
                int pos = savedInstanceState.getInt(KEY_POSITION);
                cardStackLayoutManager.scrollToPosition(pos);
            }
        });
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
}