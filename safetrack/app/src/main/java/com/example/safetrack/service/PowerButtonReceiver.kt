package com.example.safetrack.service

import android.app.*
import android.content.*
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.safetrack.activity.emergency_screen
import com.google.android.material.bottomsheet.BottomSheetDialog

class PowerButtonReceiver : BroadcastReceiver() {

    private var pressCount = 0
    private val resetDelay: Long = 3000
    private val handler = Handler(Looper.getMainLooper())

    private val resetRunnable = Runnable {
        Log.d("PowerButtonReceiver", "Resetting press count to 0")
        pressCount = 0
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("PowerButtonReceiver", "Received action: $action")

        when (action) {
            // 👇 Add this block for BOOT COMPLETED
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("PowerButtonReceiver", "Device boot completed, starting service")

                val serviceIntent = Intent(context, PowerButtonService::class.java)
//                context?.startForegroundService(serviceIntent)
            }

            Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON -> {
                pressCount++
                Log.d("PowerButtonReceiver", "Screen event detected. Press count: $pressCount")

                handler.removeCallbacks(resetRunnable)
                handler.postDelayed(resetRunnable, resetDelay)

                if (pressCount >= 2) {
                    pressCount = 0


                    // ✅ Wake up the screen like Alarm apps do
                    val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
                    val wakeLock = powerManager.newWakeLock(
                        PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                        "safetrack:wakelock"
                    )
                    wakeLock.acquire(3000) // Screen stays on for 3 seconds


                    // Launch emergency screen directly
                    val emergencyIntent = Intent(context, emergency_screen::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context?.startActivity(emergencyIntent)

                    // Also show a full-screen notification
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        emergencyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val notification = NotificationCompat.Builder(context!!, "sos_channel")
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("🚨 SOS Activated")
                        .setContentText("Press here to call an Emergency")
                        .setPriority(NotificationCompat.PRIORITY_MAX) // 🔥 Make it a heads-up notification
                        .setCategory(NotificationCompat.CATEGORY_ALARM) // 🔥 Required for full-screen intent
                        .setFullScreenIntent(pendingIntent, true) // 🔥 Launch activity on screen off
                        .setAutoCancel(true)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .build()

                    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            "sos_channel",
                            "SOS Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                        )
                        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                        manager.createNotificationChannel(channel)
                    }

                    manager.notify(1001, notification)

                    Toast.makeText(context, "🚨 SOS Triggered!", Toast.LENGTH_SHORT).show()
                }
            }

            else -> {
                Log.w("PowerButtonReceiver", "Unknown intent received: $action")
            }
        }
    }

}


