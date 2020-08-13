package com.andruid.magic.texttoaudiofile.data.model

import java.io.File

sealed class TtsResult {
    data class Success(val file: File) : TtsResult()
    data class Error(val error: String) : TtsResult()
}