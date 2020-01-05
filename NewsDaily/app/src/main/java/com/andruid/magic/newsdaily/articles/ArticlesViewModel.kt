package com.andruid.magic.newsdaily.articles

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.andruid.magic.newsloader.data.Constants
import com.andruid.magic.newsloader.model.News

class ArticlesViewModel(private val language : String) : ViewModel() {
    private val queryLiveData: MutableLiveData<String> = MutableLiveData()
    private var pagedListLiveData : LiveData<PagedList<News>>

    init {
        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(Constants.PAGE_SIZE)
                .build()
        pagedListLiveData = Transformations.switchMap(queryLiveData) { query: String? ->
            LivePagedListBuilder(ArticlesDataSourceFactory(language,
                    query ?: ""), pagedListConfig).build()
        }
    }

    fun setQuery(query : String) { queryLiveData.postValue(query) }
}