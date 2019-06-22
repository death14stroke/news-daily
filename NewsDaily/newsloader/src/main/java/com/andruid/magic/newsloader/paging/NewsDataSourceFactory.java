package com.andruid.magic.newsloader.paging;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import com.andruid.magic.newsloader.model.News;

public class NewsDataSourceFactory extends DataSource.Factory<Integer, News> {
    private MutableLiveData<PageKeyedDataSource<Integer, News>> liveDataSource =
            new MutableLiveData<>();
    private String country, category;
    private Context context;

    NewsDataSourceFactory(Context context, String country, String category){
        this.country = country;
        this.category = category;
        this.context = context;
    }

    @NonNull
    @Override
    public DataSource<Integer, News> create() {
        NewsDataSource dataSource = new NewsDataSource(context, country, category);
        liveDataSource.postValue(dataSource);
        return dataSource;
    }
}