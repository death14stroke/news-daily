package com.andruid.magic.newsdaily.application;

import android.app.Application;

import com.andruid.magic.newsdaily.BuildConfig;
import com.andruid.magic.newsloader.api.NewsRepository;
import com.blongho.country_data.World;

import timber.log.Timber;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        World.init(this);
        NewsRepository.init(this);
    }
}