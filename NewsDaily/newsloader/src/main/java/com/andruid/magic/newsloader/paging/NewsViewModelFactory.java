package com.andruid.magic.newsloader.paging;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class NewsViewModelFactory implements ViewModelProvider.Factory {
    private String category;

    public NewsViewModelFactory(String category) {
        this.category = category;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NewsViewModel(category);
    }
}
