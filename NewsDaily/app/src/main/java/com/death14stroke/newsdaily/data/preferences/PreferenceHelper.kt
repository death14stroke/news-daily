package com.death14stroke.newsdaily.data.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Helper class to maintain application preferences
 */
sealed interface PreferenceHelper {
    /**
     * Checks if this is the first time opening the application for showing introduction
     */
    fun isFirstTime(): Flow<Boolean>

    /**
     * Marks that user has gone through the introduction
     */
    suspend fun markFirstTimeDone()

    /**
     * Get user's preferred country for fetching the news
     */
    fun getSelectedCountry(): String
}