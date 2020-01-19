package com.andruid.magic.newsdaily.eventbus

import com.andruid.magic.newsloader.model.NewsOnline

data class NewsEvent(
    val newsOnline: NewsOnline,
    val action: String
)