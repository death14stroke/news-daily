package com.andruid.magic.newsdaily.ui.viewmodel

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.andruid.magic.newsdaily.paging.ArticlesDataSource
import com.andruid.magic.newsdaily.paging.BaseDataSourceFactory
import com.andruid.magic.newsloader.data.Constants
import com.andruid.magic.newsloader.model.News

class ArticlesViewModel : ViewModel() {
    private val queryLiveData: MutableLiveData<String> = MutableLiveData()

    var pagedListLiveData : LiveData<PagedList<News>>

    init {
        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(Constants.PAGE_SIZE)
            .build()
        pagedListLiveData = Transformations.switchMap(queryLiveData) { query: String? ->
            LivePagedListBuilder(BaseDataSourceFactory { ArticlesDataSource(viewModelScope,
                "en",query ?: "") }, pagedListConfig).build()
        }
    }

    fun setQuery(query : String) = queryLiveData.postValue(query)
}