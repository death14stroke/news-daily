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

public class NewsLoader {
    private RetrofitService service;

    public NewsLoader(Context context){
        service = RetrofitClient.getRetrofitInstance(context).create(RetrofitService.class);
    }

    public void loadHeadlines(String country, String category, final int page, int pageSize,
                              final NewsLoadedListener mListener){
        service.getHeadlines(country, category, page, pageSize).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if(response.body()==null)
                    return;
                Timber.tag("pagelog").d("page: %d, size: %d", page, response.body().getNewsList().size());
                List<News> newsList = response.body().getNewsList();
                if(newsList!=null)
                    mListener.onSuccess(newsList, response.body().isHasMore());
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                mListener.onFailure(t);
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
                Timber.tag("pagelog").d("page: %d, size: %d", page, response.body().getNewsList().size());
                List<News> newsList = response.body().getNewsList();
                if(newsList!=null)
                    mListener.onSuccess(newsList, response.body().isHasMore());
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                mListener.onFailure(t);
            }
        });
    }

    public interface NewsLoadedListener{
        void onSuccess(List<News> newsList, boolean hasMore);
        void onFailure(Throwable t);
    }
}