package com.andruid.magic.newsdaily.headlines;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.andruid.magic.newsloader.model.News;

import static com.andruid.magic.newsloader.data.Constants.PAGE_SIZE;

public class NewsViewModel extends ViewModel {
    private MutableLiveData<String> countryLiveData, categoryLiveData;
    private LiveData<PagedList<News>> pagedListLiveData;

    NewsViewModel(String country) {
        countryLiveData = new MutableLiveData<>(country);
        categoryLiveData = new MutableLiveData<>();
        PagedList.Config pagedListConfig = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(PAGE_SIZE)
                .build();
        pagedListLiveData = Transformations.switchMap(categoryLiveData, category ->
            new LivePagedListBuilder<>(new NewsDataSourceFactory(countryLiveData.getValue(),
                    categoryLiveData.getValue()), pagedListConfig).build());
    }

    public void setCountry(String country){
        countryLiveData.postValue(country);
    }

    public LiveData<PagedList<News>> getNewsForCategory(String category) {
        categoryLiveData.postValue(category);
        return pagedListLiveData;
    }
}