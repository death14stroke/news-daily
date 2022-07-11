package com.death14stroke.texttoaudiofile.api

import com.death14stroke.texttoaudiofile.data.exceptions.TextToSpeechException
import java.io.File

/**
 * Helper class to convert text to speech for playing news over audio
 */
sealed interface TtsHelper {
    /**
     * Convert given [text] to speech and save the audio file
     * @param utteranceId unique id for the TextToSpeech engine
     * @return path of the audio file
     * @throws TextToSpeechException
     */
    suspend fun convertToAudioFile(text: String, utteranceId: String): File

    /**
     * Release resources used by the TextToSpeech engine
     */
    fun release()

    /**
     * Initialize the TextToSpeech engine
     * @param onInitialize callback which returns the [Boolean] success status
     */
    fun initialize(onInitialize: (success: Boolean) -> Unit)
}