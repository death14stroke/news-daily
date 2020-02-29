package com.andruid.magic.newsdaily.eventbus

import com.andruid.magic.newsdaily.database.entity.News

data class NewsEvent(
    val news: News,
    val action: String
)