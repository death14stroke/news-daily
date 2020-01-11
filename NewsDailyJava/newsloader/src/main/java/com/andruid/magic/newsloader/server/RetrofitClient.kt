package com.andruid.magic.newsloader.server

import android.content.Context
import com.andruid.magic.newsloader.data.Constants
import com.andruid.magic.newsloader.util.NetworkUtil
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {

    companion object {
        private lateinit var INSTANCE : Retrofit
        private val LOCK = Any()

        @JvmStatic
        fun getRetrofitInstance(context: Context): Retrofit {
            synchronized(LOCK) {
                if (!::INSTANCE.isInitialized) {
                    val cache = Cache(context.cacheDir, Constants.CACHE_SIZE.toLong())
                    val client = OkHttpClient.Builder()
                            .cache(cache)
                            .addInterceptor { chain: Interceptor.Chain ->
                                var request = chain.request()
                                request = if (NetworkUtil.hasNetwork(context))
                                    request.newBuilder().header("Cache-Control", "public, max-age=" + 5)
                                            .build()
                                else
                                    request.newBuilder().header("Cache-Control",
                                            "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7)
                                            .build()
                                chain.proceed(request)
                            }
                            .build()
                    INSTANCE = Retrofit.Builder()
                            .client(client)
                            .baseUrl(Constants.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                }
            }
            return INSTANCE
        }
    }
}