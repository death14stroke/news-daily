package com.death14stroke.newsdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.death14stroke.newsdaily.data.PAGE_SIZE
import com.death14stroke.newsdaily.data.model.Result
import com.death14stroke.newsdaily.data.repository.MainRepository
import com.death14stroke.newsdaily.paging.NewsPagingSource
import com.death14stroke.newsloader.data.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class NewsViewModel(private val repository: MainRepository, category: Category) : ViewModel() {
    private val config = PagingConfig(PAGE_SIZE)
    val countryFlow = MutableStateFlow(repository.getSelectedCountry())

    @OptIn(ExperimentalCoroutinesApi::class)
    val newsFlow = countryFlow.flatMapLatest { country ->
        Pager(config) { NewsPagingSource(repository, country, category) }
            .flow
            .cachedIn(viewModelScope)
            .map { news -> Result.Success(news) }
            .flowOn(Dispatchers.IO)
    }.stateIn(viewModelScope, SharingStarted.Lazily, Result.Loading)
}