@file:Suppress("DEPRECATION")

package com.andruid.magic.newsloader.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build

class NetworkUtil {

    companion object {
        fun hasNetwork(context: Context) : Boolean {
            val cm : ConnectivityManager? = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager
            cm?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val network : Network? = cm.activeNetwork
                    if (network !== null) {
                        val nc : NetworkCapabilities? = cm.getNetworkCapabilities(network)
                        nc?.let {
                            return it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                    it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        } ?: return false
                    }
                }
                else {
                    val networkInfo : NetworkInfo? = cm.activeNetworkInfo
                    networkInfo?.let {
                        return (it.isConnected &&
                                (networkInfo.type == ConnectivityManager.TYPE_WIFI ||
                                        networkInfo.type == ConnectivityManager.TYPE_MOBILE))
                    } ?: return false
                }
                return false
            } ?: return false
        }
    }
}