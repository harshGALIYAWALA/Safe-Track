package com.yourapp.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import org.maplibre.android.geometry.LatLng

object LocationTracker {

    var latestLatLng: LatLng? = null
    var latestLatitude: Double? = null
    var latestLongitude: Double? = null
    private var isTracking = false
    private var firstFixAchieved = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingPermission") // Make sure permissions are handled
    fun startTracking(context: Context) {
        if (isTracking) return
        isTracking = true
        firstFixAchieved = false

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val fastRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setMinUpdateIntervalMillis(500)
            .build()

        val slowRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000) // 3 seconds
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    latestLatLng = LatLng(location.latitude, location.longitude)
                    Log.d("LocationTracker", "Updated: $latestLatLng")

                    latestLatitude = location.latitude
                    latestLongitude = location.longitude

                    if (!firstFixAchieved) {
                        firstFixAchieved = true
                        Log.d("LocationTracker", "First fix achieved, switching to slow updates")

                        // Switch to slower updates
                        fusedLocationClient.removeLocationUpdates(this)
                        fusedLocationClient.requestLocationUpdates(slowRequest, this, Looper.getMainLooper())
                    }
                }
            }
        }

        // Start with fast updates
        fusedLocationClient.requestLocationUpdates(fastRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopTracking() {
        if (!isTracking) return
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isTracking = false
    }
}
