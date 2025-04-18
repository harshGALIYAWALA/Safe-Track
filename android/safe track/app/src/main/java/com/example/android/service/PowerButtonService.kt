package com.example.android.service

import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.android.R

class PowerButtonService : Service() {

    private lateinit var receiver: PowerButtonReceiver

    override fun onCreate() {
        super.onCreate()
        Log.d("PowerButtonReceiver", "Service created")

        // Notification setup for foreground
        val channelId = "sos_service_channel"
        val channelName = "SOS Emergency Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SafeTrack Running")
            .setContentText("Listening for SOS trigger")
            .setSmallIcon(R.drawable.marker_map)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX) // For pre-Oreo
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE) // For API 31+
            .build()

        startForeground(1, notification)

        // ✅ Register screen events dynamically
        receiver = PowerButtonReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(receiver, filter)
        Log.d("PowerButtonReceiver", "Registered screen ON/OFF receiver dynamically")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PowerButtonReceiver", "Service started with START_STICKY")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        Log.d("PowerButtonReceiver", "Service destroyed and receiver unregistered")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
