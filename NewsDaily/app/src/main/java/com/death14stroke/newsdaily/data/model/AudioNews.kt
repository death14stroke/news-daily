package com.death14stroke.newsdaily.data.model

import com.death14stroke.newsloader.data.model.News

/**
 * Data model for news item in the audio playlist
 * @property uri uri of the text to speech output
 * @property news metadata of the news object
 * @see News
 */
data class AudioNews(
    val uri: String,
    val news: News
)