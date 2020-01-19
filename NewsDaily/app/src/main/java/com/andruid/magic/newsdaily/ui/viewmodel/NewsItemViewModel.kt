package com.andruid.magic.newsdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.andruid.magic.newsdaily.data.Constants
import com.andruid.magic.newsdaily.eventbus.NewsEvent
import com.andruid.magic.newsloader.model.NewsOnline
import org.greenrobot.eventbus.EventBus

class NewsItemViewModel(val newsOnline: NewsOnline) : ViewModel() {
    fun shareNews() = EventBus.getDefault().post(NewsEvent(newsOnline, Constants.ACTION_SHARE_NEWS))

    fun openUrl() = EventBus.getDefault().post(NewsEvent(newsOnline, Constants.ACTION_OPEN_URL))
}