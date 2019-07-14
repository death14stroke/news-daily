package com.andruid.magic.newsdaily.headlines;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.andruid.magic.newsloader.model.News;

import static com.andruid.magic.newsdaily.data.Constants.PAGE_SIZE;

public class NewsViewModel extends ViewModel {
    private LiveData<PagedList<News>> pagedListLiveData;

    NewsViewModel(String category, String country) {
        NewsDataSourceFactory dataSourceFactory = new NewsDataSourceFactory(country, category);
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