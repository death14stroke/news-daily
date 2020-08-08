package com.andruid.magic.newsloader.data.model

sealed class Result<out T> {
    data class Success<out T>(val data: T?) : Result<T>()
    data class Error<out T>(val message: String) : Result<T>()
    data class Loading<out T>(val message: String) : Result<T>()
}