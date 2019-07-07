package com.andruid.magic.newsloader.headlines;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class NewsViewModelFactory implements ViewModelProvider.Factory {
    private String category, country;

    public NewsViewModelFactory(String category, String country) {
        this.category = category;
        this.country = country;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NewsViewModel(category, country);
    }
}