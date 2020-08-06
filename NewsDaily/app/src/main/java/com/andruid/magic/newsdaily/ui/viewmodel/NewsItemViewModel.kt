package com.andruid.magic.newsdaily.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.andruid.magic.newsdaily.database.entity.NewsItem

class NewsItemViewModel(val news: NewsItem) : ViewModel() {
    fun shareNews() {
        Log.d("log", "share")
    } //EventBus.getDefault().post(NewsEvent(news, Constants.ACTION_SHARE_NEWS))

    fun openUrl() {
        Log.d("log", "open")
    } //EventBus.getDefault().post(NewsEvent(news, Constants.ACTION_OPEN_URL))
}
