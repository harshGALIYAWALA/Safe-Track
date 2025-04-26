package com.example.android.interfaces

import com.example.android.model.TrackingRetrofitModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LocationTrackingInterface {

    @POST("api/startSession")
    fun sendLocation(                        // i can use suspend before fun to make sure that main thread is not block
        @Body location: TrackingRetrofitModel
    ): Call<TrackingRetrofitModel>

}