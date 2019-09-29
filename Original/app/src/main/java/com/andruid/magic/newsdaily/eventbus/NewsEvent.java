package com.andruid.magic.newsdaily.eventbus;

import com.andruid.magic.newsloader.model.News;

public class NewsEvent {
    private News news;
    private String action;

    public NewsEvent(News news, String action) {
        this.news = news;
        this.action = action;
    }

    public News getNews() {
        return news;
    }

    public String getAction() {
        return action;
    }
}