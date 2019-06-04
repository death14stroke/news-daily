package com.andruid.magic.newsloader.paging;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.andruid.magic.newsloader.model.News;

import static com.andruid.magic.newsloader.data.Constants.PAGE_SIZE;

public class NewsViewModel extends ViewModel {
    private LiveData<PagedList<News>> pagedListLiveData;

    public NewsViewModel() {
        NewsDataSourceFactory dataSourceFactory = new NewsDataSourceFactory("in");
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