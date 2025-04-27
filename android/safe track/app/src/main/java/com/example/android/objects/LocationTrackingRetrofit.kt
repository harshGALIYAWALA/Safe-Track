package com.example.android.objects

import com.example.android.interfaces.LocationTrackingInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LocationTrackingRetrofit {
    private val locationTrackingRet by lazy {
        Retrofit.Builder().baseUrl("http://192.168.4.101:5000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val locationTrackingApi by lazy {
        locationTrackingRet.create(LocationTrackingInterface::class.java)
    }
} 