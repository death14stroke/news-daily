package com.andruid.magic.newsloader.api;

import android.content.Context;

import com.andruid.magic.newsloader.model.ApiResponse;
import com.andruid.magic.newsloader.model.News;
import com.andruid.magic.newsloader.server.RetrofitClient;
import com.andruid.magic.newsloader.server.RetrofitService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class NewsRepository {
    private static final Object LOCK = new Object();
    private static NewsRepository sInstance;
    private static RetrofitService service;

    public static void init(Context context){
        service = RetrofitClient.getInstance(context).create(RetrofitService.class);
    }

    public static NewsRepository getInstance() {
        if(sInstance == null){
            synchronized (LOCK) {
                Timber.i("Created news repository instance");
                sInstance = new NewsRepository();
            }
        }
        return sInstance;
    }

    public void loadHeadlines(String country, String category, final int page, int pageSize,
                              final NewsLoadedListener mListener){
        service.getHeadlines(country, category, page, pageSize).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if(response.body()==null)
                    return;
                Timber.d("headlines page: %d, size: %d", page,
                        response.body().getNewsList().size());
                List<News> newsList = response.body().getNewsList();
                if(newsList!=null)
                    mListener.onSuccess(newsList, response.body().isHasMore());
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Timber.e(t,"News loading failed");
            }
        });
    }

    public void loadArticles(String language, String query, final int page, int pageSize,
                             final NewsLoadedListener mListener){
        service.getArticles(language, query, page, pageSize).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if(response.body()==null)
                    return;
                Timber.i("articles page: %d, size: %d", page,
                        response.body().getNewsList().size());
                List<News> newsList = response.body().getNewsList();
                if(newsList!=null)
                    mListener.onSuccess(newsList, response.body().isHasMore());
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Timber.e(t,"News loading failed");
            }
        });
    }

    public interface NewsLoadedListener {
        void onSuccess(List<News> newsList, boolean hasMore);
    }
}