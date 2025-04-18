package com.example.android.objects

import com.example.android.interfaces.OpenRouteServiceAPI
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Base URL for OpenRouteService API v2
    private const val BASE_URL = "https://api.openrouteservice.org/"

    // Keep the logging interceptor for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configure OkHttpClient (consider adding timeouts)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Example: Add connect timeout
        .readTimeout(30, TimeUnit.SECONDS)    // Example: Add read timeout
        .build()

    // Build Retrofit instance for ORS
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // Use the configured OkHttpClient
        .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON parsing
        .build()

    // Create the API service instance using the new interface
    // Rename mapTilerAPI to something more appropriate like orsApi
    val orsApi: OpenRouteServiceAPI = retrofit.create(OpenRouteServiceAPI::class.java)
}