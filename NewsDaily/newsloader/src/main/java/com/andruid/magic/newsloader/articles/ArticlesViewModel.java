package com.andruid.magic.newsloader.articles;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.andruid.magic.newsloader.model.News;

import static com.andruid.magic.newsloader.data.Constants.PAGE_SIZE;

public class ArticlesViewModel extends AndroidViewModel {
    private LiveData<PagedList<News>> pagedListLiveData;

    ArticlesViewModel(Application application, String language, String query) {
        super(application);
        ArticlesDataSourceFactory dataSourceFactory = new ArticlesDataSourceFactory(application
                .getApplicationContext(), language, query);
        PagedList.Config pagedListConfig = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(PAGE_SIZE)
                .build();
        pagedListLiveData = new LivePagedListBuilder<>(dataSourceFactory, pagedListConfig).build();
    }

    public LiveData<PagedList<News>> getPagedListLiveData() {
        return pagedListLiveData;
    }
}