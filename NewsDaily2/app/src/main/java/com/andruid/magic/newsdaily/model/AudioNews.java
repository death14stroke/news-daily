package com.andruid.magic.newsdaily.model;

import com.andruid.magic.newsloader.model.News;

public class AudioNews {
    private final String uri;
    private final News news;

    public AudioNews(String uri, News news) {
        this.uri = uri;
        this.news = news;
    }

    public String getUri() {
        return uri;
    }

    public News getNews() {
        return news;
    }
}