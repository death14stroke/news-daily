package com.andruid.magic.texttoaudiofile.api

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import com.andruid.magic.texttoaudiofile.data.Constants
import com.andruid.magic.texttoaudiofile.util.FileUtils.getFileName
import java.io.File
import java.util.*

class TtsApi {
    companion object {
        private val TAG = TtsApi::class.java.simpleName

        private lateinit var tts: TextToSpeech
        private lateinit var dir: File
        private lateinit var INSTANCE: TtsApi

        private var ttsInit = false

        private val LOCK = Any()

        @JvmStatic
        fun init(application: Application) {
            dir = File(application.cacheDir, Constants.DIR_TTS)
            initTTS(application.applicationContext)
        }

        @JvmStatic
        fun getInstance(): TtsApi {
            if (!::INSTANCE.isInitialized) {
                synchronized(LOCK) {
                    Log.d(TAG, "tts api instance created")
                    INSTANCE = TtsApi()
                }
            }
            return INSTANCE
        }

        @JvmStatic
        fun release() {
            tts.shutdown()
        }

        private fun initTTS(context: Context) {
            tts = TextToSpeech(context, TextToSpeech.OnInitListener {
                val status = it
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts.setLanguage(Locale.getDefault())
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED
                    )
                        Toast.makeText(
                            context, "Text to speech not available",
                            Toast.LENGTH_SHORT
                        ).show()
                    else
                        ttsInit = true
                }
            })
        }
    }

    fun convertToAudioFile(
        text: String, utteranceId: String,
        mListener: AudioConversionListener
    ) {
        if (!dir.exists()) {
            val res = dir.mkdir()
            Log.d(TAG, "dir created $res")
        }
        if (ttsInit) {
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {}

                override fun onDone(utteranceId: String) {
                    val file = File(dir, getFileName(utteranceId))
                    mListener.onAudioCreated(file)
                }

                override fun onError(utteranceId: String) {
                    mListener.onFailure("Failed creating audio for utterance id $utteranceId")
                }
            })
            val file = File(dir, getFileName(utteranceId))
            tts.synthesizeToFile(text, null, file, utteranceId)
        } else
            mListener.onFailure("TTS init failed")
    }

    fun isReady() = ttsInit

    interface AudioConversionListener {
        fun onAudioCreated(file: File)
        fun onFailure(msg: String)
    }
}