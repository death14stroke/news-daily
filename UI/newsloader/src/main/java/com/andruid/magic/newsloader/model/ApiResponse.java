package com.andruid.magic.newsloader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponse {
    @Expose
    @SerializedName("news")
    private final List<News> newsList;
    private final boolean hasMore;

    public ApiResponse(List<News> newsList, boolean hasMore) {
        this.newsList = newsList;
        this.hasMore = hasMore;
    }

    public List<News> getNewsList() {
        return newsList;
    }

    public boolean isHasMore() {
        return hasMore;
    }
}