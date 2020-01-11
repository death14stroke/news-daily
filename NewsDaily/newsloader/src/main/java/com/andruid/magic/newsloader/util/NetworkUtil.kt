@file:Suppress("DEPRECATION")

package com.andruid.magic.newsloader.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build

object NetworkUtil {
    fun hasNetwork(context: Context): Boolean {
        val cm: ConnectivityManager? = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager?
        cm?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cm.getNetworkCapabilities(cm.activeNetwork)?.apply {
                    return hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                }
            } else {
                val networkInfo: NetworkInfo? = cm.activeNetworkInfo
                networkInfo?.let {
                    return (it.isConnected &&
                            (networkInfo.type == ConnectivityManager.TYPE_WIFI ||
                                    networkInfo.type == ConnectivityManager.TYPE_MOBILE))
                }
            }
        }
        return false
    }
}