package com.andruid.magic.newsdaily.articles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.andruid.magic.newsloader.model.News;

import static com.andruid.magic.newsloader.data.Constants.PAGE_SIZE;

public class ArticlesViewModel extends ViewModel {
    private MutableLiveData<String> queryLiveData;
    private LiveData<PagedList<News>> pagedListLiveData;

    ArticlesViewModel(String language) {
        queryLiveData = new MutableLiveData<>();
        PagedList.Config pagedListConfig = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(PAGE_SIZE)
                .build();
        pagedListLiveData = Transformations.switchMap(queryLiveData, query ->
                new LivePagedListBuilder<>(new ArticlesDataSourceFactory(language,
                        queryLiveData.getValue()), pagedListConfig).build());
    }


    public LiveData<PagedList<News>> loadArticles(String query) {
        queryLiveData.postValue(query);
        return pagedListLiveData;
    }
}