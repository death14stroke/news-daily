package com.andruid.magic.newsloader.server;

import android.content.Context;

import com.andruid.magic.newsloader.util.NetworkUtil;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class RetrofitClient {
    private static final String BASE_URL = "https://news-daily.herokuapp.com",
            HEADER_CACHE_CONTROL = "Cache-Control", HEADER_PRAGMA = "Pragma";
    private static final int CACHE_SIZE = 50 * 1024 * 1024, MAX_STALE_DAYS = 2, MAX_AGE_SEC = 60;
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(final Context context){
        if(retrofit==null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .addInterceptor(provideOfflineCacheInterceptor(context))
                    .addNetworkInterceptor(provideCacheInterceptor(context))
                    .cache(provideCache(context));
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(builder.build())
                    .build();
        }
        return retrofit;
    }

    private static Cache provideCache(final Context context) {
        Cache cache = null;
        try {
            cache = new Cache(new File(context.getCacheDir(), "http-cache"), CACHE_SIZE);
        } catch (Exception e) {
            Timber.e("Could not create Cache!");
            e.printStackTrace();
        }
        return cache;
    }

    private static Interceptor provideCacheInterceptor(final Context context) {
        return chain -> {
            Response response = chain.proceed(chain.request());
            CacheControl cacheControl;
            if (NetworkUtil.isConnected(context)) {
                cacheControl = new CacheControl.Builder()
                        .maxAge(MAX_AGE_SEC, TimeUnit.SECONDS)
                        .build();
            } else {
                cacheControl = new CacheControl.Builder()
                        .maxStale(MAX_STALE_DAYS, TimeUnit.DAYS)
                        .build();
            }
            return response.newBuilder()
                    .removeHeader(HEADER_PRAGMA)
                    .removeHeader(HEADER_CACHE_CONTROL)
                    .header(HEADER_CACHE_CONTROL, cacheControl.toString())
                    .build();
        };
    }

    private static Interceptor provideOfflineCacheInterceptor(final Context context) {
        return chain -> {
            Request request = chain.request();
            if (!NetworkUtil.isConnected(context)) {
                CacheControl cacheControl = new CacheControl.Builder()
                        .maxStale(MAX_STALE_DAYS, TimeUnit.DAYS)
                        .build();
                request = request.newBuilder()
                        .removeHeader(HEADER_PRAGMA)
                        .removeHeader(HEADER_CACHE_CONTROL)
                        .cacheControl(cacheControl)
                        .build();
            }
            return chain.proceed(request);
        };
    }
}