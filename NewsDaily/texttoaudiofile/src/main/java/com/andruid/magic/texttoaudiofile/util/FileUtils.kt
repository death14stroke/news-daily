package com.andruid.magic.texttoaudiofile.util

object FileUtils {
    fun getFileName(utteranceId: String) =
        "news_${utteranceId}.mp3"

    fun getUtteranceId(fileName: String) =
        fileName.substring(5, fileName.length - 4)
}