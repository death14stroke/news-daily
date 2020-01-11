package com.andruid.magic.texttoaudiofile.util

class FileUtils {
    companion object {

        @JvmStatic
        fun getFileName(utteranceId: String): String {
            return "news_${utteranceId}.mp3"
        }

        @JvmStatic
        fun getUtteranceId(fileName: String): String {
            return fileName.substring(5, fileName.length - 4)
        }
    }
}