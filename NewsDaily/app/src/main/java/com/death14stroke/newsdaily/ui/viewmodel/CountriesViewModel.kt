package com.death14stroke.newsdaily.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.death14stroke.newsdaily.data.repository.MainRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class CountriesViewModel(private val repository: MainRepository) : ViewModel() {
    val countriesFlow = flow {
        emit(repository.getCountries())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getSelectedCountry(): String {
        return repository.getSelectedCountry()
    }
}