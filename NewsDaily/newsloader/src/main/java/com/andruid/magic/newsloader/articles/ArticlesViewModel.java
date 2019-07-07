package com.andruid.magic.newsloader.articles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.andruid.magic.newsloader.model.News;

import static com.andruid.magic.newsloader.data.Constants.PAGE_SIZE;

public class ArticlesViewModel extends ViewModel {
    private LiveData<PagedList<News>> pagedListLiveData;

    ArticlesViewModel(String language, String query) {
        ArticlesDataSourceFactory dataSourceFactory = new ArticlesDataSourceFactory(language, query);
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