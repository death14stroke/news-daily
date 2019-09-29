package com.andruid.magic.newsloader.articles;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ArticlesViewModelFactory implements ViewModelProvider.Factory {
    private String language;

    public ArticlesViewModelFactory(String language) {
        this.language = language;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ArticlesViewModel(language);
    }
}