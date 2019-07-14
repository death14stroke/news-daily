package com.andruid.magic.newsdaily.headlines;

import androidx.annotation.NonNull;
import androidx.paging.PageKeyedDataSource;

import com.andruid.magic.newsloader.model.News;
import com.andruid.magic.newsloader.repo.NewsRepository;

import java.util.List;

import timber.log.Timber;

import static android.nfc.tech.MifareUltralight.PAGE_SIZE;

public class NewsDataSource extends PageKeyedDataSource<Integer, News> {
    private static final int FIRST_PAGE = 1;
    private String country, category;

    NewsDataSource(String country, String category) {
        this.category = category;
        this.country = country;
        Timber.d("data source created");
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, News> callback) {
        Timber.d("load initial");
        NewsRepository.getInstance().loadHeadlines(country, category, FIRST_PAGE, PAGE_SIZE, new NewsRepository.NewsLoadedListener() {
            @Override
            public void onSuccess(List<News> newsList, boolean hasMore) {
                callback.onResult(newsList, null, FIRST_PAGE + 1);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void loadBefore(@NonNull final LoadParams<Integer> params, @NonNull final LoadCallback<Integer, News> callback) {
        NewsRepository.getInstance().loadHeadlines(country, category, params.key, PAGE_SIZE, new NewsRepository.NewsLoadedListener() {
            @Override
            public void onSuccess(List<News> newsList, boolean hasMore) {
                Integer adjacentKey = (params.key > FIRST_PAGE) ? params.key - 1 : null;
                if(newsList!=null)
                    callback.onResult(newsList, adjacentKey);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void loadAfter(@NonNull final LoadParams<Integer> params, @NonNull final LoadCallback<Integer, News> callback) {
        Timber.d("load page %d", params.key);
        NewsRepository.getInstance().loadHeadlines(country, category, params.key, PAGE_SIZE, new NewsRepository.NewsLoadedListener() {
            @Override
            public void onSuccess(List<News> newsList, boolean hasMore) {
                Integer key = hasMore ? params.key + 1 : null;
                callback.onResult(newsList, key);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }
}