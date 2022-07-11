package com.death14stroke.texttoaudiofile.api

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.death14stroke.texttoaudiofile.data.exceptions.TextToSpeechException
import com.death14stroke.texttoaudiofile.util.FileUtils.getFileName
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TtsHelperImpl(private val context: Context) : TtsHelper {
    companion object {
        /**
         * Private cache directory where all text to speech outputs are saved
         */
        private const val DIR_TTS = "ttsNews"
    }

    private lateinit var tts: TextToSpeech
    private val dir = File(context.cacheDir, DIR_TTS)

    override fun initialize(onInitialize: (success: Boolean) -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.getDefault())
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
                    onInitialize.invoke(true)
                else
                    onInitialize.invoke(false)
            } else {
                onInitialize.invoke(false)
            }
        }
    }

    override suspend fun convertToAudioFile(text: String, utteranceId: String) =
        suspendCoroutine { continuation ->
            if (!dir.exists()) {
                val res = dir.mkdir()
                Timber.d("dir created $res")
            }

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String) {
                    val file = File(dir, getFileName(utteranceId))
                    continuation.resume(file)
                }

                @Suppress("OVERRIDE_DEPRECATION")
                override fun onError(utteranceId: String) {
                    onError(utteranceId, -99)
                }

                override fun onError(utteranceId: String, errorCode: Int) {
                    super.onError(utteranceId, errorCode)
                    continuation.resumeWithException(TextToSpeechException(utteranceId, errorCode))
                }

                override fun onStart(utteranceId: String) {}
            })

            val file = File(dir, getFileName(utteranceId))
            tts.synthesizeToFile(text, null, file, utteranceId)
        }

    override fun release() {
        tts.shutdown()
    }
}