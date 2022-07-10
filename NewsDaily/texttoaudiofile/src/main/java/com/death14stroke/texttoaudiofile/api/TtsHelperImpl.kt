package com.death14stroke.texttoaudiofile.api

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.death14stroke.texttoaudiofile.data.DIR_TTS
import com.death14stroke.texttoaudiofile.data.model.TtsResult
import com.death14stroke.texttoaudiofile.util.FileUtils.getFileName
import java.io.File
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TtsHelperImpl(context: Context) : TtsHelper {
    companion object {
        private val TAG = TtsHelperImpl::class.java.simpleName
    }

    private val initListener = TextToSpeech.OnInitListener { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.getDefault())
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
                _isReady = true
        }
    }
    private val tts: TextToSpeech = TextToSpeech(context, initListener)
    private val dir = File(context.cacheDir, DIR_TTS)

    private var _isReady = false
    override val isReady: Boolean
        get() = _isReady

    override suspend fun convertToAudioFile(text: String, utteranceId: String) =
        suspendCoroutine { continuation ->
            if (!dir.exists()) {
                val res = dir.mkdir()
                Log.d(TAG, "dir created $res")
            }

            if (isReady) {
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String) {
                        val file = File(dir, getFileName(utteranceId))
                        continuation.resume(TtsResult.Success(file))
                    }

                    @Suppress("OVERRIDE_DEPRECATION")
                    override fun onError(utteranceId: String) {
                        continuation.resume(TtsResult.Error("Failed creating audio for utterance id $utteranceId"))
                    }

                    override fun onStart(utteranceId: String) {}
                })

                val file = File(dir, getFileName(utteranceId))
                tts.synthesizeToFile(text, null, file, utteranceId)
            } else {
                continuation.resume(TtsResult.Error("TTS init failed"))
            }
        }

    override fun release() {
        tts.shutdown()
    }
}