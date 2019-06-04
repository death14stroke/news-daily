package com.andruid.magic.newsdaily.paging;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import com.andruid.magic.newsloader.model.News;

public class NewsDataSourceFactory extends DataSource.Factory<Integer, News> {
    private MutableLiveData<PageKeyedDataSource<Integer, News>> liveDataSource =
            new MutableLiveData<>();
    private String country;

    public NewsDataSourceFactory(String country){
        this.country = country;
    }

    @NonNull
    @Override
    public DataSource<Integer, News> create() {
        NewsDataSource dataSource = new NewsDataSource(country);
        liveDataSource.postValue(dataSource);
        return dataSource;
    }

    MutableLiveData<PageKeyedDataSource<Integer, News>> getLiveDataSource() {
        return liveDataSource;
    }
}