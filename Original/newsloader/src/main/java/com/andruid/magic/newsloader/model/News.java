package com.andruid.magic.newsloader.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class News implements Parcelable {
    private String sourceName, title, desc, url, imageUrl;
    private long published;

    public News() {
    }

    public News(String sourceName, String title, String desc, String url, String imageUrl,
                long published) {
        this.sourceName = sourceName;
        this.title = title;
        this.desc = desc;
        this.url = url;
        this.imageUrl = imageUrl;
        this.published = published;
    }

    protected News(Parcel in) {
        sourceName = in.readString();
        title = in.readString();
        desc = in.readString();
        url = in.readString();
        imageUrl = in.readString();
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
        return sourceName.equals(news.sourceName) && title.equals(news.title) &&
                desc.equals(news.desc) && url.equals(news.url) && imageUrl.equals(news.imageUrl)
                && published==news.published;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sourceName);
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeString(url);
        dest.writeString(imageUrl);
        dest.writeLong(published);
    }
}