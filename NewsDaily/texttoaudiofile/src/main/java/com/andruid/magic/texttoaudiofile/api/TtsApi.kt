package com.andruid.magic.texttoaudiofile.api

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.andruid.magic.texttoaudiofile.data.DIR_TTS
import com.andruid.magic.texttoaudiofile.data.model.TtsResult
import com.andruid.magic.texttoaudiofile.util.FileUtils.getFileName
import java.io.File
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object TtsApi {
    private val TAG = TtsApi::class.java.simpleName

    private lateinit var tts: TextToSpeech
    private lateinit var dir: File

    var isReady = false
        private set

    fun init(application: Application) {
        dir = File(application.cacheDir, DIR_TTS)
        initTTS(application.applicationContext)
    }

    fun release() {
        tts.shutdown()
    }

    private fun initTTS(context: Context) {
        tts = TextToSpeech(context, TextToSpeech.OnInitListener {
            val status = it
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.getDefault())
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
                    isReady = true
            }
        })
    }

    suspend fun convertToAudioFile(text: String, utteranceId: String): TtsResult {
        lateinit var result: Continuation<TtsResult>

        if (!dir.exists()) {
            val res = dir.mkdir()
            Log.d(TAG, "dir created $res")
        }

        if (isReady) {
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String) {
                    val file = File(dir, getFileName(utteranceId))
                    result.resume(TtsResult.Success(file))
                }

                override fun onError(utteranceId: String) {
                    result.resume(TtsResult.Error("Failed creating audio for utterance id $utteranceId"))
                }

                override fun onStart(utteranceId: String) {}
            })

            val file = File(dir, getFileName(utteranceId))
            tts.synthesizeToFile(text, null, file, utteranceId)
        } else
            result.resume(TtsResult.Error("TTS init failed"))

        return suspendCoroutine { continuation -> result = continuation }
    }
}