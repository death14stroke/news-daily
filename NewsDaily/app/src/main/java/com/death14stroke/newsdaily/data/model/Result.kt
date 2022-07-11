package com.death14stroke.newsdaily.data.model

/**
 * Placeholder for state management of various api results
 */
sealed class Result<out T> {
    /**
     * Success state with the [data]
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Error state with the [error] and [throwable]
     */
    data class Error(val error: String? = null, val throwable: Throwable? = null) :
        Result<Nothing>()

    /**
     * Loading state
     */
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