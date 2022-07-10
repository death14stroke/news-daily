package com.death14stroke.newsdaily.data.preferences

import android.content.Context
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import com.death14stroke.newsdaily.R
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceHelperImpl(private val context: Context) : PreferenceHelper {
    companion object {
        private val IS_FIRST_TIME = booleanPreferencesKey("first_time")
    }

    private val dataStore = context.dataStore

    override fun isFirstTime() = dataStore.data.map { preferences ->
        preferences[IS_FIRST_TIME] ?: true
    }

    override suspend fun markFirstTimeDone() {
        dataStore.edit { settings ->
            settings[IS_FIRST_TIME] = false
        }
    }

    override fun getSelectedCountry(): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(context.getString(R.string.pref_country), getDefaultCountry())!!
    }

    private fun getDefaultCountry(): String {
        return context.getSystemService<TelephonyManager>()?.networkCountryIso ?: "in"
    }
}