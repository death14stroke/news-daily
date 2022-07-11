package com.death14stroke.texttoaudiofile.data.exceptions

/**
 * Exception thrown when text to speech fails for a particular text
 * @property utteranceId unique identifier for TextToSpeech engine
 * @property errorCode reason why the text to speech failed
 */
data class TextToSpeechException(private val utteranceId: String, private val errorCode: Int) :
    Exception() {
    override val message: String
        get() = "Text to speech failed for utteranceId = $utteranceId with errorCode = $errorCode"
}