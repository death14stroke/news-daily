package com.death14stroke.newsdaily.data.countries

import android.content.Context
import com.blongho.country_data.Country
import com.blongho.country_data.World
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CountryHelperImpl(private val context: Context) : CountryHelper {
    companion object {
        private const val ASSET_COUNTRIES = "file:///android_asset/countries.txt"
    }

    init {
        World.init(context)
    }

    override fun getCountryFromCode(code: String): Country = World.getCountryFrom(code)

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getCountries(): List<Country> {
        val actualFilename = ASSET_COUNTRIES.split("file:///android_asset/").toTypedArray()[1]
        return withContext(Dispatchers.IO) {
            context.assets.open(actualFilename).bufferedReader().useLines { countryCodes ->
                countryCodes.map { code -> getCountryFromCode(code) }.toList()
            }
        }
    }
}