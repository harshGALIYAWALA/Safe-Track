package com.example.android.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android.R
import com.example.android.databinding.ActivitySettingScreenBinding
import com.example.android.service.LocationService
import com.example.android.service.PowerButtonService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class setting_screen : AppCompatActivity() {

    private lateinit var binding : ActivitySettingScreenBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var sharedPrefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // sharePreference
        // Load saved switch state
//        val prefs = getSharedPreferences("SafeTrackPrefs", MODE_PRIVATE)
//        val isServiceEnabled = prefs.getBoolean("service_enabled", false)
//        binding.emergencyForegoundButton.isChecked = isServiceEnabled
//
//        // Handle switch toggle
//        binding.emergencyForegoundButton.setOnCheckedChangeListener { _, isChecked ->
//            prefs.edit().putBoolean("service_enabled", isChecked).apply()
//
//            val serviceIntent = Intent(this, PowerButtonService::class.java)
//
//            if (isChecked) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startForegroundService(serviceIntent)
//                } else {
//                    startService(serviceIntent)
//                }
//
//            } else {
//                stopService(serviceIntent)
//            }
//
//
//        }

        val prefs = getSharedPreferences("SafeTrackPrefs", MODE_PRIVATE)
        val isServiceEnabled = prefs.getBoolean("service_enabled", false)
        binding.emergencyForegoundButton.isChecked = isServiceEnabled

// Set listener only AFTER setting isChecked, to avoid it firing on load
        binding.emergencyForegoundButton.setOnCheckedChangeListener(null) // remove any existing listener

        binding.emergencyForegoundButton.post {
            binding.emergencyForegoundButton.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("service_enabled", isChecked).apply()

                val serviceIntent = Intent(this, PowerButtonService::class.java)
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                } else {
                    stopService(serviceIntent)
                }
            }
        }

        // fetch name and set to textview as a Profile name
        fetchNameFromDatabase()


        binding.logOut.setOnClickListener{
            auth.signOut()
            finish()
            startActivity(Intent(this, login_screen::class.java))

        }

        binding.termAndCondition.setOnClickListener{
            startActivity(Intent(this, privacy_policy_screen::class.java))
        }

        binding.contactUs.setOnClickListener{
            startActivity(Intent(this, contact_us::class.java))
        }


        // Initialize sharedPrefs
        sharedPrefs = getSharedPreferences("SafeTrackPrefs", MODE_PRIVATE)

// Temporarily remove listener to avoid triggering on programmatic change
        binding.trackingService.setOnCheckedChangeListener(null)

// Restore saved state
        val isTrackingEnabled = sharedPrefs.getBoolean("location_tracking", false)
        binding.trackingService.isChecked = isTrackingEnabled

// Now add the listener
        binding.trackingService.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("location_tracking", isChecked).apply()

            val serviceIntent = Intent(this, LocationService::class.java)
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                Toast.makeText(this, "Location service started", Toast.LENGTH_SHORT).show()
            } else {
                stopService(serviceIntent)
                Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show()
            }
        }
//



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchNameFromDatabase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val nameRef = database.getReference("user").child(userId)

            nameRef.child("firstName").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.getValue(String::class.java)
                    name?.let {
                        binding.name.text = it
                        Toast.makeText(this@setting_screen, "name fetched successfully", Toast.LENGTH_SHORT).show()
                    }?: Toast.makeText(this@setting_screen, "name not found", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@setting_screen, "failed to fetch name ", Toast.LENGTH_SHORT).show()
                    Log.e("FirebaseName", "Database error: ${error.message}")
                }

            })

        }
    }

//    private fun startService() {
//        val intent = Intent(this, PowerButtonService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent)
//        } else {
//            startService(intent)
//        }
//    }
//
//    private fun stopService() {
//        val intent = Intent(this, PowerButtonService::class.java)
//        stopService(intent)
//    }
}

