package com.example.android.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android.R
import com.example.android.databinding.ActivityMainBinding
import com.example.android.model.ContactModel
import com.example.android.service.PowerButtonService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yourapp.helpers.LocationTracker
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView
    private var sosContactList = ArrayList<String>()

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val SMS_PERMISSION_REQUEST = 1002
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Init MapLibre and firebase
        MapLibre.getInstance(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


       Log.d("mainContact", "onCreate: ${sosContactList}")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // let user know that service is running in background
        checkAndRequestPermissions()



        val prefs = getSharedPreferences("SafeTrackPrefs", MODE_PRIVATE)
        val isServiceEnabled = prefs.getBoolean("service_enabled", false)

        if (isServiceEnabled) {
            val serviceIntent = Intent(this, PowerButtonService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }

        Log.d( "foreGrouf", "2222onCreate: ")




        // displaying map  at fix coordinate
        binding.mapView.getMapAsync { map ->
            map.setStyle("https://api.maptiler.com/maps/streets/style.json?key=fIVoqc2T9Nh7Y7NIz9r9")
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(21.181005, 72.818671))
                .zoom(5.0)
                .build()
        }

        // locate the user location first and ofc. turn on the gps
        turnOnGPS_and_locateUser()


//         Initialize emergency Animation
//        this func. contain both the click and long listeners
//        long click --> first its animate and vibrate and after 300ms it will Intent
//        single click --> first its animate and vibrate and after 300ms it will Intent
        emergencyButtonAnimation()


        //Fetching contact numbers from database to send those numbers as sos message
        fetchContactsNumberFromDatabase()


        binding.settingsIcon.setOnClickListener{
            startActivity(Intent(this, setting_screen::class.java))
        }

        binding.contacts.setOnClickListener{
            startActivity(Intent(this, contact_screen::class.java))
        }

        binding.direactToMap.setOnClickListener{
            startActivity(Intent(this, map_screen::class.java))
        }

        binding.iconMap.setOnClickListener{
            startActivity(Intent(this, map_screen::class.java))
        }

        binding.contactIcon.setOnClickListener{
            startActivity(Intent(this, contact_screen::class.java))
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        } 
    }



    private fun checkAndRequestPermissions() {
        val PERMISSION_REQUEST_CODE = 101

// Only permissions that need to be requested at runtime
        val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
        )

// Add POST_NOTIFICATIONS if Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            REQUIRED_PERMISSIONS.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissions(missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    private fun turnOnGPS_and_locateUser() {
        // check if user has turn on GPS
        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val smsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)

        val permissionNeeded = ArrayList<String>()

        if(locationPermission != PackageManager.PERMISSION_GRANTED){
            permissionNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(smsPermission != PackageManager.PERMISSION_GRANTED){
            permissionNeeded.add(Manifest.permission.SEND_SMS)
        }

        if (permissionNeeded.isNotEmpty()){
            ActivityCompat.requestPermissions(this, permissionNeeded.toTypedArray(), 101)
        } else{
            LocationTracker.startTracking(this)
            Handler(Looper.getMainLooper()).postDelayed({
                val location = LocationTracker.latestLatLng
                Toast.makeText(this, "location is ready", Toast.LENGTH_SHORT).show()
            }, 3000)
        }

    }


    private fun fetchContactsNumberFromDatabase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val contactRef = database.getReference("user").child(userId).child("contacts")

            contactRef.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    sosContactList.clear()
                    for (contactSnapshot in snapshot.children){
                        val contact = contactSnapshot.getValue(ContactModel::class.java)
                        contact?.let { sosContactList.add(it.number!!) }
                    }
                    Log.d("SOS_CONTACTS", "Fetched contacts: $sosContactList") // Debugging log
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "failed to load data from database", Toast.LENGTH_SHORT).show()
                }

            } )

        }
    }


    // Function to check if the user has granted SMS permission before sending an SOS message
    private fun sendSOS() {
        val location = LocationTracker.latestLatLng
        val long = LocationTracker.latestLongitude
        val lat = LocationTracker.latestLatitude
        if(location != null){
            Toast.makeText(this, "location is ready", Toast.LENGTH_SHORT).show()
        } else{
            Toast.makeText(this, "location is null", Toast.LENGTH_SHORT).show()
        }
        val mapLink = "https://www.google.com/maps/search/?api=1&query=${lat},${long}"
        val message = "🚨 SOS ALERT! I am in danger. Please help!\nMy location: $mapLink"

        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Ask for SMS permission if not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_REQUEST)
        } else {
            // If permission is granted, get location and send SOS
            sendMessage(message)
        }
    }

    // Function to send the SOS message to all emergency contacts
    private fun sendMessage(message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            if (sosContactList.isNullOrEmpty()){
                Toast.makeText(this, "No contacts found, Data_Lt?> destroyed", Toast.LENGTH_SHORT).show()
            } else {
                for (contact in sosContactList) {
                    val parts = smsManager.divideMessage(message)  // Splits long messages
                    smsManager.sendMultipartTextMessage(contact, null, parts, null, null)
                }
                Toast.makeText(this, "🚨 SOS message sent!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SOS message!", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            100 -> {
                // GPS-related permissions
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO: Start GPS or related feature
                } else {
                    Toast.makeText(this, "GPS permission denied!", Toast.LENGTH_SHORT).show()
                }
            }

            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // SMS permission granted (for sending SOS)
                    sendMessage("🚨 SOS ALERT! I am in danger. Please help! My current location: mapLink ")

                    // Check if all other permissions were granted (location, etc.)
                    if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                        LocationTracker.startTracking(this)
                    }
                } else {
                    Toast.makeText(this, "Permissions required for proper functionality", Toast.LENGTH_LONG).show()
                }
            }

            else -> {
                Toast.makeText(this, "Unexpected permission result", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun emergencyButtonAnimation() {
        val clickAnimation = AnimationUtils.loadAnimation(this, R.anim.click_animation_emergency)
        val longClickAnimation = AnimationUtils.loadAnimation(this, R.anim.long_click_emergency)


        binding.emergencyButton.setOnClickListener {
            binding.emergencyButton.startAnimation(clickAnimation)
            sendSOS()
        }

        binding.emergencyButton.setOnLongClickListener {
            binding.emergencyButton.startAnimation(longClickAnimation)

            // Add a small delay before starting the intent
            binding.emergencyButton.postDelayed({
                startActivity(Intent(this, emergency_screen::class.java))
            }, 300) // Delay for 300ms (same as vibration time)

            true
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
//        currentLifecycleState = "onStart"
//        Log.d("LIFECYCLE_EVENT", "$currentLifecycleState called ${LocationTracker.latestLatLng}")
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
//        currentLifecycleState = "onResume"
//        Log.d("LIFECYCLE_EVENT", "$currentLifecycleState called ${LocationTracker.latestLatLng}")
//        isLoggingActive = true
//        logHandler.post(logRunnable)
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
//        currentLifecycleState = "onPause"
//        Log.d("LIFECYCLE_EVENT", "$currentLifecycleState called ${LocationTracker.latestLatLng}")
//        isLoggingActive = false
//        logHandler.removeCallbacks(logRunnable)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
//        currentLifecycleState = "onStop"
//        Log.d("LIFECYCLE_EVENT", "$currentLifecycleState called ${LocationTracker.latestLatLng}")
    }

    override fun onRestart() {
        super.onRestart()
//        currentLifecycleState = "onRestart"
//        Log.d("LIFECYCLE_EVENT", "$currentLifecycleState called ${LocationTracker.latestLatLng}")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
//        currentLifecycleState = "onLowMemory"
//        Log.d("GPS_LIFECYCLE", "$currentLifecycleState - LocationTracker isTracking: ${LocationTracker.latestLatLng}")
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
//        currentLifecycleState = "onDestroy"
//        isLoggingActive = false
//        logHandler.removeCallbacks(logRunnable)
//        Log.d("GPS_LIFECYCLE", "$currentLifecycleState - LocationTracker isTracking: ${LocationTracker.latestLatLng}")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
//        currentLifecycleState = "onSaveInstanceState"
//        Log.d("GPS_LIFECYCLE", "$currentLifecycleState - LocationTracker isTracking: ${LocationTracker.latestLatLng}")
    }
}