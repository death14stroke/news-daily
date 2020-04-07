package com.andruid.magic.newsdaily.ui.viewmodel

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.andruid.magic.newsdaily.database.entity.News
import com.andruid.magic.newsdaily.paging.ArticlesDataSource
import com.andruid.magic.newsdaily.paging.BaseDataSourceFactory
import com.andruid.magic.newsloader.data.Constants
import kotlinx.coroutines.cancel

class ArticlesViewModel : ViewModel() {
    private val queryLiveData: MutableLiveData<String> = MutableLiveData()

    val searchLiveData: LiveData<PagedList<News>>
    var pos = 0

    init {
        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(Constants.PAGE_SIZE)
            .build()
        searchLiveData = Transformations.switchMap(queryLiveData) { query: String? ->
            LivePagedListBuilder(BaseDataSourceFactory {
                ArticlesDataSource(
                    viewModelScope,
                    "en", query ?: ""
                )
            }, pagedListConfig).build()
        }
    }

    fun setQuery(query: String) = queryLiveData.postValue(query)

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}