package com.andruid.magic.newsloader.server;

import android.content.Context;

import com.andruid.magic.newsloader.util.NetworkUtil;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.andruid.magic.newsloader.data.Constants.BASE_URL;
import static com.andruid.magic.newsloader.data.Constants.CACHE_SIZE;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(final Context context){
        if(retrofit==null) {
            Cache cache = new Cache(context.getCacheDir(), CACHE_SIZE);
            OkHttpClient client = new OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            if(NetworkUtil.hasNetwork(context))
                                request = request.newBuilder().header("Cache-Control",
                                        "public, max-age=" + 5).build();
                            else
                                request = request.newBuilder().header("Cache-Control",
                                        "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7)
                                        .build();
                            return chain.proceed(request);
                        }
                    })
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