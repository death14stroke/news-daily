package com.andruid.magic.newsloader.model;

import android.os.Parcel;
import android.os.Parcelable;

public class News implements Parcelable {
    private final String sourceName, title, desc, url, imageUrl;
    private final long published;

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
}