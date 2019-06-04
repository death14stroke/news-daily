package com.andruid.magic.newsloader.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class News implements Parcelable {
    private String sourceName, author, title, desc, url, imageUrl, content;
    private long published;

    public News() {
    }

    public News(String sourceName, String author, String title, String desc, String url,
                String imageUrl, String content, long published) {
        this.sourceName = sourceName;
        this.author = author;
        this.title = title;
        this.desc = desc;
        this.url = url;
        this.imageUrl = imageUrl;
        this.content = content;
        this.published = published;
    }

    protected News(Parcel in) {
        sourceName = in.readString();
        author = in.readString();
        title = in.readString();
        desc = in.readString();
        url = in.readString();
        imageUrl = in.readString();
        content = in.readString();
        published = in.readLong();
    }

    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getPublished() {
        return published;
    }

    public void setPublished(long published) {
        this.published = published;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null || obj.getClass() != News.class)
            return false;
        News news = (News)obj;
        return sourceName.equals(news.sourceName) && author.equals(news.author) &&
                title.equals(news.title) && desc.equals(news.desc) && url.equals(news.url) &&
                imageUrl.equals(news.imageUrl) && content.equals(news.content) && published==news.published;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sourceName);
        dest.writeString(author);
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeString(url);
        dest.writeString(imageUrl);
        dest.writeString(content);
        dest.writeLong(published);
    }
}