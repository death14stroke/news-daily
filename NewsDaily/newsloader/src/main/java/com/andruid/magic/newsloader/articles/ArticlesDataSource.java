package com.andruid.magic.newsloader.articles;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.paging.PageKeyedDataSource;

import com.andruid.magic.newsloader.api.NewsLoader;
import com.andruid.magic.newsloader.model.News;

import java.util.List;

import timber.log.Timber;

import static com.andruid.magic.newsloader.data.Constants.FIRST_PAGE;
import static com.andruid.magic.newsloader.data.Constants.PAGE_SIZE;

public class ArticlesDataSource extends PageKeyedDataSource<Integer, News> {
    private String language, query;
    private NewsLoader newsLoader;

    ArticlesDataSource(Context context, String language, String query){
        Timber.tag("dslog").d("datasource created");
        this.language = language;
        this.query = query;
        newsLoader = new NewsLoader(context);
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, News> callback) {
        Timber.tag("pagelog").d("load initial");
        newsLoader.loadArticles(language, query, FIRST_PAGE, PAGE_SIZE, new NewsLoader.NewsLoadedListener() {
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
        newsLoader.loadArticles(language, query, params.key, PAGE_SIZE, new NewsLoader.NewsLoadedListener() {
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
        if(language != null)
            newsLoader.loadArticles(language, query, params.key, PAGE_SIZE, new NewsLoader.NewsLoadedListener() {
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