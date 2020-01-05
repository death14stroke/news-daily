package com.andruid.magic.newsdaily.headlines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.andruid.magic.newsloader.data.Constants
import com.andruid.magic.newsloader.model.News

class NewsViewModel(country : String) : ViewModel() {
    var country : String = country
        set(value) {
            countryLiveData.postValue(value)
        }

    private var countryLiveData : MutableLiveData<String> = MutableLiveData(country)
    private var categoryLiveData : MutableLiveData<String> = MutableLiveData()

    var pagedListLiveData : LiveData<PagedList<News>>

    init {
        countryLiveData.postValue(country)

        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .build()
        pagedListLiveData = Transformations.switchMap(categoryLiveData) { category: String? ->
            LivePagedListBuilder(NewsDataSourceFactory(countryLiveData.value ?: "in",
                    category ?: "general"), pagedListConfig).build()
        }
    }

    fun setCategory(category: String) { categoryLiveData.postValue(category) }
}