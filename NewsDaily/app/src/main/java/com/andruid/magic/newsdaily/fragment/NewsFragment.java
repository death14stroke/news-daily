package com.andruid.magic.newsdaily.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.activity.WebViewActivity;
import com.andruid.magic.newsdaily.adapter.NewsAdapter;
import com.andruid.magic.newsdaily.databinding.FragmentNewsBinding;
import com.andruid.magic.newsdaily.eventbus.NewsEvent;
import com.andruid.magic.newsloader.model.News;
import com.andruid.magic.newsloader.paging.NewsViewModel;
import com.andruid.magic.newsloader.paging.NewsViewModelFactory;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import timber.log.Timber;

import static com.andruid.magic.newsdaily.data.Constants.ACTION_OPEN_URL;
import static com.andruid.magic.newsdaily.data.Constants.ACTION_SHARE_NEWS;
import static com.andruid.magic.newsdaily.data.Constants.KEY_CATEGORY;
import static com.andruid.magic.newsdaily.data.Constants.NEWS_URL;

public class NewsFragment extends Fragment {
    private FragmentNewsBinding binding;
    private String category;
    private NewsViewModel newsViewModel;
    private NewsAdapter newsAdapter;
    private CardStackLayoutManager cardStackLayoutManager;

    public static NewsFragment newInstance(String category) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(KEY_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            category = getArguments().getString(KEY_CATEGORY);
        newsViewModel = ViewModelProviders.of(this, new NewsViewModelFactory(
                Objects.requireNonNull(getActivity()).getApplication(), category))
                .get(NewsViewModel.class);
        Timber.tag("assetslog").d("fragment created %s", category);
        newsAdapter = new NewsAdapter();
        cardStackLayoutManager = new CardStackLayoutManager(getContext(), new CardStackListener() {
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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_news, container, false);
        setUpCardStackView();
        loadHeadlines();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
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
    }

    private void loadHeadlines() {
        newsViewModel.getPagedListLiveData().observe(this, pagedList ->
                newsAdapter.submitList(pagedList)
        );
        binding.cardStackView.setAdapter(newsAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewsEvent(NewsEvent newsEvent){
        String action = newsEvent.getAction();
        News news = newsEvent.getNews();
        if(ACTION_SHARE_NEWS.equals(action))
            shareNews(news);
        else if(ACTION_OPEN_URL.equals(action))
            loadUrl(news.getUrl());
    }

    private void loadUrl(String url) {
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        intent.putExtra(NEWS_URL, url);
        startActivity(intent);
    }

    private void shareNews(News news) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, news.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, news.getUrl());
        startActivity(Intent.createChooser(intent, "Share news via..."));
    }
}