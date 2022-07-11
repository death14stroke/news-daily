package com.death14stroke.newsdaily.data.countries

import com.blongho.country_data.Country

/**
 * Helper class to load countries information
 */
sealed interface CountryHelper {
    /**
     * Fetch all countries supported by the application
     */
    suspend fun getCountries(): List<Country>

    /**
     * Get country information from country [code]
     */
    fun getCountryFromCode(code: String): Country
}