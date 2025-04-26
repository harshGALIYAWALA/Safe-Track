package com.example.android.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.android.R
import com.example.android.activity.setting_screen
import com.example.android.model.CreatedAt
import com.example.android.model.Location
import com.example.android.model.TrackingRetrofitModel
import com.example.android.objects.LocationTrackingRetrofit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Time
import java.time.LocalDate
import java.time.LocalTime

class LocationService: Service() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase



    private  var count: Int = 0

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        startUpdatingService()
    }


    private fun startUpdatingService(){
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                location?.let {
                   count++
                    Log.d("LocationService", "$count Location updated: ${it.latitude}, ${it.longitude}")
//                    val timestamp = it.time/1000      // Convert to seconds (Unix time)
//                    val isSharing = true  // You can toggle this later dynamically
//                    storing location in firebase
//                    saveLocationToFirebase(it.latitude, it.longitude, timestamp, isSharing)

                    // sending location to backend
                    val time = LocalTime.now()
                    val date = LocalDate.now()
                   sendLocationToBackend(it.latitude, it.latitude, time, date)

                }?: Log.d("LocationService", "Location is null")
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("LocationService", "Permission not granted")
        }
        fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    // storing location to backend using retrofit (Node, Express and MongoDB)
    private fun sendLocationToBackend(latitude: Double, longitude: Double, time: LocalTime, date: LocalDate){
        val session = TrackingRetrofitModel(
            userId = "1",
            location = Location(latitude, longitude),
            createdAt = CreatedAt(date.toString(), time.toString())
        )
        LocationTrackingRetrofit.locationTrackingApi.sendLocation(session).enqueue(object : Callback<TrackingRetrofitModel>{
            override fun onResponse(
                p0: Call<TrackingRetrofitModel?>,
                p1: Response<TrackingRetrofitModel?>
            ) {
                Log.d("LocationService", "success")
                if(p1.isSuccessful){
                    Toast.makeText(this@LocationService, "Retrofit is Working", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@LocationService, "it is working but something is down", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(
                p0: Call<TrackingRetrofitModel?>,
                p1: Throwable
            ) {
                Log.d("LocationService", "failed ${p1.message}")
                Toast.makeText(this@LocationService, "whole system is broken! ${p1.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    // storing location to firebase
    private fun saveLocationToFirebase(latitude: Double, longitude: Double, timestamp: Long, isSharing: Boolean){
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val userId = auth.currentUser?.uid
        userId?.let {
            val locationRef = database.getReference("user").child(userId).child("liveLocation")

            val locationData = mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "timestamp" to timestamp,
                "isSharing" to isSharing
            )
            locationRef.setValue(locationData).addOnSuccessListener{
                Log.d("LocationService", "Location saved to Firebase")
            }.addOnFailureListener{
                Log.d("LocationService", "Failed to save location to Firebase")
            }
        } ?: Log.d("LocationService", "User ID is null")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(2, createNotification())
        return START_STICKY
    }

    private fun createNotification(): Notification{
        val channelId = "location_channel"
        val channelName = "Location Tracking"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Create Intent to open the desired Activity when clicked
        val intent = Intent(this, setting_screen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("tracking Location")
            .setContentText("Live location is being tracked")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSmallIcon(R.drawable.marker_map)
            .build()

        return notification
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LocationService", "Service destroyed")
        stopForeground(STOP_FOREGROUND_REMOVE)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}