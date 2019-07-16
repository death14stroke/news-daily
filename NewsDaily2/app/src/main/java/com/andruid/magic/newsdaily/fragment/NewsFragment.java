package com.andruid.magic.newsdaily.fragment;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import com.andruid.magic.newsdaily.activity.WebViewActivity;
import com.andruid.magic.newsdaily.adapter.NewsAdapter;
import com.andruid.magic.newsdaily.eventbus.NewsEvent;
import com.andruid.magic.newsdaily.headlines.NewsViewModel;
import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.databinding.NewsFragmentBinding;
import com.andruid.magic.newsdaily.headlines.NewsViewModelFactory;
import com.andruid.magic.newsdaily.util.PrefUtil;
import com.andruid.magic.newsloader.model.News;
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

import static com.andruid.magic.newsdaily.data.Constants.ACTION_OPEN_URL;
import static com.andruid.magic.newsdaily.data.Constants.ACTION_SHARE_NEWS;
import static com.andruid.magic.newsdaily.data.Constants.KEY_URL;

public class NewsFragment extends Fragment {
    private static final String KEY_POSITION = "position", KEY_CATEGORY = "category";
    private static final String DEF_CATEGORY = "general";
    private NewsFragmentBinding binding;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.news_fragment, container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpCardStackView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, cardStackLayoutManager.getTopPosition());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.unbind();
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String category;
        if(getArguments() != null)
            category = getArguments().getString(KEY_CATEGORY);
        else
            category = DEF_CATEGORY;
        String country = PreferenceManager.getDefaultSharedPreferences(Objects
                .requireNonNull(getContext())).getString(getString(R.string.pref_country),
                PrefUtil.getDefaultCountry(getContext()));
        newsViewModel = ViewModelProviders.of(this,
                new NewsViewModelFactory(category, country)).get(NewsViewModel.class);
        loadHeadlines(savedInstanceState);
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
        intent.putExtra(KEY_URL, url);
        startActivity(intent);
    }

    private void shareNews(News news) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, news.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, news.getUrl());
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

    private void loadHeadlines(Bundle savedInstanceState) {
        newsViewModel.getPagedListLiveData().observe(this, pagedList -> {
            newsAdapter.submitList(pagedList);
            if(savedInstanceState != null){
                int pos = savedInstanceState.getInt(KEY_POSITION);
                cardStackLayoutManager.scrollToPosition(pos);
            }
        });
    }
}