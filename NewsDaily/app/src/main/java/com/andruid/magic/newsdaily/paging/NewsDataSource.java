package com.andruid.magic.newsdaily.paging;

import androidx.annotation.NonNull;
import androidx.paging.PageKeyedDataSource;

import com.andruid.magic.newsloader.api.NewsLoader;
import com.andruid.magic.newsloader.model.News;

import java.util.List;

import timber.log.Timber;

public class NewsDataSource extends PageKeyedDataSource<Integer, News> {
    static final int PAGE_SIZE = 10;
    private static final int FIRST_PAGE = 1;
    private String country;
    private NewsLoader newsLoader;

    public NewsDataSource(String country) {
        Timber.tag("dslog").d("datasource created");
        newsLoader = new NewsLoader();
        this.country = country;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, News> callback) {
        Timber.tag("pagelog").d("load initial");
        newsLoader.loadHeadlines(country, FIRST_PAGE, PAGE_SIZE, new NewsLoader.NewsLoadedListener() {
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
        newsLoader.loadHeadlines(country, params.key, PAGE_SIZE, new NewsLoader.NewsLoadedListener() {
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
        Timber.tag("pagelog").d("load page %d", params.key);
        newsLoader.loadHeadlines(country, params.key, PAGE_SIZE, new NewsLoader.NewsLoadedListener() {
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