package com.death14stroke.newsdaily.data.preferences

import kotlinx.coroutines.flow.Flow

interface PreferenceHelper {
    fun isFirstTime(): Flow<Boolean>
    suspend fun markFirstTimeDone()
    fun getSelectedCountry(): String
}