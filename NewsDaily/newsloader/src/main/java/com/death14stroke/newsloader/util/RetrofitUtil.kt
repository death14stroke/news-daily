package com.death14stroke.newsloader.util

import android.content.Context
import com.death14stroke.newsloader.BuildConfig
import com.death14stroke.newsloader.api.RetrofitService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/** cache size 10 MB **/
private const val CACHE_SIZE = 10L * 1024L * 1024L

/** if online, max cache age for details query = 10 min **/
private const val CACHE_MAX_AGE = 10

/** if offline, max cache stale = 7 days if offline **/
private const val CACHE_MAX_STALE = 7

private const val BASE_URL = "https://daily-news-express.herokuapp.com"

/**
 * Build the [OkHttpClient] object for all network requests
 */
fun buildClient(context: Context, loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
    val cache = Cache(context.cacheDir, CACHE_SIZE)
    return OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(loggingInterceptor)
        .addNetworkInterceptor { chain ->
            // add cache-control headers for request
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

/**
 * Build the HTTP request and response logging interceptor.
 * For debug variant, level is [Level.BODY] while for release variant it is [Level.BASIC]
 */
private fun buildLoggingInterceptor() = HttpLoggingInterceptor().apply {
    setLevel(
        if (BuildConfig.DEBUG)
            Level.BODY
        else
            Level.BASIC
    )
}

private fun provideOkHttpClient(context: Context) =
    buildClient(context, buildLoggingInterceptor())

@OptIn(ExperimentalSerializationApi::class)
private fun provideRetrofit(
    okHttpClient: OkHttpClient
): Retrofit {
    val contentType = "application/json"
    val json = Json { ignoreUnknownKeys = true }
    return Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory(contentType.toMediaType()))
        .build()
}

/**
 * Creates the [RetrofitService] instance used for calling REST APIs
 */
fun provideApiService(context: Context): RetrofitService {
    val client = provideOkHttpClient(context)
    val retrofit = provideRetrofit(client)
    return retrofit.create(RetrofitService::class.java)
}

/**
 * Set maximum stale for offline cache of [CACHE_MAX_STALE] days
 * @see [onlineCacheControl] [cacheControl]
 */
private fun CacheControl.Builder.offlineCacheControl() =
    onlyIfCached().maxStale(CACHE_MAX_STALE, TimeUnit.DAYS).build().toString()

/**
 * Set maximum age for cache of [CACHE_MAX_AGE] minutes if network available
 * * @see [offlineCacheControl] [cacheControl]
 */
private fun CacheControl.Builder.onlineCacheControl() =
    maxAge(CACHE_MAX_AGE, TimeUnit.MINUTES).build().toString()

/**
 * Set cache control headers for HTTP request and response
 * @see [onlineCacheControl] [offlineCacheControl]
 */
private fun CacheControl.Builder.cacheControl(hasNetwork: Boolean) =
    if (hasNetwork) onlineCacheControl() else offlineCacheControl()