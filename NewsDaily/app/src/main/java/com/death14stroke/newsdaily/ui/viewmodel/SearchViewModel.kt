package com.death14stroke.newsdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.death14stroke.newsdaily.data.PAGE_SIZE
import com.death14stroke.newsdaily.data.repository.MainRepository
import com.death14stroke.newsdaily.paging.ArticlesPagingSource
import com.death14stroke.newsdaily.data.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class SearchViewModel(private val repository: MainRepository) : ViewModel() {
    private val config = PagingConfig(PAGE_SIZE)
    val queryFlow = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val articlesFlow = queryFlow.flatMapLatest { query ->
        Pager(config) { ArticlesPagingSource(repository, query) }
            .flow.cachedIn(viewModelScope)
            .map { news -> Result.Success(news) }
            .flowOn(Dispatchers.IO)
    }.stateIn(viewModelScope, SharingStarted.Lazily, Result.Loading)
}