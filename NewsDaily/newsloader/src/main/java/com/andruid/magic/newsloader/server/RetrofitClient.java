package com.andruid.magic.newsloader.server;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.andruid.magic.newsloader.data.Constants.BASE_URL;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(){
        if(retrofit==null)
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        return retrofit;
    }
}