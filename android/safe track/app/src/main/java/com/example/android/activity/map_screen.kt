package com.example.android.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android.R
import com.example.android.databinding.ActivityMapScreenBinding
import com.example.android.model.OrsRouteResponse
import com.example.android.objects.RetrofitClient
import com.google.android.gms.location.*
import com.yourapp.helpers.LocationTracker
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import retrofit2.Call
import retrofit2.Response
import java.util.Locale


class map_screen : AppCompatActivity() {

    private lateinit var binding: ActivityMapScreenBinding
    private var mapView: MapView? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mapLibreMap: MapLibreMap
    private var userMarker: org.maplibre.android.annotations.Marker? = null
    private var isUserInteractedWithMap = false  // To track user interaction
    private var isFirstLocationUpdate  = true
    private var searchMarker: org.maplibre.android.annotations.Marker? = null

    // Variable to hold the reference to the currently drawn polyline for later removal
    private var currentRoutePolyline: Polyline? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityMapScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialization
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)



        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            mapLibreMap = map
            map.setStyle("https://api.maptiler.com/maps/streets/style.json?key=fIVoqc2T9Nh7Y7NIz9r9")
            // streets
            // satellite
            //basic
            //hybrid
            map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                .target(LatLng(21.181005, 72.818671))
                .zoom(12.0)
                .build()

            // Add listener for user interaction with the map
            mapLibreMap.addOnCameraIdleListener {
                isUserInteractedWithMap = true  // User has moved the map manually
            }

            requestLocationPermission()
            // set the position of compass below Search Bar
            setCompassPosition()
            // working SearchBar
            searchAndLocateAddress()
        }



        // locate user on map
        binding.baseLocator.setOnClickListener{
            val userLocation = LocationTracker.latestLatLng
            if (userLocation != null) {
                mapLibreMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(userLocation)
                            .zoom(15.0)
                            .build()
                    )
                )
                isUserInteractedWithMap = false
            } else{
                Toast.makeText(this, "try again in 3 secon", Toast.LENGTH_SHORT).show()
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateUserLocation(location)
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun searchAndLocateAddress() {
        binding.searchEditText.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchLocation(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
    }

    private fun searchLocation(locationName: String){
        val geoCoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses  = geoCoder.getFromLocationName(locationName, 1)
            if (addresses  != null && addresses .isNotEmpty()) {

                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)

                mapLibreMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, 12.0)
                )

                // Load and resize the bitmap
                val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.locator_22)
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 110, 110, false)

                // Create custom icon from scaled bitmap
                val iconFactory = IconFactory.getInstance(this)
                val customIcon = iconFactory.fromBitmap(scaledBitmap)

                searchMarker?.let { mapLibreMap.removeMarker(it) } //  to remove previous searched marker so it wont show two marker

                // Add new marker and save reference
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title(locationName)
                    .icon(customIcon)

                searchMarker = mapLibreMap.addMarker(markerOptions)

                // ge the route to new location from base location
                val userLocation = LocationTracker.latestLatLng
                if (userLocation != null) {
                    getRoute(userLocation, latLng)
                }

            } else{
                Toast.makeText(this, "failed to show address", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun getRoute(start: LatLng, end: LatLng) {
        // 1. Get your ORS API key (Replace with your actual key)
        // !! IMPORTANT: Avoid hardcoding keys in production. Use BuildConfig or secure storage. !!
        val orsApiKey = "5b3ce3597851110001cf6248bf83669bc69c4732bda0bdfc52efb5c1" // YOUR KEY HERE

        // 2. Format coordinates for ORS API: "longitude,latitude"
        val startPoint = "${start.longitude},${start.latitude}"
        val endPoint = "${end.longitude},${end.latitude}"

        // 3. Define the routing profile (check ORS docs for options)
        val profile = "driving-car" // e.g., "driving-car", "foot-walking", "cycling-regular"

        // 4. Create the API call using the RetrofitClient and the ORS API interface
        val call = RetrofitClient.orsApi.getDirections(
            profile = profile,
            apiKey = orsApiKey,
            start = startPoint,
            end = endPoint
        )

        // 5. Execute the call asynchronously
        call.enqueue(object : retrofit2.Callback<OrsRouteResponse> {
            override fun onResponse(call: Call<OrsRouteResponse>, response: Response<OrsRouteResponse>) {
                if (response.isSuccessful) {
                    val routeResponse = response.body()
                    val feature = routeResponse?.features?.firstOrNull()
                    val geometry = feature?.geometry
                    val summary = feature?.properties?.summary

                    // Inside the getRoute function -> call.enqueue -> onResponse callback:
// ... (previous code in onResponse)
                    if (feature != null && geometry?.coordinates != null && summary != null) {
                        val routeCoordinates = geometry.coordinates // List<List<Double>> ([lon, lat])
                        val distanceMeters = summary.distance ?: 0.0
                        val durationSeconds = summary.duration ?: 0.0

                        Log.d("ROUTE_ERROR", "ORS Route fetched successfully.") // Changed tag for info
                        Log.d("ROUTE_ERROR", "Distance: ${distanceMeters / 1000.0} km") // Changed tag for info
                        Log.d("ROUTE_ERROR", "Duration: ${durationSeconds / 60.0} min") // Changed tag for info
                        Log.d("ROUTE_ERROR", "Coordinates count: ${routeCoordinates.size}") // Changed tag for info

                        // Pass the coordinates AND the CORRECT map object to your map drawing function
                        // Check if mapLibreMap (the one initialized in getMapAsync) is ready
                        if (::mapLibreMap.isInitialized) { // CHECK THE CORRECT VARIABLE
                            // Call drawRouteOnMap using the core polyline method
                            drawRouteOnMap(mapLibreMap, routeCoordinates) // PASS THE CORRECT VARIABLE
                        } else {
                            // This block should ideally not be reached if getRoute is called
                            // after getMapAsync completes (which it is in searchLocation),
                            // but keep the log for safety.
                            Log.e("ROUTE_ERROR", "MapLibreMap object (mapLibreMap) is not initialized. Unexpected state.")
                            Toast.makeText(this@map_screen, "Map not ready to draw route", Toast.LENGTH_SHORT).show()
                        }

                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Toast.makeText(this@map_screen, "ORS route fetch failed: ${response.code()}", Toast.LENGTH_LONG).show()
                    Log.e("ROUTE_ERROR", "ORS API Error Code: ${response.code()}, Message: ${response.message()}")
                    Log.e("ROUTE_ERROR", "ORS Error Body: $errorBody")
                }
            }

            override fun onFailure(call: Call<OrsRouteResponse>, t: Throwable) {
                Toast.makeText(this@map_screen, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("ROUTE_ERROR", "Network request failed", t)
            }
        })
    }

    private fun drawRouteOnMap(map: MapLibreMap, coordinates: List<List<Double>>) {
        Log.d("ROUTE_ERROR", "Attempting to draw route with ${coordinates.size} coordinates using core Polyline.")

        if (coordinates.isEmpty()) {
            Log.w("ROUTE_ERROR", "Coordinate list is empty, cannot draw route.")
            return
        }

        // Convert the list of [lon, lat] pairs to a list of MapLibre LatLng objects
        val mapLibreLatLngs = mutableListOf<LatLng>()
        coordinates.forEach { lonLatPair ->
            if (lonLatPair.size == 2) {
                val lon = lonLatPair[0]
                val lat = lonLatPair[1]
                mapLibreLatLngs.add(LatLng(lat, lon)) // MapLibre LatLng is (latitude, longitude)
            } else {
                Log.w("ROUTE_ERROR", "Skipping invalid coordinate pair: $lonLatPair")
            }
        }

        if (mapLibreLatLngs.size < 2) {
            Log.w("ROUTE_ERROR", "Not enough valid coordinates to draw a polyline.")
            return
        }

        // --- Using Core MapLibre Polylines ---

        // 1. Remove the previous polyline if it exists
        currentRoutePolyline?.let { map.removeAnnotation(it) } // Use removeAnnotation for core polylines
        // Alternatively, to remove ALL polylines (and other annotations): map.removeAnnotations()

        // 2. Define the appearance using PolylineOptions
        val polylineOptions = PolylineOptions()
            .addAll(mapLibreLatLngs)
            .color(Color.BLUE   ) // Use Color utility class (import android.graphics.Color)
            .width(5.0f)

        // 3. Add the new polyline to the map and store a reference to it
        currentRoutePolyline = map.addPolyline(polylineOptions)
        Log.d("ROUTE_ERROR", "Route drawn using core map.addPolyline.")


        // Optional: Move camera to fit the route bounds
        // val boundsBuilder = org.maplibre.android.geometry.LatLngBounds.Builder()
        // mapLibreLatLngs.forEach { boundsBuilder.include(it) }
        // val bounds = boundsBuilder.build()
        // map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngBounds(bounds, 100)) // 100px padding
    }


    private fun setCompassPosition() {
        val uiSettings = mapLibreMap.uiSettings
        uiSettings.isCompassEnabled = true
        uiSettings.compassGravity = Gravity.TOP or Gravity.END
        uiSettings.setCompassMargins(10, 240, 10, 50) // left, top, right, bottom
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setMinUpdateIntervalMillis(500)
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        }
    }

    private fun updateUserLocation(location: Location) {



        val latLng = LatLng(location.latitude, location.longitude)

        Log.d("SafeTrackLocation", "latLng: " + latLng);



        // Move camera only on the first update OR if the user hasn't interacted with the map
        if (!isUserInteractedWithMap || isFirstLocationUpdate) {
            mapLibreMap.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(latLng)
                        .zoom(12.0)
                        .build()
                )
            )
            isFirstLocationUpdate = false
        }




        // Add or update marker for user's location
        userMarker?.let {
            mapLibreMap.removeMarker(it)
        }

        val iconFactory = IconFactory.getInstance(this)
        val userIcon = iconFactory.defaultMarker()

        userMarker = mapLibreMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("You are here")
                .icon(userIcon)
        )
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        // Stop location updates when the activity is no longer in the foreground
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}

