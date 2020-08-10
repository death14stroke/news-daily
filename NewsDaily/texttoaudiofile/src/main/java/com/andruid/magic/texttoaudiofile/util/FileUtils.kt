package com.andruid.magic.texttoaudiofile.util

object FileUtils {
    fun getFileName(utteranceId: String): String {
        return "news_${utteranceId}.mp3"
    }

    fun getUtteranceId(fileName: String): String {
        return fileName.substring(5, fileName.length - 4)
    }
}