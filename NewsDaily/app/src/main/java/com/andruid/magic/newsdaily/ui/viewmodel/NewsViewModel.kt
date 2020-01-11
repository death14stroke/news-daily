package com.andruid.magic.newsdaily.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.andruid.magic.newsdaily.paging.NewsDataSourceFactory
import com.andruid.magic.newsloader.data.Constants
import com.andruid.magic.newsloader.model.News

class NewsViewModel(category: String) : ViewModel() {
    private val newsLiveData: LiveData<PagedList<News>>

    init {
        val config = PagedList.Config.Builder()
            .setPageSize(Constants.PAGE_SIZE)
            .setEnablePlaceholders(false)
            .build()
        newsLiveData = LivePagedListBuilder(NewsDataSourceFactory(viewModelScope,
                "in", category), config)
            .build()
    }

    fun getNews() = newsLiveData
}