package com.example.safetrack.service

import android.content.*
import android.os.Build
import com.example.safetrack.service.PowerButtonService

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val sharedPref = context?.getSharedPreferences("safetrack_prefs", Context.MODE_PRIVATE)
            val isServiceEnabled = sharedPref?.getBoolean("service_enabled", false) ?: false

            if (isServiceEnabled) {
                val serviceIntent = Intent(context, PowerButtonService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context?.startForegroundService(serviceIntent)
                } else {
                    context?.startService(serviceIntent)
                }
            }
        }
    }
}