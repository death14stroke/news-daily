package com.andruid.magic.newsloader.paging;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.andruid.magic.newsloader.model.News;

import static com.andruid.magic.newsloader.data.Constants.PAGE_SIZE;

public class NewsViewModel extends AndroidViewModel {
    private LiveData<PagedList<News>> pagedListLiveData;

    NewsViewModel(Application application, String category, String country) {
        super(application);
        NewsDataSourceFactory dataSourceFactory = new NewsDataSourceFactory(application
                .getApplicationContext(), country, category);
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