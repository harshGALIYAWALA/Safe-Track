package com.example.android.interfaces

import com.example.android.model.OrsRouteResponse // Use the new ORS response model
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenRouteServiceAPI {

    /**
     * Gets directions using the ORS API v2 GET endpoint.
     *
     * @param profile The routing profile (e.g., "driving-car", "foot-walking", "cycling-regular").
     * @param apiKey Your OpenRouteService API key. Passed as a query parameter here.
     * Alternatively, pass via Authorization header: @Header("Authorization") apiKey: String
     * @param start Start coordinate formatted as "longitude,latitude".
     * @param end End coordinate formatted as "longitude,latitude".
     * @return A Call object for the OrsRouteResponse.
     */
    @GET("v2/directions/{profile}")
    fun getDirections(
        @Path("profile") profile: String,
        @Query("api_key") apiKey: String, // API key as query parameter
        @Query("start") start: String,    // Start coordinate "lon,lat"
        @Query("end") end: String         // End coordinate "lon,lat"
    ): Call<OrsRouteResponse> // Return type uses the new data model

    /*
    // Example using Authorization Header instead of query parameter for API key
    @GET("v2/directions/{profile}")
    fun getDirectionsWithHeader(
        @Path("profile") profile: String,
        @Header("Authorization") apiKey: String, // API key as Authorization header
        @Query("start") start: String,
        @Query("end") end: String
    ): Call<OrsRouteResponse>
    */
}
