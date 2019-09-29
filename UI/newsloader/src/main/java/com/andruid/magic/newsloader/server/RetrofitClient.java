package com.andruid.magic.newsloader.server;

import android.content.Context;

import com.andruid.magic.newsloader.util.NetworkUtil;
import com.google.gson.Gson;

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

import static com.andruid.magic.newsloader.data.Constants.BASE_URL;

public class RetrofitClient {
    private static final String HEADER_CACHE_CONTROL = "Cache-Control", HEADER_PRAGMA = "Pragma";
    private static final long CACHE_SIZE_MB = 10 * 1024 * 1024;
    private static final int MAX_AGE_SEC = 15, MAX_STALE_DAYS = 7;

    public static Retrofit getInstance(Context context){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(provideOfflineCacheInterceptor(context))
                .addNetworkInterceptor(provideCacheInterceptor(context))
                .cache(provideCache(context));
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .client(httpClient.build())
                .build();
    }

    private static Cache provideCache(Context context) {
        Cache cache = null;
        try {
            cache = new Cache(new File(context.getCacheDir(), "http-cache"), CACHE_SIZE_MB);
        } catch (Exception e) {
            Timber.e("Could not create Cache!");
        }
        return cache;
    }

    private static Interceptor provideCacheInterceptor(Context context) {
        return chain -> {
            Response response = chain.proceed(chain.request());
            CacheControl cacheControl;
            if (NetworkUtil.hasNetwork(context))
                cacheControl = new CacheControl.Builder()
                        .maxAge(MAX_AGE_SEC, TimeUnit.SECONDS)
                        .build();
            else
                cacheControl = new CacheControl.Builder()
                        .maxStale(MAX_STALE_DAYS, TimeUnit.DAYS)
                        .build();
            return response.newBuilder()
                    .removeHeader(HEADER_PRAGMA)
                    .removeHeader(HEADER_CACHE_CONTROL)
                    .header(HEADER_CACHE_CONTROL, cacheControl.toString())
                    .build();

        };
    }

    private static Interceptor provideOfflineCacheInterceptor(Context context) {
        return chain -> {
            Request request = chain.request();
            if (!NetworkUtil.hasNetwork(context)) {
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