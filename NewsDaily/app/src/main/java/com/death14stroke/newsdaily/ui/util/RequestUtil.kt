package com.death14stroke.newsdaily.ui.util

import com.death14stroke.newsdaily.data.model.Result

/**
 * Wrapper for getting state placeholders for underlying requests
 * @param func suspend function that computes the result
 */
suspend fun <T> sendRequest(
    func: suspend () -> T
): Result<T> {
    return try {
        Result.Success(func())
    } catch (e: Exception) {
        Result.Error(e.message, e)
    } finally {
        Result.Error()
    }
}