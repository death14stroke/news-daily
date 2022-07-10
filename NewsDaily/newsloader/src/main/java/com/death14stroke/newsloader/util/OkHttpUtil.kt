package com.death14stroke.newsloader.util

import android.content.Context
import android.util.Log
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// cache size 10 MB
private const val CACHE_SIZE = 10L * 1024L * 1024L

// if online, max cache age for details query = 10 min
private const val CACHE_MAX_AGE = 10

// if offline, max cache stale = 7 days if offline
private const val CACHE_MAX_STALE = 7

fun buildClient(context: Context): OkHttpClient {
    val cache = Cache(context.cacheDir, CACHE_SIZE)
    return OkHttpClient.Builder()
        .cache(cache)
        .addNetworkInterceptor { chain ->
            val url = chain.request().url()
            Log.d("TAG_RETROFIT", "url path = ${url.encodedPath()}")
            // add apikey and cache-control headers for request
            val request = chain.request()
                .newBuilder()
                .header(
                    "Cache-Control",
                    CacheControl.Builder().cacheControl(context.hasNetwork())
                )
                .build()

            val response = chain.proceed(request)
            // add cache headers in response for Retrofit to cache
            response.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header(
                    "Cache-Control",
                    CacheControl.Builder().onlineCacheControl()
                )
                .build()
        }
        .build()
}

private fun CacheControl.Builder.offlineCacheControl() =
    onlyIfCached().maxStale(CACHE_MAX_STALE, TimeUnit.DAYS).build().toString()

private fun CacheControl.Builder.onlineCacheControl() =
    maxAge(CACHE_MAX_AGE, TimeUnit.MINUTES).build().toString()

private fun CacheControl.Builder.cacheControl(hasNetwork: Boolean) =
    if (hasNetwork) onlineCacheControl() else offlineCacheControl()