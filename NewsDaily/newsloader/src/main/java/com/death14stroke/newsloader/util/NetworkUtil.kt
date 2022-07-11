package com.death14stroke.newsloader.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException

/**
 * Wrapper for making network call using retrofit
 * @param requestFunc suspend function that makes the network call
 * @return the response of the network call
 * @throws HttpException
 * @throws ConnectException
 * @throws IOException
 * @throws Exception
 */
suspend fun <T> makeRetrofitCall(requestFunc: suspend () -> Response<T>): T {
    return withContext(Dispatchers.IO) {
        val response = requestFunc.invoke()
        response.body()!!
    }
}

/**
 * Checks if the device is connected to the internet
 * @return true/false
 */
fun Context.hasNetwork(): Boolean {
    return getSystemService<ConnectivityManager>()?.let { cm ->
        cm.getNetworkCapabilities(cm.activeNetwork)?.let { nc ->
            nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )
        }
    } ?: false
}