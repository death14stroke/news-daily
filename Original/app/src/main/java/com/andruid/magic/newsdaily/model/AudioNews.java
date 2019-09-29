package com.andruid.magic.newsdaily.model;

import com.andruid.magic.newsloader.model.News;

public class AudioNews {
    private String uri;
    private News news;

    public AudioNews() {
    }

    public AudioNews(String uri, News news) {
        this.uri = uri;
        this.news = news;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }
}