package com.andruid.magic.newsdaily.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.preference.PreferenceManager
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.paging.BaseDataSourceFactory
import com.andruid.magic.newsdaily.paging.NewsDataSource
import com.andruid.magic.newsloader.data.Constants
import com.andruid.magic.newsloader.model.News

class NewsViewModel(category: String, application: Application) : AndroidViewModel(application) {
    private val newsLiveData: LiveData<PagedList<News>>

    init {
        val config = PagedList.Config.Builder()
            .setPageSize(Constants.PAGE_SIZE)
            .setEnablePlaceholders(false)
            .build()

        val country = PreferenceManager.getDefaultSharedPreferences(application)
            .getString(
                application.getString(R.string.pref_country),
                application.getString(R.string.default_country)
            )

        newsLiveData = LivePagedListBuilder(BaseDataSourceFactory {
            NewsDataSource(viewModelScope, country!!, category)
        }, config)
            .build()
    }

    fun getNews() = newsLiveData
}