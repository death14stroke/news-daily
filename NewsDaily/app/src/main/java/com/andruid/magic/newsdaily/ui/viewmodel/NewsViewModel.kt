package com.andruid.magic.newsdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.database.repository.DbRepository

class NewsViewModel(private val category: String) : ViewModel() {
    private val pager = initPager()
    val news = pager.flow
        .cachedIn(viewModelScope)
        .asLiveData()

    private fun initPager(): Pager<Int, NewsItem> {
        val config = PagingConfig(10)
        return Pager(config) {
            DbRepository.getNews(category)
        }
    }
}