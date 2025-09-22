package com.example.gzingapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.appcompat.widget.Toolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.GeofenceStatusCodes
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.geojson.Point
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Polygon
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.gestures.GesturesPlugin
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONObject
import com.example.gzingapp.data.*
import com.example.gzingapp.network.ApiService
import com.example.gzingapp.network.RetrofitClient
import android.os.Build
import android.app.AlertDialog
import android.widget.EditText
import android.widget.CheckBox
import android.widget.RatingBar

class LandmarksActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var geofencingClient: GeofencingClient
    
    // Navigation Drawer Components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar
    
    // UI Components
    private var tvLocationAddress: TextView? = null
    private lateinit var tvCurrentLocation: TextView
    private lateinit var tvPinnedLocation: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvEta: TextView
    private lateinit var tvFare: TextView
    private lateinit var tvTrafficStatus: TextView
    private lateinit var trafficDot: View
    private lateinit var btnStartNavigation: MaterialButton
    private lateinit var btnCollapse: ImageView
    private lateinit var cardLocationInfo: androidx.cardview.widget.CardView
    private lateinit var rowCurrent: LinearLayout
    private lateinit var rowPinned: LinearLayout
    private lateinit var rowStats: LinearLayout
    private lateinit var bottomNavBar: LinearLayout
    
    // FABs removed
    
    // Bottom Navigation
    private lateinit var tabDashboard: LinearLayout
    private lateinit var tabRoutes: LinearLayout
    private lateinit var tabLandmarks: LinearLayout
    private lateinit var tabPlaces: LinearLayout
    
    // Map and Location
    private var mapboxMap: MapboxMap? = null
    private var currentLocation: Location? = null
    private var currentRoute: List<Point>? = null
    private var alternateRoutes: List<List<Point>>? = null
    private var currentRouteIndex = 0
    private var isMapStyleLoaded = false
    private var selectedTransportMode: String = "Car"
    private var trafficEnabled: Boolean = false
    private var isNavigating: Boolean = false
    private var hasAnnouncedArrival: Boolean = false
    private var navigationStartTime: Long = 0
    private var navTts: android.speech.tts.TextToSpeech? = null
    private var isCardCollapsed: Boolean = true
    
    // API Service
    private lateinit var apiService: ApiService
    
    // Landmarks data
    private val landmarks = mutableListOf<Landmark>()
    private var selectedLandmark: Landmark? = null
    
    // Periodic reverse geocoding
    private var reverseGeocodingHandler: android.os.Handler? = null
    private var reverseGeocodingRunnable: Runnable? = null
    
    companion object {
        private const val TAG = "LandmarksActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val NOTIFICATION_CHANNEL_ID = "landmarks_channel"
        private const val NOTIFICATION_ID = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landmarks)
        
        initializeViews()
        setupToolbar()
        setupDrawer()
        setupClickListeners()
        setupLocationServices()
        setupMap()
        setupApiService()
        logApiResponseStructure()
        loadLandmarks()
        
        // Get current location and auto-zoom to it
        getCurrentLocation()
        
        // Zoom to Antipolo-Marikina area on launch
        zoomToAntipoloMarikinaArea()
        
        // Start periodic reverse geocoding updates
        startPeriodicReverseGeocoding()
        
        // Default collapsed state: only show current & start button
        setCardCollapsed(true)
    }
    
    private fun initializeViews() {
        // Map
        mapView = findViewById(R.id.mapView)
        
        // Toolbar and Drawer
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        
        // Location Info Views
        tvLocationAddress = try { findViewById(R.id.tvLocationAddress) } catch (_: Exception) { null }
        tvCurrentLocation = findViewById(R.id.tvCurrentLocation)
        tvPinnedLocation = findViewById(R.id.tvPinnedLocation)
        tvDistance = findViewById(R.id.tvDistance)
        tvEta = findViewById(R.id.tvEta)
        tvFare = findViewById(R.id.tvFare)
        tvTrafficStatus = findViewById(R.id.tvTrafficStatus)
        trafficDot = findViewById(R.id.trafficDot)
        btnStartNavigation = findViewById(R.id.btnStartNavigation)
        btnCollapse = findViewById(R.id.btnCollapse)
        cardLocationInfo = findViewById(R.id.cardLocationInfo)
        rowCurrent = findViewById(R.id.rowCurrent)
        rowPinned = findViewById(R.id.rowPinned)
        rowStats = findViewById(R.id.rowStats)
        bottomNavBar = findViewById(R.id.bottomNavBar)
        
        // FABs removed
        
        // Bottom Navigation
        tabDashboard = findViewById(R.id.tabDashboard)
        tabRoutes = findViewById(R.id.tabRoutes)
        tabLandmarks = findViewById(R.id.tabLandmarks)
        tabPlaces = findViewById(R.id.tabPlaces)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
    }
    
    private fun setupDrawer() {
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    // Profile functionality will be implemented later
                    Toast.makeText(this, "Profile functionality coming soon", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_places -> {
                    val intent = Intent(this, PlacesActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_saved_routes -> {
                    val intent = Intent(this, NavigationRoutesActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_logout -> {
                    logout()
                }
            }
            true
        }
    }
    
    private fun setupClickListeners() {
        // FABs removed
        
        // Start Navigation Button
        btnStartNavigation.setOnClickListener { toggleNavigationMode(true) }
        
        // Collapse/Expand Location Info
        btnCollapse.setOnClickListener {
            toggleLocationCard()
        }
        
        // Bottom Navigation
        tabDashboard.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        
        tabRoutes.setOnClickListener {
            val intent = Intent(this, RoutesActivity::class.java)
            startActivity(intent)
        }
        
        tabLandmarks.setOnClickListener {
            // Already in landmarks activity
            Toast.makeText(this, "You are in Landmarks", Toast.LENGTH_SHORT).show()
        }
        
        tabPlaces.setOnClickListener {
            val intent = Intent(this, PlacesActivity::class.java)
            startActivity(intent)
        }
        
        // SOS Help FAB removed
        
        // Action Buttons - Commented out until UI is added
        // btnNavigate.setOnClickListener {
        //     navigateToPinnedLocation()
        // }
        // 
        // btnSaveRoute.setOnClickListener {
        //     saveCurrentRoute()
        // }
        // 
        // btnShareLocation.setOnClickListener {
        //     shareCurrentLocation()
        // }
        // 
        // btnClearRoute.setOnClickListener {
        //     clearRoute()
        // }
    }
    
    private fun setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    currentLocation = location
                    updateLocationUI()
                }
            }
        }
        
        if (checkLocationPermission()) {
            startLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }
    
    private fun setupMap() {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            mapboxMap = mapView.getMapboxMap()
            isMapStyleLoaded = true
            
            // Enable location component
            mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
            
            // Add map click listener for landmark interaction only
            mapView.gestures.addOnMapClickListener { point ->
                if (isNavigating) {
                    Toast.makeText(this, "Cannot interact with landmarks while navigating", Toast.LENGTH_SHORT).show()
                    return@addOnMapClickListener true
                }
                
                // Check if clicking on a landmark
                if (handleLandmarkClick(point)) {
                    return@addOnMapClickListener true
                }
                
                // If not clicking on landmark, show info
                Toast.makeText(this, "Tap on a landmark pin to see details", Toast.LENGTH_SHORT).show()
                true
            }
            
            // Add map long click listener for landmarks
            mapView.gestures.addOnMapLongClickListener { point ->
                // Map long click handling will be implemented later
                Toast.makeText(this, "Map long clicked", Toast.LENGTH_SHORT).show()
                true
            }
            
            // Display landmarks on map after style is loaded
            displayLandmarksOnMap()
        }
    }
    
    private fun setupApiService() {
        apiService = RetrofitClient.apiService
        Log.d(TAG, "API Service initialized")
    }
    
    private fun logApiResponseStructure() {
        Log.d(TAG, "=== EXPECTED API RESPONSE STRUCTURE ===")
        Log.d(TAG, "Based on: https://powderblue-pig-261057.hostingersite.com/mobile-api/endpoints/landmarks/")
        Log.d(TAG, "Response format:")
        Log.d(TAG, "  - success: Boolean")
        Log.d(TAG, "  - message: String")
        Log.d(TAG, "  - data: Object")
        Log.d(TAG, "    - landmarks: Array of landmark objects")
        Log.d(TAG, "    - pagination: Object with current_page, total_pages, etc.")
        Log.d(TAG, "    - filters: Object with categories array")
        Log.d(TAG, "    - api_info: Object with version, endpoint, description")
        Log.d(TAG, "  - timestamp: String")
        Log.d(TAG, "")
        Log.d(TAG, "Landmark object structure:")
        Log.d(TAG, "  - id: Integer")
        Log.d(TAG, "  - name: String")
        Log.d(TAG, "  - description: String")
        Log.d(TAG, "  - category: String (business, recreation, school, transport, other)")
        Log.d(TAG, "  - coordinates: Object with latitude, longitude")
        Log.d(TAG, "  - address: String")
        Log.d(TAG, "  - phone: String")
        Log.d(TAG, "  - pin_color: String (hex color)")
        Log.d(TAG, "  - opening_time: String (HH:MM:SS)")
        Log.d(TAG, "  - closing_time: String (HH:MM:SS)")
        Log.d(TAG, "  - is_open: Boolean")
        Log.d(TAG, "  - created_at: String")
        Log.d(TAG, "  - updated_at: String")
        Log.d(TAG, "  - time_info: Object with current_philippines_time, calculated_status")
        Log.d(TAG, "  - created_at_formatted: String")
        Log.d(TAG, "  - updated_at_formatted: String")
        Log.d(TAG, "=== END API RESPONSE STRUCTURE ===")
    }
    
    private fun loadLandmarks() {
        Log.d(TAG, "=== LANDMARKS API REQUEST START ===")
        Log.d(TAG, "API Endpoint: https://powderblue-pig-261057.hostingersite.com/mobile-api/endpoints/landmarks/")
        Log.d(TAG, "Request Time: ${System.currentTimeMillis()}")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting API call to getLandmarks()")
                val response = apiService.getLandmarks()
                Log.d(TAG, "API Response Code: ${response.code()}")
                Log.d(TAG, "API Response Message: ${response.message()}")
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val landmarksResponse = response.body()
                        Log.d(TAG, "API Response Body: $landmarksResponse")
                        
                        if (landmarksResponse?.success == true) {
                            Log.d(TAG, "API Success: ${landmarksResponse.message}")
                            Log.d(TAG, "API Data: ${landmarksResponse.data}")
                            Log.d(TAG, "API Pagination: ${landmarksResponse.data.pagination}")
                            Log.d(TAG, "API Filters: ${landmarksResponse.data.filters}")
                            Log.d(TAG, "API Info: ${landmarksResponse.data.apiInfo}")
                            
                            landmarks.clear()
                            landmarks.addAll(landmarksResponse.data.landmarks)
                            
                            // Log each landmark
                            landmarks.forEachIndexed { index, landmark ->
                                Log.d(TAG, "Landmark $index: ID=${landmark.id}, Name=${landmark.name}, Category=${landmark.category}")
                                Log.d(TAG, "  Coordinates: Lat=${landmark.coordinates.latitude}, Lng=${landmark.coordinates.longitude}")
                                Log.d(TAG, "  Address: ${landmark.address}")
                                Log.d(TAG, "  Pin Color: ${landmark.pinColor}")
                                Log.d(TAG, "  Opening Hours: ${landmark.openingTime} - ${landmark.closingTime}")
                                Log.d(TAG, "  Is Open: ${landmark.isOpen}")
                                Log.d(TAG, "  Time Info: ${landmark.timeInfo}")
                            }
                            
                            // Display landmarks on map if style is already loaded
                            if (isMapStyleLoaded) {
                                Log.d(TAG, "Map style loaded, displaying landmarks on map")
                                displayLandmarksOnMap()
                            } else {
                                Log.d(TAG, "Map style not loaded yet, landmarks will be displayed when style loads")
                            }
                            
                            Log.d(TAG, "Successfully loaded ${landmarks.size} landmarks from API")
                            Toast.makeText(this@LandmarksActivity, "Loaded ${landmarks.size} landmarks", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e(TAG, "API returned error: ${landmarksResponse?.message}")
                            Toast.makeText(this@LandmarksActivity, "Failed to load landmarks: ${landmarksResponse?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e(TAG, "HTTP error: ${response.code()}")
                        Log.e(TAG, "HTTP error message: ${response.message()}")
                        Log.e(TAG, "HTTP error body: ${response.errorBody()?.string()}")
                        Toast.makeText(this@LandmarksActivity, "Failed to load landmarks: HTTP ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during API call", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LandmarksActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        Log.d(TAG, "=== LANDMARKS API REQUEST END ===")
    }
    
    private fun displayLandmarksOnMap() {
        if (!isMapStyleLoaded) {
            Log.d(TAG, "Map style not loaded, skipping landmark display")
            return
        }
        
        Log.d(TAG, "=== DISPLAYING LANDMARKS ON MAP ===")
        Log.d(TAG, "Total landmarks to display: ${landmarks.size}")
        Log.d(TAG, "Map style loaded: $isMapStyleLoaded")
        
        if (landmarks.isEmpty()) {
            Log.w(TAG, "No landmarks to display")
            return
        }
        
        // Create features for landmarks with properties for text labels and icons
        val landmarkFeatures = landmarks.mapIndexed { index, landmark ->
            Feature.fromGeometry(
                Point.fromLngLat(landmark.coordinates.longitude, landmark.coordinates.latitude)
            ).apply {
                addStringProperty("name", landmark.name)
                addStringProperty("description", landmark.description)
                addStringProperty("category", landmark.category)
                addStringProperty("address", landmark.address)
                addStringProperty("phone", landmark.phone)
                addStringProperty("pin_color", landmark.pinColor)
                addStringProperty("icon_name", "landmark-pin-$index")
                addNumberProperty("id", landmark.id)
            }
        }
        
        Log.d(TAG, "Created ${landmarkFeatures.size} landmark features")
        
        try {
            Log.d(TAG, "Starting to add landmarks to existing style")
            Log.d(TAG, "Style is ready: ${mapboxMap?.getStyle()?.isStyleLoaded()}")
            
            mapboxMap?.getStyle { style ->
                // Remove existing landmarks if they exist (layers first, then sources)
                Log.d(TAG, "Removing existing landmark layers and sources")
                if (style.styleLayerExists("landmarks-text-layer")) {
                    style.removeStyleLayer("landmarks-text-layer")
                    Log.d(TAG, "Removed landmarks-text-layer")
                }
                if (style.styleLayerExists("landmarks-layer")) {
                    style.removeStyleLayer("landmarks-layer")
                    Log.d(TAG, "Removed landmarks-layer")
                }
                if (style.styleSourceExists("landmarks")) {
                    style.removeStyleSource("landmarks")
                    Log.d(TAG, "Removed landmarks source")
                }
                
                // Add individual landmark sources and layers with custom colored pins
                landmarks.forEachIndexed { index, landmark ->
                    val pinBitmap = createColoredPinBitmap(landmark.pinColor)
                    if (pinBitmap != null) {
                        Log.d(TAG, "Adding custom pin for ${landmark.name} with color ${landmark.pinColor}")
                        
                        // Add pin image to style
                        style.addImage("landmark-pin-$index", pinBitmap)
                        
                        // Create individual feature for this landmark
                        val landmarkFeature = Feature.fromGeometry(
                            Point.fromLngLat(landmark.coordinates.longitude, landmark.coordinates.latitude)
                        ).apply {
                            addStringProperty("name", landmark.name)
                            addStringProperty("description", landmark.description)
                            addStringProperty("category", landmark.category)
                            addStringProperty("address", landmark.address)
                            addStringProperty("phone", landmark.phone)
                            addStringProperty("pin_color", landmark.pinColor)
                            addNumberProperty("id", landmark.id)
                        }
                        
                        // Add individual source for this landmark
                        style.addSource(
                            geoJsonSource("landmark-source-$index") {
                                geometry(landmarkFeature.geometry()!!)
                            }
                        )
                        
                        // Add individual layer for this landmark
                        style.addLayer(
                            symbolLayer("landmark-layer-$index", "landmark-source-$index") {
                                iconImage("landmark-pin-$index")
                                iconSize(1.5)
                                iconAllowOverlap(true)
                                iconIgnorePlacement(true)
                                iconAnchor(com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor.BOTTOM)
                            }
                        )
                        
                        Log.d(TAG, "Successfully added landmark ${landmark.name} with custom colored pin")
                    }
                }
                
                // Add text labels for each landmark
                landmarks.forEachIndexed { index, landmark ->
                    style.addLayer(
                        symbolLayer("landmark-text-$index", "landmark-source-$index") {
                            textField(landmark.name)
                            textSize(16.0)
                            textColor("#000000")
                            // Stronger white halo to simulate white background
                            textHaloColor("#FFFFFF")
                            textHaloWidth(4.0)
                            textHaloBlur(0.7)
                            // Slightly higher above the pin
                            textOffset(listOf(0.0, -2.8))
                            textAllowOverlap(true)
                            textIgnorePlacement(true)
                        }
                    )
                }
                Log.d(TAG, "Successfully added text labels for all landmarks")
                
                Log.d(TAG, "Successfully added ${landmarks.size} landmarks to map with custom colored pins")
                
                // Force map repaint to ensure visibility
                mapView.mapboxMap.triggerRepaint()
                Log.d(TAG, "Triggered map repaint")
                
                // Landmark click handling is now integrated into the main map click listener
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add landmarks to map", e)
            e.printStackTrace()
        }
    }
    
    private fun showAddLandmarkDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_landmark, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Landmark")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                // Handle landmark addition
                Toast.makeText(this, "Landmark added successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun showPluckDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pluck, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Pluck Location")
            .setView(dialogView)
            .setPositiveButton("Pluck") { _, _ ->
                // Handle pluck functionality
                Toast.makeText(this, "Location plucked", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun handleLandmarkClick(point: Point): Boolean {
        Log.d(TAG, "=== HANDLING LANDMARK CLICK ===")
        Log.d(TAG, "Click coordinates: Lat=${point.latitude()}, Lng=${point.longitude()}")
        
        // Find the closest landmark to the clicked point
        var closestLandmark: Landmark? = null
        var minDistance = Double.MAX_VALUE
        
        landmarks.forEach { landmark ->
            val landmarkPoint = Point.fromLngLat(landmark.coordinates.longitude, landmark.coordinates.latitude)
            val distance = calculateDistance(point, landmarkPoint)
            Log.d(TAG, "Distance to ${landmark.name}: ${distance}m")
            
            // Use a more reasonable click distance (about 50 meters)
            if (distance < minDistance && distance < 50.0) {
                minDistance = distance
                closestLandmark = landmark
            }
        }
        
        if (closestLandmark != null) {
            Log.d(TAG, "Found closest landmark: ${closestLandmark.name} at distance: ${minDistance}m")
            
            // Add visual feedback for pin click
            Toast.makeText(this, "📍 ${closestLandmark.name}", Toast.LENGTH_SHORT).show()
            
            // Show the landmark details modal
            showLandmarkDetailsModal(closestLandmark)
            return true
        } else {
            Log.d(TAG, "No landmark found within click distance")
        }
        
        return false
    }
    
    private fun calculateDistance(point1: Point, point2: Point): Double {
        val lat1 = Math.toRadians(point1.latitude())
        val lat2 = Math.toRadians(point2.latitude())
        val deltaLat = Math.toRadians(point2.latitude() - point1.latitude())
        val deltaLng = Math.toRadians(point2.longitude() - point1.longitude())
        
        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return 6371000 * c // Earth's radius in meters
    }
    
    private fun showLandmarkDetailsModal(landmark: Landmark) {
        Log.d(TAG, "=== SHOWING LANDMARK DETAILS MODAL ===")
        Log.d(TAG, "Landmark: ${landmark.name}")
        Log.d(TAG, "Category: ${landmark.category}")
        Log.d(TAG, "Address: ${landmark.address}")
        Log.d(TAG, "Coordinates: ${landmark.coordinates.latitude}, ${landmark.coordinates.longitude}")
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_landmark_info, null)
        
        // Update dialog content with landmark data
        dialogView.findViewById<TextView>(R.id.tvLandmarkName).text = landmark.name
        dialogView.findViewById<TextView>(R.id.tvLandmarkDescription).text = landmark.description.ifEmpty { "No description available" }
        dialogView.findViewById<TextView>(R.id.tvLandmarkCategory).text = landmark.category.replaceFirstChar { it.uppercase() }
        dialogView.findViewById<TextView>(R.id.tvLandmarkAddress).text = landmark.address
        dialogView.findViewById<TextView>(R.id.tvLandmarkPhone).text = if (landmark.phone.isNotEmpty()) landmark.phone else "No phone number"
        dialogView.findViewById<TextView>(R.id.tvLandmarkCoordinates).text = 
            "Coordinates: ${landmark.coordinates.latitude}, ${landmark.coordinates.longitude}"
        
        // Add opening hours if available
        val openingHours = "${landmark.openingTime} - ${landmark.closingTime}"
        dialogView.findViewById<TextView>(R.id.tvLandmarkHours).text = "Hours: $openingHours"
        
        // Add status (open/closed)
        val status = if (landmark.isOpen) "Open" else "Closed"
        dialogView.findViewById<TextView>(R.id.tvLandmarkStatus).text = "Status: $status"
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("📍 ${landmark.name}")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()
        
        // Set up start navigation button
        dialogView.findViewById<MaterialButton>(R.id.btnNavigateToLandmark).setOnClickListener {
            Log.d(TAG, "Start navigation button clicked for ${landmark.name}")
            startNavigationToLandmark(landmark)
            dialog.dismiss()
        }
        
        // Set up share button
        dialogView.findViewById<MaterialButton>(R.id.btnShareLandmark).setOnClickListener {
            Log.d(TAG, "Share button clicked for ${landmark.name}")
            shareLandmark(landmark)
            dialog.dismiss()
        }
        
        dialog.show()
        Log.d(TAG, "Landmark details modal displayed successfully")
    }
    
    private fun startNavigationToLandmark(landmark: Landmark) {
        Log.d(TAG, "=== STARTING NAVIGATION TO LANDMARK ===")
        Log.d(TAG, "Destination: ${landmark.name}")
        Log.d(TAG, "Coordinates: ${landmark.coordinates.latitude}, ${landmark.coordinates.longitude}")
        
        // Show transport mode dialog for navigation
        showTransportModeDialog { mode ->
            selectedTransportMode = mode
            Log.d(TAG, "Transport mode selected for navigation: $mode")
            
            // Launch MapActivity with landmark coordinates and details
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("selected_place_lat", landmark.coordinates.latitude)
                putExtra("selected_place_lng", landmark.coordinates.longitude)
                putExtra("selected_place_name", landmark.name)
                putExtra("selected_place_address", landmark.address)
                putExtra("transport_mode", mode)
                putExtra("auto_start_navigation", true) // Auto-start navigation
            }
            
            Log.d(TAG, "Launching MapActivity with landmark data:")
            Log.d(TAG, "  - Name: ${landmark.name}")
            Log.d(TAG, "  - Address: ${landmark.address}")
            Log.d(TAG, "  - Coordinates: ${landmark.coordinates.latitude}, ${landmark.coordinates.longitude}")
            Log.d(TAG, "  - Transport Mode: $mode")
            Log.d(TAG, "  - Auto Start Navigation: true")
            
            startActivity(intent)
            Toast.makeText(this, "Starting navigation to ${landmark.name}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToLandmark(landmark: Landmark) {
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra("navigation_mode", true)
            putExtra("end_lat", landmark.coordinates.latitude)
            putExtra("end_lng", landmark.coordinates.longitude)
            putExtra("destination_name", landmark.name)
        }
        startActivity(intent)
    }
    
    private fun shareLandmark(landmark: Landmark) {
        val shareText = """
            ${landmark.name}
            ${landmark.address}
            Category: ${landmark.category}
            Coordinates: ${landmark.coordinates.latitude}, ${landmark.coordinates.longitude}
            ${if (landmark.phone.isNotEmpty()) "Phone: ${landmark.phone}" else ""}
        """.trimIndent()
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share Landmark"))
    }
    
    private fun showLandmarkInfoDialog(point: Point) {
        // This method is now handled by setupLandmarkClickListener
        Toast.makeText(this, "Tap on a landmark pin to see details", Toast.LENGTH_SHORT).show()
    }
    
    private fun cycleAlternateRoutes() {
        alternateRoutes?.let { routes ->
            if (routes.isNotEmpty()) {
                currentRouteIndex = (currentRouteIndex + 1) % routes.size
                displayRoute(routes[currentRouteIndex])
            }
        }
    }
    
    private fun displayRoute(route: List<Point>) {
        // Route display will be implemented later
        Log.d(TAG, "Displaying route with ${route.size} points")
    }
    
    
    private fun calculateRoute(origin: Point, destination: Point) {
        // Route calculation will be implemented later
        Toast.makeText(this, "Route calculation not yet implemented", Toast.LENGTH_SHORT).show()
    }
    
    private fun saveCurrentRoute() {
        // Route saving will be implemented later
        Toast.makeText(this, "Route saving not yet implemented", Toast.LENGTH_SHORT).show()
    }
    
    private fun shareCurrentLocation() {
        currentLocation?.let { location ->
            val locationText = "My current location: ${location.latitude}, ${location.longitude}"
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, locationText)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share Location"))
        }
    }
    
    
    private fun updateLocationInfo(point: Point) {
        // Update UI with location information
        tvPinnedLocation?.text = "Pinned: ${point.latitude()}, ${point.longitude()}"
    }
    
    private fun updateLocationUI() {
        currentLocation?.let { location ->
            tvCurrentLocation.text = "Current: ${location.latitude}, ${location.longitude}"
        }
    }
    
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    
    private fun startLocationUpdates() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }
    
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    private fun logout() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        startPeriodicReverseGeocoding()
    }
    
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        stopPeriodicReverseGeocoding()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        stopPeriodicReverseGeocoding()
    }
    
    // Card collapse functionality from MapActivity
    private fun setCardCollapsed(collapsed: Boolean) {
        isCardCollapsed = collapsed
        // When collapsed: show only current row and start button; hide chips and detail rows
        rowPinned.visibility = if (collapsed) View.GONE else View.VISIBLE
        rowStats.visibility = if (collapsed) View.GONE else View.VISIBLE
        rowCurrent.visibility = View.VISIBLE
        btnStartNavigation.visibility = View.VISIBLE
        btnCollapse.setImageResource(if (collapsed) R.drawable.ic_expand_more else R.drawable.ic_expand_less)
    }

    private fun toggleLocationCard() { 
        setCardCollapsed(!isCardCollapsed) 
    }
    
    // Navigation mode functionality from MapActivity
    private fun toggleNavigationMode(enable: Boolean) {
        isNavigating = enable
        
        if (enable) {
            // Disable drawer and bottom navigation during navigation
            disableDrawerAndBottomNav()
        } else {
            // Re-enable drawer and bottom navigation after navigation stops
            enableDrawerAndBottomNav()
        }
        
        btnStartNavigation.text = if (enable) "Stop Navigation" else "Start Navigation"
        btnStartNavigation.setOnClickListener { toggleNavigationMode(!isNavigating) }
        if (enable) {
            // Reset arrival flag for new navigation session
            hasAnnouncedArrival = false
            navigationStartTime = System.currentTimeMillis() // Record navigation start time
            try { 
                android.util.Log.d("NavigationMode", "Starting navigation - hasAnnouncedArrival reset to false") 
            } catch (_: Exception) { }
        } else {
            // Stop TTS
            try { navTts?.stop(); navTts?.shutdown() } catch (_: Exception) { }
            navTts = null
        }
        Toast.makeText(this, if (enable) "Navigating..." else "Navigation stopped", Toast.LENGTH_SHORT).show()
    }
    
    private fun disableDrawerAndBottomNav() {
        try {
            // Disable drawer layout
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            
            // Disable toolbar navigation icon (hamburger menu)
            toolbar.navigationIcon = null
            toolbar.setNavigationOnClickListener(null)
            
            // Hide bottom navigation bar
            bottomNavBar.visibility = View.GONE
            
            // Disable individual bottom navigation tabs
            tabDashboard.isEnabled = false
            tabRoutes.isEnabled = false
            tabLandmarks.isEnabled = false
            tabPlaces.isEnabled = false
        } catch (e: Exception) {
            Log.e("LandmarksActivity", "Error disabling drawer and bottom navigation", e)
        }
    }
    
    private fun enableDrawerAndBottomNav() {
        try {
            // Re-enable drawer layout
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            
            // Re-enable toolbar navigation icon (hamburger menu)
            toolbar.setNavigationIcon(R.drawable.ic_menu)
            toolbar.setNavigationOnClickListener {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START)
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            
            // Show bottom navigation bar
            bottomNavBar.visibility = View.VISIBLE
            
            // Re-enable individual bottom navigation tabs
            tabDashboard.isEnabled = true
            tabRoutes.isEnabled = true
            tabLandmarks.isEnabled = true
            tabPlaces.isEnabled = true
        } catch (e: Exception) {
            Log.e("LandmarksActivity", "Error enabling drawer and bottom navigation", e)
        }
    }
    
    
    private fun showTransportModeDialog(onSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_transport_mode, null)
        dialog.setContentView(view)
        val rg = view.findViewById<android.widget.RadioGroup>(R.id.rgTransport)
        when (selectedTransportMode) {
            "Car" -> rg.check(R.id.rbCar)
            "Walk" -> rg.check(R.id.rbWalk)
            "Motor" -> rg.check(R.id.rbMotor)
        }
        view.findViewById<android.widget.Button>(R.id.btnTransportApply).setOnClickListener {
            val selId = rg.checkedRadioButtonId
            val mode = when (selId) {
                R.id.rbCar -> "Car"
                R.id.rbWalk -> "Walk"
                R.id.rbMotor -> "Motor"
                else -> "Car"
            }
            dialog.dismiss()
            onSelected(mode)
        }
        dialog.show()
    }
    
    
    private suspend fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        Log.d(TAG, "=== GET ADDRESS FROM COORDINATES ===")
        Log.d(TAG, "Input coordinates: Lat=$lat, Lng=$lng")
        Log.d(TAG, "Using OpenStreetMap Nominatim API for reverse geocoding")
        
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting reverse geocoding API call")
                // Using OpenStreetMap Nominatim API for reverse geocoding (free)
                val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&zoom=18&addressdetails=1"
                Log.d(TAG, "Reverse geocoding URL: $url")
                val connection = URL(url).openConnection()
                connection.setRequestProperty("User-Agent", "GzingApp/1.0")
                Log.d(TAG, "Making HTTP request to Nominatim API")
                
                val response = connection.getInputStream().bufferedReader().use { it.readText() }
                Log.d(TAG, "Received response from Nominatim API")
                Log.d(TAG, "Response length: ${response.length} characters")
                val jsonObject = JSONObject(response)
                Log.d(TAG, "Parsed JSON response successfully")
                
                val displayName = jsonObject.optString("display_name", "")
                Log.d(TAG, "Display name from API: $displayName")
                val address = jsonObject.optJSONObject("address")
                Log.d(TAG, "Address object from API: $address")
                
                if (displayName.isNotEmpty()) {
                    Log.d(TAG, "Display name is not empty, formatting address")
                    // Format the address nicely
                    val parts = displayName.split(", ")
                    Log.d(TAG, "Address parts count: ${parts.size}")
                    Log.d(TAG, "Address parts: $parts")
                    if (parts.size >= 3) {
                        val formattedAddress = "${parts[0]}, ${parts[1]}, ${parts[2]}"
                        Log.d(TAG, "Formatted address: $formattedAddress")
                        formattedAddress
                    } else {
                        Log.d(TAG, "Using full display name: $displayName")
                        displayName
                    }
                } else {
                    Log.w(TAG, "Display name is empty, using coordinates")
                    "Location: $lat, $lng"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in reverse geocoding", e)
                Log.w(TAG, "Using fallback coordinates: $lat, $lng")
                "Location: $lat, $lng"
            }
        }
    }
    
    
    private fun updateRoute(startPoint: Point, endPoint: Point) {
        if (!isMapStyleLoaded) return
        
        Log.d("LandmarksActivity", "Updating route with transportation mode: $selectedTransportMode")
        
        // For now, just show a toast - route calculation will be implemented later
        Toast.makeText(this, "Route calculation will be implemented", Toast.LENGTH_SHORT).show()
    }
    
    private fun redrawAllLayers() {
        if (!isMapStyleLoaded) {
            Log.d(TAG, "Map style not loaded, skipping layer redraw")
            return
        }
        
        Log.d(TAG, "=== REDRAWING ALL LAYERS ===")
        
        mapboxMap?.getStyle { style ->
            // Re-display landmarks
            if (landmarks.isNotEmpty()) {
                displayLandmarksOnMap()
            }
            
            // Re-display user location if available
            currentLocation?.let { location ->
                val point = Point.fromLngLat(location.longitude, location.latitude)
                // Add user location layer
                addUserLocationLayer(style, point)
            }
            
            // Re-display route if available
            currentRoute?.let { route ->
                addRouteLayer(style, route)
            }
            
            Log.d(TAG, "All layers redrawn successfully")
        }
    }
    
    private fun addUserLocationLayer(style: Style, point: Point) {
        try {
            // Remove existing user location layer if it exists
            if (style.styleLayerExists("user-location-layer")) {
                style.removeStyleLayer("user-location-layer")
            }
            if (style.styleSourceExists("user-location")) {
                style.removeStyleSource("user-location")
            }
            
            // Add user location source
            style.addSource(
                geoJsonSource("user-location") {
                    geometry(point)
                }
            )
            
            // Add user location layer
            style.addLayer(
                circleLayer("user-location-layer", "user-location") {
                    circleColor("#007AFF")
                    circleRadius(8.0)
                    circleStrokeColor("#FFFFFF")
                    circleStrokeWidth(3.0)
                }
            )
            
            Log.d(TAG, "User location layer added")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding user location layer", e)
        }
    }
    
    
    private fun addRouteLayer(style: Style, route: List<Point>) {
        try {
            // Remove existing route layer if it exists
            if (style.styleLayerExists("route-layer")) {
                style.removeStyleLayer("route-layer")
            }
            if (style.styleSourceExists("route")) {
                style.removeStyleSource("route")
            }
            
            if (route.size >= 2) {
                // Create line string from route points
                val lineString = LineString.fromLngLats(route)
                
                // Add route source
                style.addSource(
                    geoJsonSource("route") {
                        geometry(lineString)
                    }
                )
                
                // Add route layer
                style.addLayer(
                    lineLayer("route-layer", "route") {
                        lineColor("#007AFF")
                        lineWidth(4.0)
                        lineCap(LineCap.ROUND)
                        lineJoin(LineJoin.ROUND)
                    }
                )
                
                Log.d(TAG, "Route layer added with ${route.size} points")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding route layer", e)
        }
    }
    
    // Missing methods referenced in click listeners
    private fun applyCurrentStyleAndRedraw() {
        if (!::mapView.isInitialized) return
        
        Log.d(TAG, "=== APPLYING CURRENT STYLE AND REDRAWING ===")
        Log.d(TAG, "Traffic enabled: $trafficEnabled")
        
        // Load appropriate map style based on traffic setting
        val styleUri = if (trafficEnabled) {
            Style.MAPBOX_STREETS // Use streets style for traffic
        } else {
            Style.MAPBOX_STREETS // Use same style but without traffic layer
        }
        
        mapView.mapboxMap.loadStyleUri(styleUri) { style ->
            isMapStyleLoaded = true
            Log.d(TAG, "Style loaded: $styleUri")
            
            // Add or remove traffic layer based on traffic setting
            if (trafficEnabled) {
                addTrafficLayer(style)
            } else {
                removeTrafficLayer(style)
            }
            
            // Re-display landmarks after style change
            displayLandmarksOnMap()
            
            // Re-display user location
            currentLocation?.let { location ->
                val point = Point.fromLngLat(location.longitude, location.latitude)
                updateUserLocation(point)
            }
            
            // Re-display route if exists
            if (currentRoute != null && currentLocation != null) {
                val currentPoint = Point.fromLngLat(currentLocation!!.longitude, currentLocation!!.latitude)
                // Route display will be handled by the navigation system
            }
            
            Log.d(TAG, "Style application and redraw completed")
        }
    }
    
    private fun addTrafficLayer(style: Style) {
        try {
            Log.d(TAG, "Adding traffic layer")
            // Add traffic source
            style.addSource(
                geoJsonSource("traffic") {
                    url("mapbox://mapbox.mapbox-traffic-v1")
                }
            )
            
            // Add traffic layer
            style.addLayer(
                lineLayer("traffic-layer", "traffic") {
                    lineColor("#FF0000") // Red for traffic
                    lineWidth(3.0)
                    lineOpacity(0.8)
                }
            )
            
            Log.d(TAG, "Traffic layer added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding traffic layer", e)
        }
    }
    
    private fun removeTrafficLayer(style: Style) {
        try {
            Log.d(TAG, "Removing traffic layer")
            if (style.styleLayerExists("traffic-layer")) {
                style.removeStyleLayer("traffic-layer")
            }
            if (style.styleSourceExists("traffic")) {
                style.removeStyleSource("traffic")
            }
            Log.d(TAG, "Traffic layer removed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing traffic layer", e)
        }
    }
    
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // No location permission, show default location and request permission
            showDefaultLocation()
            requestLocationPermission()
            return
        }
        
        // Get last known location first
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val point = Point.fromLngLat(location.longitude, location.latitude)
                    updateUserLocation(point)
                    performReverseGeocoding(point)
                } else {
                    // Location is null, show default location
                    showDefaultLocation()
                }
            }
            .addOnFailureListener {
                // Failed to get location, show default location
                showDefaultLocation()
                Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun updateUserLocation(point: Point) {
        // Convert Point to Location for currentLocation
        val location = android.location.Location("gps")
        location.latitude = point.latitude()
        location.longitude = point.longitude()
        currentLocation = location
        
        Log.d(TAG, "=== AUTO-ZOOM TO CURRENT LOCATION ===")
        Log.d(TAG, "Current location: Lat=${point.latitude()}, Lng=${point.longitude()}")
        Log.d(TAG, "Map style loaded: $isMapStyleLoaded")
        
        if (!isMapStyleLoaded) {
            Log.d(TAG, "Map style not loaded, skipping auto-zoom")
            return
        }
        
        // Update user location by redrawing layers
        redrawAllLayers()
        
        // Auto-center functionality removed - user can manually center using FAB
    }
    
    private fun performReverseGeocoding(point: Point) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val address = getAddressFromCoordinates(point.latitude(), point.longitude())
                withContext(Dispatchers.Main) {
                    tvLocationAddress?.text = address
                    tvCurrentLocation.text = address
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val fallback = "Location: ${point.latitude()}, ${point.longitude()}"
                    tvLocationAddress?.text = fallback
                    tvCurrentLocation.text = fallback
                    Toast.makeText(this@LandmarksActivity, "Failed to get address: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showDefaultLocation() {
        // Default to Antipolo-Marikina area, Philippines
        // Coordinates between Antipolo and Marikina for better coverage
        val defaultPoint = Point.fromLngLat(121.1750, 14.6500)
        
        // Move camera to default location with appropriate zoom for Antipolo-Marikina area
        mapView.mapboxMap.setCamera(
            com.mapbox.maps.CameraOptions.Builder()
                .center(defaultPoint)
                .zoom(12.0) // Good zoom level to show both Antipolo and Marikina
                .build()
        )
        
        // Show default location info
        tvLocationAddress?.text = "Antipolo-Marikina Area, Philippines"
        tvCurrentLocation.text = "Antipolo-Marikina Area, Philippines"
    }
    
    // Emergency SMS method removed
    
    // Zoom to Antipolo-Marikina area
    private fun zoomToAntipoloMarikinaArea() {
        Log.d(TAG, "=== ZOOMING TO ANTIPOLO-MARIKINA AREA ===")
        
        // Coordinates that cover both Antipolo and Marikina
        val antipoloMarikinaPoint = Point.fromLngLat(121.1750, 14.6500)
        
        Log.d(TAG, "Setting camera to Antipolo-Marikina area: Lat=${antipoloMarikinaPoint.latitude()}, Lng=${antipoloMarikinaPoint.longitude()}")
        
        // Set camera to Antipolo-Marikina area with appropriate zoom
        mapView.mapboxMap.setCamera(
            com.mapbox.maps.CameraOptions.Builder()
                .center(antipoloMarikinaPoint)
                .zoom(12.0) // Good zoom level to show both Antipolo and Marikina
                .build()
        )
        
        Log.d(TAG, "Successfully zoomed to Antipolo-Marikina area")
    }
    
    // Periodic reverse geocoding methods
    private fun startPeriodicReverseGeocoding() {
        Log.d(TAG, "Starting periodic reverse geocoding every 5 seconds")
        
        reverseGeocodingHandler = android.os.Handler(android.os.Looper.getMainLooper())
        reverseGeocodingRunnable = object : Runnable {
            override fun run() {
                updateCurrentLocationName()
                // Schedule next update in 5 seconds
                reverseGeocodingHandler?.postDelayed(this, 5000)
            }
        }
        
        // Start the first update immediately
        reverseGeocodingHandler?.post(reverseGeocodingRunnable!!)
    }
    
    private fun stopPeriodicReverseGeocoding() {
        Log.d(TAG, "Stopping periodic reverse geocoding")
        reverseGeocodingHandler?.removeCallbacks(reverseGeocodingRunnable!!)
        reverseGeocodingHandler = null
        reverseGeocodingRunnable = null
    }
    
    private fun updateCurrentLocationName() {
        currentLocation?.let { location ->
            val point = Point.fromLngLat(location.longitude, location.latitude)
            Log.d(TAG, "=== PERIODIC REVERSE GEOCODING UPDATE ===")
            Log.d(TAG, "Updating location name for: Lat=${point.latitude()}, Lng=${point.longitude()}")
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val address = getAddressFromCoordinates(point.latitude(), point.longitude())
                    withContext(Dispatchers.Main) {
                        tvCurrentLocation.text = address
                        Log.d(TAG, "Updated current location name: $address")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        val fallback = "Location: ${point.latitude()}, ${point.longitude()}"
                        tvCurrentLocation.text = fallback
                        Log.e(TAG, "Error updating location name: ${e.message}")
                    }
                }
            }
        } ?: run {
            Log.d(TAG, "No current location available for reverse geocoding")
        }
    }
    
    // Convert vector drawable to Bitmap for Mapbox symbol image
    private fun vectorToBitmap(drawableResId: Int): android.graphics.Bitmap? {
        val drawable: android.graphics.drawable.Drawable =
            androidx.core.content.res.ResourcesCompat.getDrawable(resources, drawableResId, theme)
                ?: return null
        val wrappedDrawable = androidx.core.graphics.drawable.DrawableCompat.wrap(drawable).mutate()
        val width = if (wrappedDrawable.intrinsicWidth > 0) wrappedDrawable.intrinsicWidth else 96
        val height = if (wrappedDrawable.intrinsicHeight > 0) wrappedDrawable.intrinsicHeight else 96
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        wrappedDrawable.setBounds(0, 0, canvas.width, canvas.height)
        wrappedDrawable.draw(canvas)
        return bitmap
    }
    
    // Create colored pin bitmap based on API color
    private fun createColoredPinBitmap(colorHex: String): android.graphics.Bitmap? {
        return try {
            Log.d(TAG, "Creating colored pin bitmap with color: $colorHex")
            
            // Get the original pin drawable
            val originalDrawable = androidx.core.content.res.ResourcesCompat.getDrawable(
                resources, R.drawable.ic_custom_pin, theme
            ) ?: return null
            
            // Create a mutable copy
            val coloredDrawable = originalDrawable.mutate()
            
            // Apply color tint to the pin body (the main part)
            androidx.core.graphics.drawable.DrawableCompat.setTint(coloredDrawable, android.graphics.Color.parseColor(colorHex))
            
            // Create bitmap
            val width = 72 // 36dp * 2 for better quality and bigger size
            val height = 96 // 48dp * 2 for better quality and bigger size
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            
            // Draw the colored pin
            coloredDrawable.setBounds(0, 0, width, height)
            coloredDrawable.draw(canvas)
            
            Log.d(TAG, "Successfully created colored pin bitmap")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error creating colored pin bitmap", e)
            null
        }
    }
}
        