package com.andruid.magic.newsdaily.util

import android.util.Log

fun Any.logd(message: String, tag: String = "${this::class.java.simpleName}Log") {
    Log.d(tag, message)
}

fun Any.logi(message: String, tag: String = "${this::class.java.simpleName}Log") {
    Log.i(tag, message)
}

fun Any.loge(message: String, tag: String = "${this::class.java.simpleName}Log", throwable: Throwable? = null) {
    Log.e(tag, message, throwable)
}