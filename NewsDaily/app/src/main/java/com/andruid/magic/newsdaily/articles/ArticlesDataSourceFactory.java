package com.andruid.magic.newsdaily.articles;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import com.andruid.magic.newsloader.model.News;

public class ArticlesDataSourceFactory extends DataSource.Factory<Integer, News> {
    private MutableLiveData<PageKeyedDataSource<Integer, News>> liveDataSource =
            new MutableLiveData<>();
    private String language, query;


    ArticlesDataSourceFactory(String language, String query){
        this.language = language;
        this.query = query;
    }

    @NonNull
    @Override
    public DataSource<Integer, News> create() {
        ArticlesDataSource dataSource = new ArticlesDataSource(language, query);
        liveDataSource.postValue(dataSource);
        return dataSource;
    }
}