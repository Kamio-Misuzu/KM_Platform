package com.example.mforum.network

import android.content.Context
import com.example.mforum.utils.PreferencesManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    // 修改BASE_URL指向Flask后端
    private const val BASE_URL = "http://192.168.0.108:5000/api/"
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 添加重试拦截器
    class RetryInterceptor(private val maxRetries: Int) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            var response: Response
            var retryCount = 0

            while (true) {
                try {
                    response = chain.proceed(request)
                    if (response.isSuccessful || retryCount >= maxRetries) {
                        return response
                    }
                } catch (e: Exception) {
                    if (retryCount >= maxRetries) {
                        throw e
                    }
                }

                retryCount++
                // 等待一段时间后重试
                try {
                    Thread.sleep(1000 * retryCount.toLong())
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Interrupted during retry", e)
                }
            }
        }
    }


    private fun getClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val token = PreferencesManager(context).getToken()
                val requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")
                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .addInterceptor(RetryInterceptor(3)) // 添加重试拦截器，最多重试3次
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS) // 增加连接超时时间
            .readTimeout(60, TimeUnit.SECONDS) // 增加读取超时时间
            .writeTimeout(60, TimeUnit.SECONDS) // 增加写入超时时间
            .build()
    }

    fun getApiService(context: Context): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getClient(context))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}