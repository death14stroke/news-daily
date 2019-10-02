package com.andruid.magic.newsdaily.articles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.andruid.magic.newsloader.model.News;

import static com.andruid.magic.newsloader.data.Constants.PAGE_SIZE;

public class ArticlesViewModel extends ViewModel {
    private final String language;

    ArticlesViewModel(String language) {
        this.language = language;
    }


    public LiveData<PagedList<News>> loadArticles(String query) {
        ArticlesDataSourceFactory dataSourceFactory = new ArticlesDataSourceFactory(language, query);
        PagedList.Config pagedListConfig = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(PAGE_SIZE)
                .build();
        return new LivePagedListBuilder<>(dataSourceFactory, pagedListConfig).build();
    }
}