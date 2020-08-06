package com.andruid.magic.newsdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.andruid.magic.newsdaily.data.Constants
import com.andruid.magic.newsdaily.database.entity.News
import com.andruid.magic.newsdaily.eventbus.NewsEvent
import org.greenrobot.eventbus.EventBus

class NewsItemViewModel(val news: News) : ViewModel() {
    fun shareNews() = EventBus.getDefault().post(NewsEvent(news, Constants.ACTION_SHARE_NEWS))

    fun openUrl() = EventBus.getDefault().post(NewsEvent(news, Constants.ACTION_OPEN_URL))
}