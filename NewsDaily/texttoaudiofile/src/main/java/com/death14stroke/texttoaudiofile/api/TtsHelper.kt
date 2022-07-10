package com.death14stroke.texttoaudiofile.api

import com.death14stroke.texttoaudiofile.data.model.TtsResult

sealed interface TtsHelper {
    val isReady: Boolean
    suspend fun convertToAudioFile(text: String, utteranceId: String): TtsResult
    fun release()
}