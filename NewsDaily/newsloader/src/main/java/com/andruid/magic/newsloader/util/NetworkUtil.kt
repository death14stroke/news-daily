package com.andruid.magic.newsloader.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import com.andruid.magic.newsloader.data.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException

suspend fun <T> sendNetworkRequest(requestFunc: suspend () -> Response<T>): Result<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = requestFunc.invoke()
            Log.d("newsLog", "body = ${response.body() ?: "null"}")
            Result.Success(response.body())
        } catch (e: HttpException) {
            Result.Error<T>(e.message())
        } catch (e: ConnectException) {
            Result.Error<T>(e.message ?: "ConnectException")
        } catch (e: IOException) {
            Result.Error<T>(e.message ?: "IOException")
        }
    }
}

@Suppress("DEPRECATION")
fun Context.hasNetwork(): Boolean {
    getSystemService<ConnectivityManager>()?.let { cm ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.getNetworkCapabilities(cm.activeNetwork)?.let { nc ->
                return nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
        } else {
            cm.activeNetworkInfo?.let { networkInfo ->
                return (networkInfo.isConnected &&
                        (networkInfo.type == ConnectivityManager.TYPE_WIFI ||
                                networkInfo.type == ConnectivityManager.TYPE_MOBILE))
            }
        }
    }
    return false
}