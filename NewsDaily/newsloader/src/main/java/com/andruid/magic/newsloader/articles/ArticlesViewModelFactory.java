package com.andruid.magic.newsloader.articles;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ArticlesViewModelFactory implements ViewModelProvider.Factory {
    private Application application;
    private String language, query;

    public ArticlesViewModelFactory(Application application, String language, String query) {
        this.application = application;
        this.language = language;
        this.query = query;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ArticlesViewModel(application, language, query);
    }
}