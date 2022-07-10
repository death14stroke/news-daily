package com.death14stroke.newsloader.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.core.content.getSystemService
import com.death14stroke.newsloader.api.RetrofitService
import com.death14stroke.newsloader.data.model.Result
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.net.ConnectException

private const val TAG = "NetworkLogger"
private const val BASE_URL = "https://daily-news-express.herokuapp.com"

suspend fun <T> sendNetworkRequest(requestFunc: suspend () -> Response<T>): Result<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = requestFunc.invoke()
            Log.d(TAG, "body = ${response.body() ?: "null"}")
            Result.Success(response.body()!!)
        } catch (e: HttpException) {
            Result.Error(e.message(), e)
        } catch (e: ConnectException) {
            Result.Error(e.message ?: "ConnectException", e)
        } catch (e: IOException) {
            Result.Error(e.message ?: "IOException", e)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e.message, e)
        }
    }
}

fun Context.hasNetwork(): Boolean {
    return getSystemService<ConnectivityManager>()?.let { cm ->
        cm.getNetworkCapabilities(cm.activeNetwork)?.let { nc ->
            nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )
        }
    } ?: false
}

private fun provideOkHttpClient(context: Context) = buildClient(context)

@OptIn(ExperimentalSerializationApi::class)
private fun provideRetrofit(
    okHttpClient: OkHttpClient
): Retrofit {
    val contentType = "application/json"
    val json = Json { ignoreUnknownKeys = true }
    return Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory(MediaType.get(contentType)))
        .build()
}

fun provideApiService(context: Context): RetrofitService {
    val client = provideOkHttpClient(context)
    val retrofit = provideRetrofit(client)
    return retrofit.create(RetrofitService::class.java)
}
