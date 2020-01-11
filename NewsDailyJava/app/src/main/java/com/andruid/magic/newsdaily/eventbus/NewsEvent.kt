package com.andruid.magic.newsdaily.eventbus

import com.andruid.magic.newsloader.model.News

data class NewsEvent(
        val news : News,
        val action : String
)