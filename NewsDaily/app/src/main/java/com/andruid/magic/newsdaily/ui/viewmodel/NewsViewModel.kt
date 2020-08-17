package com.andruid.magic.newsdaily.ui.viewmodel

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.andruid.magic.newsdaily.data.PAGE_SIZE
import com.andruid.magic.newsdaily.database.repository.DbRepository
import com.andruid.magic.newsloader.data.model.Result
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class NewsViewModel(selectedCountry: String, private val category: String) : ViewModel() {
    private val countryLiveData = MutableLiveData(selectedCountry)
    val newsLiveData = countryLiveData.switchMap { country ->
        flow {
            emit(Result.Loading)

            val config = PagingConfig(PAGE_SIZE)
            val pager = Pager(config) {
                DbRepository.getNews(country, category)
            }

            pager.flow
                .cachedIn(viewModelScope)
                .collect { news ->
                    emit(Result.Success(news))
                }
        }.asLiveData()
    }

    fun updateCountry(country: String) {
        countryLiveData.postValue(country)
    }
}