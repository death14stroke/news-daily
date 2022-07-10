package com.death14stroke.newsdaily.di

import com.death14stroke.newsdaily.data.countries.CountryHelper
import com.death14stroke.newsdaily.data.countries.CountryHelperImpl
import com.death14stroke.newsdaily.data.preferences.PreferenceHelper
import com.death14stroke.newsdaily.data.preferences.PreferenceHelperImpl
import com.death14stroke.newsdaily.data.repository.MainRepository
import com.death14stroke.newsloader.api.NetworkNewsHelper
import com.death14stroke.newsloader.api.NetworkNewsHelperImpl
import com.death14stroke.newsloader.util.provideApiService
import com.death14stroke.texttoaudiofile.api.TtsHelper
import com.death14stroke.texttoaudiofile.api.TtsHelperImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repoModule = module {
    single { provideApiService(androidContext()) }
    single<NetworkNewsHelper> { NetworkNewsHelperImpl(get()) }
    single<PreferenceHelper> { PreferenceHelperImpl(androidContext()) }
    single<CountryHelper> { CountryHelperImpl(androidContext()) }
    single<TtsHelper> { TtsHelperImpl(androidContext()) }
    single { MainRepository(get(), get(), get()) }
}