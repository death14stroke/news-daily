package com.andruid.magic.newsdaily.fragment;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.andruid.magic.newsdaily.R;
import com.andruid.magic.newsdaily.activity.WebViewActivity;
import com.andruid.magic.newsdaily.adapter.NewsAdapter;
import com.andruid.magic.newsdaily.databinding.FragmentArticlesBinding;
import com.andruid.magic.newsdaily.eventbus.NewsEvent;
import com.andruid.magic.newsdaily.service.AudioNewsService;
import com.andruid.magic.newsloader.articles.ArticlesViewModel;
import com.andruid.magic.newsloader.articles.ArticlesViewModelFactory;
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

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import timber.log.Timber;

import static com.andruid.magic.newsdaily.data.Constants.ACTION_OPEN_URL;
import static com.andruid.magic.newsdaily.data.Constants.ACTION_SHARE_NEWS;
import static com.andruid.magic.newsdaily.data.Constants.INTENT_PREPARE_AUDIO;
import static com.andruid.magic.newsdaily.data.Constants.MY_DATA_CHECK_CODE;
import static com.andruid.magic.newsdaily.data.Constants.NEWS_URL;

public class ArticlesFragment extends Fragment {
    private static final long TIMEOUT = 250;
    private FragmentArticlesBinding binding;
    private ArticlesViewModel articlesViewModel;
    private NewsAdapter newsAdapter;
    private CardStackLayoutManager cardStackLayoutManager;
    private MediaBrowserCompat mediaBrowserCompat;
    private MediaControllerCompat mediaControllerCompat;
    private MediaControllerCallback mediaControllerCallback;

    public static ArticlesFragment newInstance() {
        return new ArticlesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        String language = "en";
        articlesViewModel = ViewModelProviders.of(this,
                new ArticlesViewModelFactory(language)).get(ArticlesViewModel.class);
        Timber.d("fragment created articles: %s", language);
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
        MBConnectionCallback mbConnectionCallback = new MBConnectionCallback();
        mediaControllerCallback = new MediaControllerCallback();
        mediaBrowserCompat = new MediaBrowserCompat(getContext(), new ComponentName(
                Objects.requireNonNull(getContext()), AudioNewsService.class),
                mbConnectionCallback, null);
        mediaBrowserCompat.connect();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_articles, container, false);
        Observable.create((ObservableOnSubscribe<String>) emitter ->
                binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        emitter.onNext(query);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        emitter.onNext(newText);
                        return false;
                    }
        })).map(text -> text.toLowerCase().trim())
                .debounce(TIMEOUT, TimeUnit.MILLISECONDS)
                .distinct()
                .filter(text -> !text.isEmpty())
                .subscribe(this::loadArticles);
        setUpCardStackView();
        binding.speakBtn.setOnClickListener(v -> {
            Intent checkTTSIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        });
        return binding.getRoot();
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
    public void onDestroy() {
        super.onDestroy();
        if(mediaControllerCompat!=null)
            mediaControllerCompat.unregisterCallback(mediaControllerCallback);
        if(mediaBrowserCompat!=null)
            mediaBrowserCompat.disconnect();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Intent intent = new Intent(getActivity(), AudioNewsService.class);
                intent.setAction(INTENT_PREPARE_AUDIO);
                Objects.requireNonNull(getContext()).startService(intent);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
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

    private void loadArticles(String query) {
        articlesViewModel.loadArticles(query).observe(this, pagedList -> {
            newsAdapter.submitList(pagedList);
            binding.cardStackView.setAdapter(newsAdapter);
        });
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

    private class MBConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mediaControllerCompat = new MediaControllerCompat(getContext(),
                        mediaBrowserCompat.getSessionToken());
                mediaControllerCompat.registerCallback(mediaControllerCallback);
                MediaControllerCompat.setMediaController(Objects.requireNonNull(getActivity()),
                        mediaControllerCompat);
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