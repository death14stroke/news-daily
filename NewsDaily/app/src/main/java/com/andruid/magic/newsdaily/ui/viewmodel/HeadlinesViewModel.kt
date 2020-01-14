package com.andruid.magic.newsdaily.ui.viewmodel

import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.cancel

class HeadlinesViewModel(category: String, application: Application) : AndroidViewModel(application) {
    val newsLiveData: LiveData<PagedList<News>>
    var pos = 0

    init {
        val config = PagedList.Config.Builder()
            .setPageSize(Constants.PAGE_SIZE)
            .setEnablePlaceholders(false)
            .build()

        val country = PreferenceManager.getDefaultSharedPreferences(application)
            .getString(application.getString(R.string.pref_country), application.getString(R.string.default_country))

        newsLiveData = LivePagedListBuilder(BaseDataSourceFactory {
            NewsDataSource(viewModelScope, country!!, category)
        }, config)
            .build()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("newslog", "onCleared: viewmodel destroyed")
        viewModelScope.cancel()
    }
}