package com.andruid.magic.newsdaily.ui.viewmodel

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.andruid.magic.newsdaily.paging.ArticlesPagingSource
import kotlinx.coroutines.cancel

class SearchViewModel : ViewModel() {
    private val config = PagingConfig(10)
    private val queryLiveData = MutableLiveData<String>("")

    val articles = Transformations.switchMap(queryLiveData) { query ->
        Pager(config) {
            ArticlesPagingSource(query)
        }.flow.cachedIn(viewModelScope)
            .asLiveData()
    }

    fun setQuery(query: String) = queryLiveData.postValue(query)

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}