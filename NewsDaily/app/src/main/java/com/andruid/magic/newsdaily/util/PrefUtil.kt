package com.andruid.magic.newsdaily.util

import android.content.Context
import android.telephony.TelephonyManager
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.preference.PreferenceManager
import com.andruid.magic.newsdaily.R
import com.blongho.country_data.Country
import com.blongho.country_data.World

private const val PREF_FIRST_TIME = "pref_first_time"
private const val KEY_FIRST_TIME = "first_time"

private const val ASSET_COUNTRIES = "file:///android_asset/countries.txt"

fun Context.isFirstTime(): Boolean {
    return getSharedPreferences(PREF_FIRST_TIME, Context.MODE_PRIVATE)
        .getBoolean(KEY_FIRST_TIME, true)
}

fun Context.updateFirstTimePref() {
    getSharedPreferences(PREF_FIRST_TIME, Context.MODE_PRIVATE)
        .edit { putBoolean(KEY_FIRST_TIME, false) }
}

fun Context.getCountries(): List<Country> {
    val actualFilename = ASSET_COUNTRIES.split("file:///android_asset/").toTypedArray()[1]
    return assets?.let { manager ->
        return@let manager.open(actualFilename).bufferedReader().useLines { countryCodes ->
            countryCodes.map { code -> World.getCountryFrom(code) }.toList()
        }
    } ?: return emptyList()
}

fun Context.getSelectedCountry(): String {
    return PreferenceManager.getDefaultSharedPreferences(this)
        .getString(getString(R.string.pref_country), getDefaultCountry())!!
}

fun Context.getDefaultCountry(): String {
    return getSystemService<TelephonyManager>()?.let { telephonyManager ->
        telephonyManager.networkCountryIso ?: "in"
    } ?: "in"
}