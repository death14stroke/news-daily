package com.andruid.magic.newsloader.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class News implements Parcelable {
    private final String sourceName, title, desc, url, imageUrl;
    private final long published;

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

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getPublished() {
        return published;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(sourceName);
        parcel.writeString(title);
        parcel.writeString(desc);
        parcel.writeString(url);
        parcel.writeString(imageUrl);
        parcel.writeLong(published);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        News news = (News) o;
        return getPublished() == news.getPublished() &&
                Objects.equals(getSourceName(), news.getSourceName()) &&
                Objects.equals(getTitle(), news.getTitle()) &&
                Objects.equals(getDesc(), news.getDesc()) &&
                Objects.equals(getUrl(), news.getUrl()) &&
                Objects.equals(getImageUrl(), news.getImageUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSourceName(), getTitle(), getDesc(), getUrl(), getImageUrl(), getPublished());
    }

    public static class Builder {
        private String sourceName, title, desc, url, imageUrl;
        private long published;

        public Builder setSourceName(String sourceName) {
            this.sourceName = sourceName;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDesc(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder setPublished(long published) {
            this.published = published;
            return this;
        }

        public News build() {
            return new News(sourceName, title, desc, url, imageUrl, published);
        }
    }
}