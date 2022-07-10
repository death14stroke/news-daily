package com.death14stroke.newsdaily.di

import com.death14stroke.newsdaily.ui.viewmodel.CountriesViewModel
import com.death14stroke.newsdaily.ui.viewmodel.NewsViewModel
import com.death14stroke.newsdaily.ui.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { parameters -> NewsViewModel(get(), parameters.get()) }
    viewModel { CountriesViewModel(get()) }
    viewModel { SearchViewModel(get()) }
}