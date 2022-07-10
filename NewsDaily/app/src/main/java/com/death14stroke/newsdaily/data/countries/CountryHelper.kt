package com.death14stroke.newsdaily.data.countries

import com.blongho.country_data.Country

sealed interface CountryHelper {
    suspend fun getCountries(): List<Country>
    fun getCountryFromCode(code: String): Country
}