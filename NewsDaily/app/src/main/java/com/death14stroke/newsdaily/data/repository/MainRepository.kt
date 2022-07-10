package com.death14stroke.newsdaily.data.repository

import com.death14stroke.newsdaily.data.countries.CountryHelper
import com.death14stroke.newsdaily.data.preferences.PreferenceHelper
import com.death14stroke.newsloader.api.NetworkNewsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class MainRepository(
    private val networkHelper: NetworkNewsHelper,
    private val preferenceHelper: PreferenceHelper,
    private val countryHelper: CountryHelper
) {
    fun isFirstTime() = preferenceHelper.isFirstTime()

    suspend fun markFirstTimeDone() {
        coroutineScope {
            launch(Dispatchers.IO) { preferenceHelper.markFirstTimeDone() }
        }
    }

    suspend fun getCountries() = countryHelper.getCountries()

    fun getCountryFromCode(code: String) = countryHelper.getCountryFromCode(code)

    fun getSelectedCountry() = preferenceHelper.getSelectedCountry()

    suspend fun loadHeadlines(country: String, category: String, page: Int, pageSize: Int) =
        networkHelper.loadHeadlines(country, category, page, pageSize)

    suspend fun loadArticles(language: String, query: String, page: Int, pageSize: Int) =
        networkHelper.loadArticles(language, query, page, pageSize)
}