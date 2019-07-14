package com.andruid.magic.newsloader.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

public class NetworkUtil {

    @SuppressWarnings("deprecation")
    public static boolean isConnected(final Context context){
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final Network network = cm.getActiveNetwork();
                if (network != null) {
                    final NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                    if (nc != null)
                        return nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                }
            }
            else{
                final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null)
                    return (networkInfo.isConnected() &&
                            (networkInfo.getType() == ConnectivityManager.TYPE_WIFI ||
                                    networkInfo.getType() == ConnectivityManager.TYPE_MOBILE));
            }
        }
        return false;
    }
}