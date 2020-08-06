package com.andruid.magic.newsloader.data.server

import android.content.Context
import com.andruid.magic.newsloader.util.hasNetwork
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://news-daily.herokuapp.com"
    private const val CACHE_SIZE = 10L * 1024L * 1024L

    private val LOCK = Any()
    private lateinit var INSTANCE: Retrofit

    fun getRetrofitInstance(context: Context): Retrofit {
        synchronized(LOCK) {
            if (!::INSTANCE.isInitialized) {
                val client = buildClient(context)

                INSTANCE = Retrofit.Builder()
                    .client(client)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
        }

        return INSTANCE
    }

    private fun buildClient(context: Context): OkHttpClient {
        val cache = Cache(context.cacheDir, CACHE_SIZE)
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor { chain: Interceptor.Chain ->
                var request = chain.request()
                request = if (context.hasNetwork())
                    request.newBuilder().header("Cache-Control", "public, max-age=" + 5)
                        .build()
                else
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7
                    )
                        .build()
                chain.proceed(request)
            }
            .build()
    }
}