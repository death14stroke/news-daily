package com.andruid.magic.newsloader.headlines;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class NewsViewModelFactory implements ViewModelProvider.Factory {
    private Application application;
    private String category, country;

    public NewsViewModelFactory(Application application, String category, String country) {
        this.application = application;
        this.category = category;
        this.country = country;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NewsViewModel(application, category, country);
    }
}