package com.death14stroke.newsloader.data.model

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val error: String? = null, val throwable: Throwable? = null) :
        Result<Nothing>()

    object Loading : Result<Nothing>()
}

inline fun <reified T> Result<T>.onSuccess(callback: (data: T?) -> Unit): Result<T> {
    if (this is Result.Success)
        callback(data)
    return this
}

inline fun <reified T> Result<T>.onError(callback: (error: String?, throwable: Throwable?) -> Unit): Result<T> {
    if (this is Result.Error)
        callback(error, throwable)
    return this
}

inline fun <reified T> Result<T>.onLoading(callback: () -> Unit): Result<T> {
    if (this is Result.Loading)
        callback()
    return this
}