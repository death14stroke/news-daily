package com.andruid.magic.newsloader.server;

import android.content.Context;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.andruid.magic.newsloader.data.Constants.BASE_URL;
import static com.andruid.magic.newsloader.data.Constants.CACHE_SIZE;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(Context context){
        if(retrofit==null) {
            Cache cache = new Cache(context.getCacheDir(), CACHE_SIZE);
            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(cache)
                    .build();
            retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}