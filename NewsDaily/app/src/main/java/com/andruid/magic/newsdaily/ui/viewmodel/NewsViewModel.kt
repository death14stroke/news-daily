package com.andruid.magic.newsdaily.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.andruid.magic.newsdaily.application.NewsApplication
import com.andruid.magic.newsdaily.data.PAGE_SIZE
import com.andruid.magic.newsdaily.database.entity.NewsItem
import com.andruid.magic.newsdaily.database.repository.DbRepository
import com.andruid.magic.newsdaily.util.getSelectedCountry
import com.andruid.magic.newsloader.data.model.Result
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class NewsViewModel(application: Application, private val category: String) :
    AndroidViewModel(application) {
    private val selectedCountry =
        getApplication<NewsApplication>().applicationContext.getSelectedCountry()
    private val countryLiveData = MutableLiveData(selectedCountry)

    val newsLiveData: LiveData<Result<PagingData<NewsItem>>>

    init {
        newsLiveData = Transformations.switchMap(countryLiveData) { country ->
            flow<Result<PagingData<NewsItem>>> {
                emit(Result.Loading(""))

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
    }

    fun updateCountry(country: String) {
        countryLiveData.postValue(country)
    }
}