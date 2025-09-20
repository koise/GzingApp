package com.example.gzingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.gzingapp.data.RouteDetails
import com.example.gzingapp.data.RouteDetailsApiResponse
import com.example.gzingapp.data.RouteDetailsPin
import com.example.gzingapp.data.RouteDetailsMapDetails
import com.example.gzingapp.network.ApiService
import com.example.gzingapp.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.utils.PolylineUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import android.content.Context
import android.app.AlertDialog
import android.widget.EditText
import android.widget.CheckBox
import android.widget.RatingBar
import com.example.gzingapp.repository.NavigationRouteRepository
import com.example.gzingapp.data.CreateNavigationRouteRequest
import kotlinx.coroutines.withContext

class RoutesMapsActivity : AppCompatActivity() {
    
    
    private lateinit var toolbar: Toolbar
    private lateinit var mapView: MapView
    private lateinit var fabMyLocation: FloatingActionButton
    private lateinit var fabSosHelp: com.example.gzingapp.ui.SosHoldButton
    // Traffic FAB removed - traffic info now always visible
    private lateinit var loadingIndicator: ProgressBar
    
    // Routes Info Card views (from view_routes_info_card.xml)
    private lateinit var routesInfoCard: android.widget.LinearLayout
    private lateinit var btnCollapseRoutes: android.widget.ImageView
    private lateinit var routesContentContainer: android.widget.LinearLayout
    private lateinit var tvCurrentToWaypoint: TextView
    private lateinit var tvDestinationInfo: TextView
    private lateinit var tvDistanceInfo: TextView
    private lateinit var tvEtaInfo: TextView
    private lateinit var tvFareInfo: TextView
    private lateinit var tvTrafficStatus: TextView
    private lateinit var trafficDot: View
    private lateinit var tvWaypointCount: TextView
    private lateinit var btnStartNavigationRoutes: MaterialButton
    private lateinit var btnStopNavigationRoutes: MaterialButton
    
    // Navigation Drawer Components
    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    private lateinit var navigationView: com.google.android.material.navigation.NavigationView
    private lateinit var actionBarDrawerToggle: androidx.appcompat.app.ActionBarDrawerToggle
    
    // Bottom Navigation
    private lateinit var tabDashboard: android.widget.LinearLayout
    private lateinit var tabRoutes: android.widget.LinearLayout
    private lateinit var tabPlaces: android.widget.LinearLayout
    private lateinit var tabLandmarks: android.widget.LinearLayout
    private lateinit var bottomNavBar: android.widget.LinearLayout
    
    
    private lateinit var apiService: ApiService
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var navigationRouteRepository: NavigationRouteRepository
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                enableLocationPuck()
                getCurrentLocationAndRefreshRoute()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                enableLocationPuck()
                getCurrentLocationAndRefreshRoute()
            }
            else -> {
                Toast.makeText(this, "Location permission is required to show your current location", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private var routeId: Int = 0
    private var routeName: String = ""
    private var currentLat: Double = 0.0
    private var currentLng: Double = 0.0
    private var endLat: Double = 0.0
    private var endLng: Double = 0.0
    private var destName: String = ""
    private var selectedRoute: RouteDetails? = null
    private var closestWaypoint: RouteDetailsPin? = null
    private var mapStyleLoaded: Boolean = false
    // Traffic is now always enabled - no toggle needed
    
    // Waypoint progression tracking
    private var currentWaypointIndex: Int = 0
    private var waypointSequence: List<RouteDetailsPin> = emptyList()
    private var destinationPin: RouteDetailsPin? = null
    
    // Tooltip related
    private var currentTooltip: View? = null
    private var currentSelectedPin: RouteDetailsPin? = null
    
    // Navigation state variables
    private var isNavigationActive: Boolean = false
    private var navigationRoute: List<Point>? = null
    private var currentNavigationStep: Int = 0
    private var navigationInstructions: List<String> = emptyList()
    private var navigationDistance: Double = 0.0
    private var navigationDuration: Int = 0
    private var lastKnownLocation: android.location.Location? = null
    private var navigationStartTime: Long = 0L
    private var hasAnnouncedArrival: Boolean = false
    private var announcedWaypoints: MutableSet<Int> = mutableSetOf()
    private var navTts: android.speech.tts.TextToSpeech? = null
    
    
    companion object {
        private const val TAG = "RoutesMapsActivity"
        private const val LOCATION_UPDATE_INTERVAL = 1000L // 1 second
        private const val MIN_DISTANCE_CHANGE = 5.0f // 5 meters
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routes_maps)
        
        initializeViews()
        setupToolbar()
        setupApiService()
        getIntentData()
        setupClickListeners()
        requestLocationPermission()
        
        // Setup alarm service connection to handle stop navigation
        setupAlarmServiceConnection()
    }
    
    private fun setupAlarmServiceConnection() {
        // Register broadcast receiver to handle alarm stop events
        val filter = android.content.IntentFilter("com.example.gzingapp.ALARM_STOPPED")
        
        // For Android 13+ (API 33+), we need to specify RECEIVER_NOT_EXPORTED since this is an internal app broadcast
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(alarmStopReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(alarmStopReceiver, filter)
        }
    }
    
    private val alarmStopReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            if (intent?.action == "com.example.gzingapp.ALARM_STOPPED") {
                Log.d(TAG, "Received alarm stopped broadcast")
                if (isNavigationActive) {
                    runOnUiThread {
                        stopNavigationFromAlarm()
                    }
                }
            }
        }
    }
    
    private fun initializeViews() {
        // Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        
        // Map View
        mapView = findViewById(R.id.mapView)
        
        // Routes Info Card (from view_routes_info_card.xml)
        routesInfoCard = findViewById(R.id.routesInfoCard)
        btnCollapseRoutes = findViewById(R.id.btnCollapseRoutes)
        routesContentContainer = findViewById(R.id.routesContentContainer)
        tvCurrentToWaypoint = findViewById(R.id.tvCurrentToWaypoint)
        tvDestinationInfo = findViewById(R.id.tvDestination)
        tvDistanceInfo = findViewById(R.id.tvDistance)
        tvEtaInfo = findViewById(R.id.tvEta)
        tvFareInfo = findViewById(R.id.tvFare)
        tvTrafficStatus = findViewById(R.id.tvTrafficStatus)
        trafficDot = findViewById(R.id.trafficDot)
        tvWaypointCount = findViewById(R.id.tvWaypointCount)
        btnStartNavigationRoutes = findViewById(R.id.btnStartNavigationRoutes)
        btnStopNavigationRoutes = findViewById(R.id.btnStopNavigationRoutes)
        
        
        // Floating Action Buttons
        fabMyLocation = findViewById(R.id.fabMyLocation)
        fabSosHelp = findViewById(R.id.fabSosHelp)
        // Traffic FAB removed - traffic info now always visible
        
        // Loading indicator
        loadingIndicator = findViewById(R.id.loadingIndicator)
        
        // Bottom Navigation
        tabDashboard = findViewById(R.id.tabDashboard)
        tabRoutes = findViewById(R.id.tabRoutes)
        tabPlaces = findViewById(R.id.tabPlaces)
        tabLandmarks = findViewById(R.id.tabLandmarks)
        bottomNavBar = findViewById(R.id.bottomNavBar)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Routes Navigation"
        
        // Setup ActionBarDrawerToggle with Toolbar for hamburger icon
        actionBarDrawerToggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // Ensure toolbar nav icon opens the drawer
        toolbar.setNavigationOnClickListener {
            if (!drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
            } else {
                drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            }
        }
        
        // Setup navigation menu item selection
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                    true
                }
                R.id.nav_places -> {
                    val intent = Intent(this, PlacesActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                    true
                }
                R.id.nav_saved_routes -> {
                    val intent = Intent(this, NavigationRoutesActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    logout()
                    drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
                    true
                }
                else -> false
            }
        }
        
        // Update navigation header with user info
        updateNavigationHeader()
    }
    
    private fun setupApiService() {
        apiService = RetrofitClient.apiService
        navigationRouteRepository = NavigationRouteRepository(this)
    }
    
    private fun getIntentData() {
        routeId = intent.getIntExtra("route_id", 0)
        routeName = intent.getStringExtra("route_name") ?: ""
        
        // Get comprehensive route data
        val routeDescription = intent.getStringExtra("route_description") ?: ""
        val routePinCount = intent.getIntExtra("route_pin_count", 0)
        val routeKilometer = intent.getStringExtra("route_kilometer") ?: "0"
        val routeEstimatedFare = intent.getStringExtra("route_estimated_fare") ?: "0"
        val routeStatus = intent.getStringExtra("route_status") ?: "active"
        
        // Get map details
        val mapCenterLng = intent.getDoubleExtra("map_center_lng", 0.0)
        val mapCenterLat = intent.getDoubleExtra("map_center_lat", 0.0)
        val mapZoom = intent.getDoubleExtra("map_zoom", 13.0)
        
        // Get route line data
        val routeLineType = intent.getStringExtra("route_line_type") ?: ""
        val routeLineGeometryType = intent.getStringExtra("route_line_geometry_type") ?: ""
        val routeLineGeometryDataType = intent.getStringExtra("route_line_geometry_data_type") ?: ""
        val routeLineCoordinatesJson = intent.getStringExtra("route_line_coordinates") ?: ""
        
        // Get pins data
        val routePinsJson = intent.getStringExtra("route_pins") ?: ""
        val routeFifoOrderJson = intent.getStringExtra("route_fifo_order") ?: ""
        
        // Get current location from passed pin (if available)
        currentLat = intent.getDoubleExtra("current_lat", 0.0)
        currentLng = intent.getDoubleExtra("current_lng", 0.0)
        val currentPinName = intent.getStringExtra("current_pin_name") ?: ""
        
        // Get destination (if specified)
        endLat = intent.getDoubleExtra("end_lat", 0.0)
        endLng = intent.getDoubleExtra("end_lng", 0.0)
        destName = intent.getStringExtra("dest_name") ?: ""
        
        Log.d(TAG, "Intent data - Route ID: $routeId, Name: $routeName, Description: $routeDescription")
        Log.d(TAG, "Route details - Pin Count: $routePinCount, Kilometer: $routeKilometer, Fare: $routeEstimatedFare, Status: $routeStatus")
        Log.d(TAG, "Map details - Center: $mapCenterLat, $mapCenterLng, Zoom: $mapZoom")
        Log.d(TAG, "Route line - Type: $routeLineType, Geometry: $routeLineGeometryType, Data Type: $routeLineGeometryDataType")
        Log.d(TAG, "Route line coordinates available: ${routeLineCoordinatesJson.isNotEmpty()}")
        Log.d(TAG, "Route pins available: ${routePinsJson.isNotEmpty()}")
        Log.d(TAG, "Current: $currentLat, $currentLng ($currentPinName), End: $endLat, $endLng ($destName)")
        
        // Parse and store route data
        parseAndStoreRouteData(
            routeId, routeName, routeDescription, routePinCount, routeKilometer, 
            routeEstimatedFare, routeStatus, mapCenterLng, mapCenterLat, mapZoom,
            routeLineType, routeLineGeometryType, routeLineGeometryDataType, 
            routeLineCoordinatesJson, routePinsJson, routeFifoOrderJson
        )
        
        if (routeId == 0) {
            showError("Invalid route data")
            finish()
        }
    }
    
    private fun parseAndStoreRouteData(
        routeId: Int, routeName: String, routeDescription: String, routePinCount: Int,
        routeKilometer: String, routeEstimatedFare: String, routeStatus: String,
        mapCenterLng: Double, mapCenterLat: Double, mapZoom: Double,
        routeLineType: String, routeLineGeometryType: String, routeLineGeometryDataType: String,
        routeLineCoordinatesJson: String, routePinsJson: String, routeFifoOrderJson: String
    ) {
        try {
            // Parse pins data
            val pins = if (routePinsJson.isNotEmpty()) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<com.example.gzingapp.data.RouteDetailsPin>>() {}.type
                gson.fromJson<List<com.example.gzingapp.data.RouteDetailsPin>>(routePinsJson, type)
            } else {
                emptyList()
            }
            
            // Parse fifo order
            val fifoOrder = if (routeFifoOrderJson.isNotEmpty()) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(routeFifoOrderJson, type)
            } else {
                emptyList()
            }
            
            // Parse route line coordinates
            val routeLineCoordinates = if (routeLineCoordinatesJson.isNotEmpty()) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<List<Double>>>() {}.type
                gson.fromJson<List<List<Double>>>(routeLineCoordinatesJson, type)
            } else {
                emptyList()
            }
            
            // Create route line object if coordinates are available
            val routeLine = if (routeLineCoordinates.isNotEmpty()) {
                com.example.gzingapp.data.RouteDetailsRouteLine(
                    type = routeLineType,
                    properties = emptyList(),
                    geometry = com.example.gzingapp.data.RouteDetailsRouteGeometry(
                        type = routeLineGeometryType,
                        properties = emptyList(),
                        geometry = com.example.gzingapp.data.RouteDetailsGeometryData(
                            coordinates = routeLineCoordinates,
                            type = routeLineGeometryDataType
                        )
                    )
                )
            } else {
                null
            }
            
            // Create map details
            val mapDetails = com.example.gzingapp.data.RouteDetailsMapDetails(
                pins = pins,
                center = if (mapCenterLng != 0.0 && mapCenterLat != 0.0) {
                    com.example.gzingapp.data.RouteDetailsCenter(mapCenterLng, mapCenterLat)
                } else null,
                zoom = if (mapZoom != 0.0) mapZoom else null,
                fifoOrder = fifoOrder,
                routeLine = routeLine
            )
            
            // Create route details object
            selectedRoute = com.example.gzingapp.data.RouteDetails(
                id = routeId,
                name = routeName,
                description = routeDescription,
                pinCount = routePinCount,
                kilometer = routeKilometer.toDoubleOrNull() ?: 0.0,
                estimatedTotalFare = routeEstimatedFare.toDoubleOrNull() ?: 0.0,
                mapDetails = mapDetails,
                status = routeStatus,
                createdAt = null,
                updatedAt = null
            )
            
            Log.d(TAG, "Successfully parsed and stored route data")
            Log.d(TAG, "Route: ${selectedRoute?.name}, Pins: ${selectedRoute?.mapDetails?.pins?.size ?: 0}")
            Log.d(TAG, "Route line available: ${selectedRoute?.mapDetails?.routeLine != null}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing route data", e)
            e.printStackTrace()
        }
    }
    
    private fun setupClickListeners() {
        // Start Navigation button in Routes Info Card
        btnStartNavigationRoutes.setOnClickListener {
            startNavigationMode()
        }
        
        // Stop Navigation button in Routes Info Card
        btnStopNavigationRoutes.setOnClickListener {
            stopNavigationMode()
        }
        
        fabMyLocation.setOnClickListener {
            requestLocationPermission()
        }
        
        
        // SOS Help FAB
        fabSosHelp.setOnSosActivatedListener {
            showSosHelpDialog()
        }
        
        // Traffic FAB removed - traffic info now always visible in route card
        
        // Bottom Navigation
        tabDashboard.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        
        tabRoutes.setOnClickListener {
            // Already on Routes, just show a toast
            Toast.makeText(this, "You are on Routes Navigation", Toast.LENGTH_SHORT).show()
        }
        
        tabPlaces.setOnClickListener {
            val intent = Intent(this, PlacesActivity::class.java)
            startActivity(intent)
        }
        
        tabLandmarks.setOnClickListener {
            val intent = Intent(this, LandmarksActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableLocationPuck()
                getCurrentLocationAndRefreshRoute()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                Toast.makeText(this, "Location permission is needed to show your current location", Toast.LENGTH_LONG).show()
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    private fun enableLocationPuck() {
        try {
            mapView.location.updateSettings { 
                this.enabled = true 
                this.puckBearingEnabled = true
            }
            Log.d(TAG, "Mapbox built-in location puck (blue dot) enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable location puck", e)
        }
    }
    
    private fun disableLocationPuck() {
        try {
            mapView.location.updateSettings { 
                this.enabled = false 
            }
            Log.d(TAG, "Mapbox built-in location puck (blue dot) disabled during navigation")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable location puck", e)
        }
    }
    
    private fun getCurrentLocationAndRefreshRoute() {
        // If we already have a current location from intent (passed pin), use it
        if (currentLat != 0.0 && currentLng != 0.0) {
            Log.d(TAG, "Using passed pin location as current: $currentLat, $currentLng")
            loadRouteDetails()
            return
        }
        
        // Otherwise, try to get device location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadRouteDetails()
            return
        }
        
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLat = location.latitude
                    currentLng = location.longitude
                    Log.d(TAG, "Device current location: $currentLat, $currentLng")
                } else {
                    Log.d(TAG, "No device location available")
                }
                loadRouteDetails()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get device location", e)
                loadRouteDetails()
        }
    }
    
    private fun loadRouteDetails() {
        // Check if we already have the route data
        if (selectedRoute != null) {
            Log.d(TAG, "Route already loaded, using cached data")
            loadRouteOnMap(selectedRoute!!)
            return
        }
        
        Log.d(TAG, "Loading route details for route ID: $routeId")
        showLoading(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getRouteDetails(routeId)
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.success == true) {
                            body.data?.route?.let { route ->
                                Log.d(TAG, "Route loaded successfully for ID $routeId: ${route.name}")
                                
                                // Log processed map details in structured format
                                logMapDetails(routeId, route)
                                
                                selectedRoute = route
                                loadRouteOnMap(route)
                            } ?: run {
                                Log.e(TAG, "Route data is null in response for ID: $routeId")
                                showError("Route data not found in response")
                            }
                        } else {
                            Log.e(TAG, "API response failed for route ID $routeId: ${body?.message}")
                            showError("Failed to load route details: ${body?.message ?: "Unknown error"}")
                        }
                    } else {
                        Log.e(TAG, "HTTP error for route ID $routeId: ${response.code()}: ${response.message()}")
                        showError("HTTP ${response.code()}: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Error loading route details: ${e.message}")
                    Log.e(TAG, "Error loading route details for ID $routeId", e)
                }
            }
        }
    }
    
    private fun logMapDetails(routeId: Int, route: RouteDetails) {
        Log.d(TAG, "=== ROUTE DATA FETCHED ===")
        Log.d(TAG, "Route ID: $routeId")
        Log.d(TAG, "Route Name: ${route.name}")
        Log.d(TAG, "Route Distance: ${route.kilometer} km")
        Log.d(TAG, "Route Fare: ₱${route.estimatedTotalFare}")
        Log.d(TAG, "Route Pin Count: ${route.pinCount}")
        
        // Log processed map details
        val mapDetails = route.mapDetails
        if (mapDetails != null) {
            Log.d(TAG, "=== MAP DETAILS EXTRACTED ===")
            Log.d(TAG, "Map Details Present: true")
            
            val pins = mapDetails.pins
            if (pins != null && pins.isNotEmpty()) {
                Log.d(TAG, "Pins Count: ${pins.size}")
                Log.d(TAG, "=== PIN LOCATIONS ===")
                pins.forEachIndexed { index, pin ->
                    Log.d(TAG, "Pin ${index + 1}: ${pin.name} | Lat: ${pin.lat} | Lng: ${pin.lng} | Number: ${pin.number}")
                }
            } else {
                Log.w(TAG, "Pins: null or empty")
            }
            
            val routeLine = mapDetails.routeLine
            if (routeLine != null) {
                Log.d(TAG, "Route Line: Present")
                Log.d(TAG, "Route Line Type: ${routeLine.type}")
            } else {
                Log.d(TAG, "Route Line: null")
            }
        } else {
            Log.w(TAG, "Map Details: null")
        }
        Log.d(TAG, "=== END ROUTE DATA ===")
    }
    
    
    private fun loadRouteOnMap(route: RouteDetails) {
        Log.d(TAG, "loadRouteOnMap called with route: ${route.name}")
        
        if (!mapStyleLoaded) {
            // Load style for the first time
            Log.d(TAG, "Loading map style for the first time")
            mapView.getMapboxMap().loadStyleUri(Style.TRAFFIC_DAY) { style ->
                Log.d(TAG, "Map style loaded, starting route rendering")
                mapStyleLoaded = true
                updateMapLayers(style, route)
            }
        } else {
            // Style already loaded, just update layers
            Log.d(TAG, "Map style already loaded, updating layers")
            mapView.getMapboxMap().getStyle { style ->
                updateMapLayers(style, route)
            }
        }
    }
    
    private fun updateMapLayers(style: Style, route: RouteDetails) {
        Log.d(TAG, "=== updateMapLayers START ===")
        Log.d(TAG, "updateMapLayers called with route: ${route.name}")
        Log.d(TAG, "Style is loaded: ${style.isStyleLoaded()}")
        Log.d(TAG, "Current location: $currentLat, $currentLng")
        
        clearMapAnnotations(style)
        
        val pins = route.mapDetails?.pins
        if (pins.isNullOrEmpty()) {
            Log.e(TAG, "No pins found in route data for route ID: $routeId")
            Log.e(TAG, "Route mapDetails: ${route.mapDetails}")
            Log.e(TAG, "Route pinCount: ${route.pinCount}")
            
            // Show error message since this specific route has no pins
            showError("No waypoints found for this route. Please try a different route.")
            return
        }
        
        Log.d(TAG, "Found ${pins.size} pins in route data")
        
        // Log all pins for debugging
        pins.forEachIndexed { index, pin ->
            Log.d(TAG, "Pin $index: ${pin.name} at ${pin.lat}, ${pin.lng} (number: ${pin.number})")
        }
        
        // Find closest waypoint to current location
        closestWaypoint = findClosestWaypointToCurrentLocation(route)
        Log.d(TAG, "Closest waypoint: ${closestWaypoint?.name}")
        
        // NEW LOGIC: Use the passed destination from RoutesActivity
        // The destination is now the selected pin from the dialog
        Log.d(TAG, "Destination from RoutesActivity: $endLat, $endLng ($destName)")
        
        // If no destination was passed, use the last pin as destination
        if (endLat == 0.0 && endLng == 0.0) {
            val lastPin = pins.maxByOrNull { it.number }
            if (lastPin != null) {
                endLat = lastPin.lat
                endLng = lastPin.lng
                destName = lastPin.name
                Log.d(TAG, "Using last pin as destination: ${lastPin.name} at ${endLat}, ${endLng}")
            }
        }
        
        // Create waypoint sequence from closest waypoint to destination
        val destPin = pins.find { pin -> 
            Math.abs(pin.lat - endLat) < 0.0001 && Math.abs(pin.lng - endLng) < 0.0001 
        }
        waypointSequence = createWaypointSequenceFromClosestToDestination(pins, closestWaypoint, destPin)
        destinationPin = destPin
        currentWaypointIndex = 0 // Reset to first waypoint
        Log.d(TAG, "Created waypoint sequence with ${waypointSequence.size} pins: ${waypointSequence.map { "${it.name} (${it.number})" }}")
        Log.d(TAG, "FIRST STOP: ${waypointSequence.firstOrNull()?.name ?: "No waypoints"} (closest waypoint)")
        
        // FIRST: Draw API response polyline (beige) - from route data
        Log.d(TAG, "FIRST: Adding API response polyline (beige)")
        addApiResponsePolyline(route, style)
        
        // SECOND: Draw Mapbox direction polyline (blue) - from current location to closest waypoint
        Log.d(TAG, "SECOND: Adding Mapbox direction polyline (blue) - current location to closest waypoint")
        addDirectRoutePolyline(route, style)
        
        // SECOND: Add all pins to map AFTER the polylines (pins above lines)
        Log.d(TAG, "SECOND: Adding ${pins.size} pins to map")
        addAllPinsToMap(pins, style)
        
        // Enable Mapbox built-in location puck (blue dot) if we have current location
        if (currentLat != 0.0 && currentLng != 0.0) {
            Log.d(TAG, "Enabling Mapbox built-in location puck")
            enableLocationPuck()
        }
        
        // Fit camera to show all pins
        Log.d(TAG, "About to fit camera to pins")
        fitCameraToAllPins(pins)
        
        // Setup pin click listener
        setupPinClickListener()
        
        // Show route info with sequence calculations
        showRouteInfoWithSequence(route, waypointSequence)
        
        // Setup routes info card
        setupRoutesInfoCard()
        
        // Create geofence for destination
        if (endLat != 0.0 && endLng != 0.0) {
            createGeofenceForDestination(Point.fromLngLat(endLng, endLat))
            // Add visual geofence circle on map
            addGeofenceCircleToMap(Point.fromLngLat(endLng, endLat), style)
        }
        
        Log.d(TAG, "=== updateMapLayers END ===")
    }
    
    
    
    private fun findClosestWaypointToCurrentLocation(route: RouteDetails): RouteDetailsPin? {
        val pins = route.mapDetails?.pins
        Log.d(TAG, "findClosestWaypointToCurrentLocation: route.mapDetails = ${route.mapDetails}")
        Log.d(TAG, "findClosestWaypointToCurrentLocation: pins = $pins")
        Log.d(TAG, "findClosestWaypointToCurrentLocation: pins size = ${pins?.size}")
        if (pins.isNullOrEmpty()) {
            Log.e(TAG, "No waypoints found for this route")
            return null
        }
        
        // If we don't have current location, use the first pin
        if (currentLat == 0.0 && currentLng == 0.0) {
            Log.d(TAG, "No current location, using first pin as closest")
            return pins.first()
        }
        
        var closestPin: RouteDetailsPin? = null
        var minDistance = Double.MAX_VALUE
        
        // Find the closest pin to the current location (passed pin or device location)
        for (pin in pins) {
            val distance = computeHaversineMeters(currentLat, currentLng, pin.lat, pin.lng)
            Log.d(TAG, "Distance to ${pin.name}: ${formatDistance(distance)}")
            if (distance < minDistance) {
                minDistance = distance
                closestPin = pin
            }
        }
        
        Log.d(TAG, "Found closest waypoint: ${closestPin?.name} at distance ${formatDistance(minDistance)}")
        return closestPin
    }
    
    
    private fun addAllPinsToMap(pins: List<RouteDetailsPin>, style: Style) {
        Log.d(TAG, "=== addAllPinsToMap START ===")
        Log.d(TAG, "addAllPinsToMap: Adding ${pins.size} pins to map")
        Log.d(TAG, "Style is loaded: ${style.isStyleLoaded()}")
        
        if (pins.isEmpty()) {
            Log.e(TAG, "addAllPinsToMap: No pins to add!")
            return
        }
        
        // Log each pin with detailed information (like RoutesActivity)
        pins.forEachIndexed { index, pin ->
            Log.d(TAG, "Pin $index: ${pin.name} at ${pin.lat}, ${pin.lng}")
            Log.d(TAG, "  - ID: ${pin.id}")
            Log.d(TAG, "  - Number: ${pin.number}")
            Log.d(TAG, "  - Address: ${pin.address}")
            Log.d(TAG, "  - Place: ${pin.placeName}")
        }
        
        // Create features for pins with properties for text labels (like RoutesActivity)
        val pinFeatures = pins.map { pin ->
            Feature.fromGeometry(
                Point.fromLngLat(pin.lng, pin.lat)
            ).apply {
                addStringProperty("name", pin.name)
                addStringProperty("address", pin.address ?: pin.placeName ?: "")
                addNumberProperty("number", pin.number)
            }
        }
        
        Log.d(TAG, "Created ${pinFeatures.size} pin features")
        
        try {
            Log.d(TAG, "Starting to add pins to existing style")
            Log.d(TAG, "Style is ready: ${style.isStyleLoaded()}")
            
            // Remove existing pins if they exist (layers first, then sources)
            Log.d(TAG, "Removing existing pin layers and sources")
            if (style.styleLayerExists("route-pins-text-layer")) {
                style.removeStyleLayer("route-pins-text-layer")
                Log.d(TAG, "Removed route-pins-text-layer")
            }
            if (style.styleLayerExists("route-pins-layer")) {
                style.removeStyleLayer("route-pins-layer")
                Log.d(TAG, "Removed route-pins-layer")
            }
            if (style.styleSourceExists("route-pins")) {
                style.removeStyleSource("route-pins")
                Log.d(TAG, "Removed route-pins source")
            }
            
            // Add custom pin image (same as RoutesActivity)
            val pinBitmap = vectorToBitmap(R.drawable.ic_custom_pin)
            if (pinBitmap != null) {
                Log.d(TAG, "Adding custom pin image to style")
                style.addImage("route-pin-icon", pinBitmap)
                Log.d(TAG, "Successfully added pin image")
            } else {
                Log.w(TAG, "Failed to create custom pin bitmap, will use fallback")
            }

            // Add pins source
            Log.d(TAG, "Adding pins source with ${pinFeatures.size} features")
            style.addSource(
                geoJsonSource("route-pins") {
                    featureCollection(FeatureCollection.fromFeatures(pinFeatures))
                }
            )
            Log.d(TAG, "Successfully added pins source")
            
            // Add pins layer using symbol layer with custom pin icon ABOVE the route line (like RoutesActivity)
            Log.d(TAG, "Adding pins layer to map")
            if (pinBitmap != null) {
                style.addLayer(
                    symbolLayer("route-pins-layer", "route-pins") {
                        iconImage("route-pin-icon")
                        iconSize(0.8)
                        iconAllowOverlap(true)
                        iconIgnorePlacement(true)
                    }
                )
                Log.d(TAG, "Successfully added pins symbol layer")
            } else {
                // Fallback: draw circles if icon missing
                Log.w(TAG, "Using fallback circle layer for pins")
                style.addLayer(
                    com.mapbox.maps.extension.style.layers.generated.circleLayer("route-pins-layer", "route-pins") {
                        circleColor("#D32F2F")
                        circleRadius(8.0)
                        circleStrokeColor("#FFFFFF")
                        circleStrokeWidth(2.0)
                    }
                )
                Log.d(TAG, "Successfully added pins circle layer")
            }
            
            // Add text labels layer for pin names ABOVE the pins layer (like RoutesActivity)
            Log.d(TAG, "Adding text labels layer to map")
            style.addLayer(
                symbolLayer("route-pins-text-layer", "route-pins") {
                    textField("{name}")
                    textSize(14.0)
                    textColor("#000000")
                    // Stronger white halo to simulate white background
                    textHaloColor("#FFFFFF")
                    textHaloWidth(4.0)
                    textHaloBlur(0.7)
                    // Slightly higher above the pin
                    textOffset(listOf(0.0, -2.4))
                    textAllowOverlap(true)
                    textIgnorePlacement(true)
                }
            )
            Log.d(TAG, "Successfully added text labels layer")
            
            Log.d(TAG, "Successfully added ${pins.size} pins to map with custom pin icons")
            
            // Force map repaint to ensure visibility
            mapView.getMapboxMap().triggerRepaint()
            Log.d(TAG, "Triggered map repaint")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add pins to map", e)
            e.printStackTrace()
        }
        
        Log.d(TAG, "=== addAllPinsToMap END ===")
    }
    
    private fun fitCameraToAllPins(pins: List<RouteDetailsPin>) {
        if (pins.isEmpty()) {
            Log.e(TAG, "fitCameraToAllPins: No pins to fit camera to")
            return
        }
        
        Log.d(TAG, "fitCameraToAllPins: Fitting camera to ${pins.size} pins")
        
        val lats = pins.map { it.lat }.toMutableList()
        val lngs = pins.map { it.lng }.toMutableList()
        
        // Include current location in bounds if available
        if (currentLat != 0.0 && currentLng != 0.0) {
            lats.add(currentLat)
            lngs.add(currentLng)
            Log.d(TAG, "Including current location in camera bounds: $currentLat, $currentLng")
        }
        
        val minLat = lats.minOrNull() ?: 0.0
        val maxLat = lats.maxOrNull() ?: 0.0
        val minLng = lngs.minOrNull() ?: 0.0
        val maxLng = lngs.maxOrNull() ?: 0.0
        
        val centerLat = (minLat + maxLat) / 2
        val centerLng = (minLng + maxLng) / 2
        val centerPoint = Point.fromLngLat(centerLng, centerLat)
        
        val maxRange = maxOf(maxLat - minLat, maxLng - minLng)
        val zoom = when {
            maxRange > 0.1 -> 10.0
            maxRange > 0.05 -> 12.0
            maxRange > 0.02 -> 14.0
            else -> 16.0
        }
        
        Log.d(TAG, "Camera center: $centerLat, $centerLng, zoom: $zoom")
        Log.d(TAG, "Bounds: lat($minLat to $maxLat), lng($minLng to $maxLng)")
        
        mapView.getMapboxMap().setCamera(
            com.mapbox.maps.CameraOptions.Builder().center(centerPoint).zoom(zoom).build()
        )
        
        Log.d(TAG, "Camera positioned successfully")
    }
    
    private fun addDirectRoutePolyline(route: RouteDetails, style: Style) {
        Log.d(TAG, "=== addDirectRoutePolyline START ===")
        Log.d(TAG, "Route details: ${route.name}, mapDetails: ${route.mapDetails}")
        
        val pins = route.mapDetails?.pins
        if (pins.isNullOrEmpty()) {
            Log.e(TAG, "No pins available for route creation")
            return
        }
        
        // Get closest waypoint only
        val closest = closestWaypoint ?: pins.first()
        
        Log.d(TAG, "Creating direct route from current location to closest waypoint: ${closest.name}")
        
        // Create waypoints for Mapbox Directions API - current location to closest waypoint
        val waypoints = mutableListOf<Point>()
        
        // Add current location if available
        if (currentLat != 0.0 && currentLng != 0.0) {
            waypoints.add(Point.fromLngLat(currentLng, currentLat))
            Log.d(TAG, "Added current location as starting point")
        } else {
            Log.e(TAG, "No current location available for direct route")
            return
        }
        
        // Add closest waypoint as ending point
        waypoints.add(Point.fromLngLat(closest.lng, closest.lat))
        Log.d(TAG, "Added closest waypoint as ending point: ${closest.name}")
        
        // Request route from Mapbox Directions API (current location → closest waypoint)
        requestMapboxRoute(waypoints, style)
        
        Log.d(TAG, "=== addDirectRoutePolyline END ===")
    }
    
    private fun addApiResponsePolyline(route: RouteDetails, style: Style) {
        Log.d(TAG, "=== addApiResponsePolyline START ===")
        
        // Get route line data from the route (same as RoutesActivity)
        val routeLine = route.mapDetails?.routeLine
        if (routeLine?.geometry?.geometry?.coordinates != null) {
            Log.d(TAG, "Adding API response polyline for route: ${route.name}")
            
            try {
                // Get coordinates from route line (nested structure)
                val coordinates = routeLine.geometry.geometry.coordinates
                if (coordinates.isNotEmpty()) {
                    Log.d(TAG, "API response polyline has ${coordinates.size} coordinate points")
                    
                    // Create LineString from coordinates
                    val lineString = LineString.fromLngLats(
                        coordinates.map { coord: List<Double> ->
                            Point.fromLngLat(coord[0], coord[1])
                        }
                    )
                    
                    // Create feature for the route line
                    val routeFeature = Feature.fromGeometry(lineString)
                    
                    // Remove existing API response polyline if it exists
                    if (style.styleSourceExists("api-route-line")) {
                        style.removeStyleSource("api-route-line")
                    }
                    if (style.styleLayerExists("api-route-line-layer")) {
                        style.removeStyleLayer("api-route-line-layer")
                    }
                    
                    // Add API response route line source
                    style.addSource(
                        geoJsonSource("api-route-line") {
                            feature(routeFeature)
                        }
                    )
                    
                    // Add API response route line layer (beige)
                    style.addLayer(
                        lineLayer("api-route-line-layer", "api-route-line") {
                            lineColor("#D2B48C") // Beige color for API response
                            lineWidth(4.0)
                            lineOpacity(0.9)
                        }
                    )
                    
                    Log.d(TAG, "Successfully added API response polyline with ${coordinates.size} points")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing API response route line data", e)
            }
        } else {
            Log.d(TAG, "No API response route line data available for route: ${route.name}")
            // Fallback: create polyline from pins if no route line data
            createFallbackApiPolylineFromPins(route, style)
        }
        
        Log.d(TAG, "=== addApiResponsePolyline END ===")
    }
    
    private fun addReducedBeigePolyline(route: RouteDetails, style: Style) {
        Log.d(TAG, "=== addReducedBeigePolyline START ===")
        
        val pins = route.mapDetails?.pins
        if (pins.isNullOrEmpty()) {
            Log.e(TAG, "No pins available for reduced beige polyline")
            return
        }
        
        // Get closest waypoint and destination
        val closest = closestWaypoint ?: pins.first()
        val destPin = pins.find { pin -> 
            Math.abs(pin.lat - endLat) < 0.0001 && Math.abs(pin.lng - endLng) < 0.0001 
        } ?: pins.last()
        
        Log.d(TAG, "Creating reduced beige polyline using Mapbox Directions API from closest waypoint to destination: ${closest.name} → ${destPin.name}")
        
        // Use Mapbox Directions API to get route from closest waypoint to destination
        val waypoints = mutableListOf<Point>()
        waypoints.add(Point.fromLngLat(closest.lng, closest.lat))
        waypoints.add(Point.fromLngLat(destPin.lng, destPin.lat))
        
        // Request Mapbox route for beige polyline (closest waypoint to destination)
        requestMapboxRouteForBeigePolyline(waypoints, style)
        
        Log.d(TAG, "=== addReducedBeigePolyline END ===")
    }
    
    private fun findRouteSegmentFromClosestToDestination(
        coordinates: List<List<Double>>, 
        closestWaypoint: RouteDetailsPin, 
        destination: RouteDetailsPin
    ): List<List<Double>> {
        Log.d(TAG, "=== findRouteSegmentFromClosestToDestination START ===")
        
        if (coordinates.isEmpty()) {
            Log.e(TAG, "No coordinates available")
            return emptyList()
        }
        
        // Find the closest coordinate to the closest waypoint
        var closestIndex = 0
        var minDistance = Double.MAX_VALUE
        
        for (i in coordinates.indices) {
            val coord = coordinates[i]
            val distance = computeHaversineMeters(
                closestWaypoint.lat, closestWaypoint.lng,
                coord[1], coord[0] // coord[1] is lat, coord[0] is lng
            )
            if (distance < minDistance) {
                minDistance = distance
                closestIndex = i
            }
        }
        
        Log.d(TAG, "Found closest waypoint at index $closestIndex with distance ${minDistance}m")
        
        // Find the closest coordinate to the destination
        var destIndex = coordinates.size - 1
        minDistance = Double.MAX_VALUE
        
        for (i in coordinates.indices) {
            val coord = coordinates[i]
            val distance = computeHaversineMeters(
                destination.lat, destination.lng,
                coord[1], coord[0] // coord[1] is lat, coord[0] is lng
            )
            if (distance < minDistance) {
                minDistance = distance
                destIndex = i
            }
        }
        
        Log.d(TAG, "Found destination at index $destIndex with distance ${minDistance}m")
        
        // Extract the segment from closest waypoint to destination
        val startIndex = minOf(closestIndex, destIndex)
        val endIndex = maxOf(closestIndex, destIndex)
        
        val segment = coordinates.subList(startIndex, endIndex + 1)
        
        Log.d(TAG, "Extracted route segment from index $startIndex to $endIndex (${segment.size} points)")
        Log.d(TAG, "=== findRouteSegmentFromClosestToDestination END ===")
        
        return segment
    }
    
    private fun createFallbackApiPolylineFromPins(route: RouteDetails, style: Style) {
        val pins = route.mapDetails?.pins
        if (pins.isNullOrEmpty()) {
            Log.d(TAG, "No pins available for fallback API polyline")
            return
        }
        
        Log.d(TAG, "Creating fallback API polyline from ${pins.size} pins")
        
        // Sort pins by their number to get the correct sequence
        val sortedPins = pins.sortedBy { it.number }
        
        // Create coordinates for the polyline
        val coordinates = sortedPins.map { pin ->
            Point.fromLngLat(pin.lng, pin.lat)
        }
        
        try {
            val lineString = LineString.fromLngLats(coordinates)
            val routeFeature = Feature.fromGeometry(lineString)
            
            // Remove existing fallback API polyline if it exists
            if (style.styleLayerExists("api-route-line-layer")) {
                style.removeStyleLayer("api-route-line-layer")
            }
            if (style.styleSourceExists("api-route-line")) {
                style.removeStyleSource("api-route-line")
            }
            
            // Add fallback API polyline source
            style.addSource(
                geoJsonSource("api-route-line") {
                    feature(routeFeature)
                }
            )
            
            // Add fallback API polyline layer (beige)
            style.addLayer(
                lineLayer("api-route-line-layer", "api-route-line") {
                    lineColor("#D2B48C") // Beige color for API response
                    lineWidth(4.0)
                    lineOpacity(0.9)
                }
            )
            
            Log.d(TAG, "Successfully added fallback API polyline with ${coordinates.size} points")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating fallback API polyline", e)
        }
    }
    
    private fun requestMapboxRoute(waypoints: List<Point>, style: Style) {
        Log.d(TAG, "=== requestMapboxRoute START ===")
        Log.d(TAG, "Requesting route with ${waypoints.size} waypoints")
        
        if (waypoints.size < 2) {
            Log.e(TAG, "Need at least 2 waypoints for routing")
                    return
        }
        
        // Create MapboxDirections request
        val builder = MapboxDirections.builder()
            .origin(waypoints.first())
            .destination(waypoints.last())
            .profile(DirectionsCriteria.PROFILE_DRIVING) // Use driving profile for car transportation
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
            .accessToken(getString(R.string.mapbox_access_token))
        
        // Add intermediate waypoints if any
        if (waypoints.size > 2) {
            val intermediateWaypoints = waypoints.subList(1, waypoints.size - 1)
            for (waypoint in intermediateWaypoints) {
                builder.addWaypoint(waypoint)
            }
            Log.d(TAG, "Added ${intermediateWaypoints.size} intermediate waypoints")
        }
        
        val client = builder.build()
        
        Log.d(TAG, "Making Mapbox Directions API request...")
        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val directionsResponse = response.body()!!
                    val routes = directionsResponse.routes()
                    
                    if (routes.isNotEmpty()) {
                        val route = routes.first()
                        Log.d(TAG, "Received route from Mapbox Directions API")
                        Log.d(TAG, "Route distance: ${route.distance()} meters")
                        Log.d(TAG, "Route duration: ${route.duration()} seconds")
                        
                        // Get the route geometry
                        val routeGeometry = route.geometry()
                        if (routeGeometry != null) {
                            Log.d(TAG, "Route geometry available, drawing polyline")
                            drawMapboxRouteLine(routeGeometry, style)
                } else {
                            Log.e(TAG, "Route geometry is null")
                }
                    } else {
                        Log.e(TAG, "No routes returned from Mapbox Directions API")
            }
        } else {
                    Log.e(TAG, "Mapbox Directions API request failed: ${response.code()} - ${response.message()}")
                }
            }
            
            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Log.e(TAG, "Mapbox Directions API request failed", t)
                // Fallback to manual route drawing
                Log.d(TAG, "Falling back to manual route drawing")
                drawManualRouteLine(waypoints, style)
            }
        })
        
        Log.d(TAG, "=== requestMapboxRoute END ===")
    }
    
    private fun requestMapboxRouteForBeigePolyline(waypoints: List<Point>, style: Style) {
        Log.d(TAG, "=== requestMapboxRouteForBeigePolyline START ===")
        Log.d(TAG, "Requesting beige polyline route with ${waypoints.size} waypoints")
        
        if (waypoints.size < 2) {
            Log.e(TAG, "Need at least 2 waypoints for beige polyline routing")
            return
        }
        
        // Create MapboxDirections request for beige polyline with traffic data
        val builder = MapboxDirections.builder()
            .origin(waypoints.first())
            .destination(waypoints.last())
            .profile(DirectionsCriteria.PROFILE_DRIVING) // Use driving profile for car transportation
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
            .steps(true) // Include step-by-step directions for traffic analysis
            .accessToken(getString(R.string.mapbox_access_token))
        
        // Add intermediate waypoints if any
        if (waypoints.size > 2) {
            val intermediateWaypoints = waypoints.subList(1, waypoints.size - 1)
            for (waypoint in intermediateWaypoints) {
                builder.addWaypoint(waypoint)
            }
            Log.d(TAG, "Added ${intermediateWaypoints.size} intermediate waypoints for beige polyline")
        }
        
        val client = builder.build()
        
        Log.d(TAG, "Making Mapbox Directions API request for beige polyline...")
        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val directionsResponse = response.body()!!
                    val routes = directionsResponse.routes()
                    
                    if (routes.isNotEmpty()) {
                        val route = routes.first()
                        Log.d(TAG, "Received beige polyline route from Mapbox Directions API")
                        Log.d(TAG, "Beige route distance: ${route.distance()} meters")
                        Log.d(TAG, "Beige route duration: ${route.duration()} seconds")
                        
                        // Analyze traffic conditions
                        val trafficCondition = analyzeTrafficConditions(route)
                        Log.d(TAG, "Traffic condition: $trafficCondition")
                        
                        // Update UI with traffic information
                        updateTrafficInfo(trafficCondition)
                        
                        // Get the route geometry
                        val routeGeometry = route.geometry()
                        if (routeGeometry != null) {
                            Log.d(TAG, "Beige route geometry available, drawing beige polyline")
                            drawBeigePolylineFromMapbox(routeGeometry, style)
                        } else {
                            Log.e(TAG, "Beige route geometry is null")
                        }
                    } else {
                        Log.e(TAG, "No routes returned from Mapbox Directions API for beige polyline")
                    }
                } else {
                    Log.e(TAG, "Mapbox Directions API request failed for beige polyline: ${response.code()} - ${response.message()}")
                }
            }
            
            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Log.e(TAG, "Mapbox Directions API request failed for beige polyline", t)
                // Fallback to simple line between waypoints
                Log.d(TAG, "Falling back to simple line for beige polyline")
                drawSimpleBeigeLine(waypoints, style)
            }
        })
        
        Log.d(TAG, "=== requestMapboxRouteForBeigePolyline END ===")
    }
    
    private fun drawMapboxRouteLine(routeGeometry: String, style: Style) {
        Log.d(TAG, "=== drawMapboxRouteLine START ===")
        
        try {
            // Decode the polyline geometry
            val coordinates = PolylineUtils.decode(routeGeometry, 6)
            Log.d(TAG, "Decoded route geometry with ${coordinates.size} points")
            
            if (coordinates.isEmpty()) {
                Log.e(TAG, "No coordinates decoded from route geometry")
            return
        }
        
            // Convert to Point list
            val points = coordinates.map { coord ->
                Point.fromLngLat(coord.longitude(), coord.latitude())
            }
            
            // Remove existing route line if it exists
            if (style.styleLayerExists("mapbox-route-line-layer")) {
                style.removeStyleLayer("mapbox-route-line-layer")
                Log.d(TAG, "Removed existing mapbox-route-line-layer")
            }
            if (style.styleSourceExists("mapbox-route-line")) {
                style.removeStyleSource("mapbox-route-line")
                Log.d(TAG, "Removed existing mapbox-route-line source")
            }
            
            // Create LineString from coordinates
            val lineString = LineString.fromLngLats(points)
            val routeFeature = Feature.fromGeometry(lineString)
            
            Log.d(TAG, "Created LineString with ${lineString.coordinates().size} coordinates")
            
            // Add route line source
            style.addSource(
                geoJsonSource("mapbox-route-line") {
                    feature(routeFeature)
                }
            )
            Log.d(TAG, "Added mapbox route line source")
            
            // Add route line layer with enhanced styling for car transportation
            style.addLayer(
                lineLayer("mapbox-route-line-layer", "mapbox-route-line") {
                    lineColor("#2196F3") // Blue color for Mapbox direction route from current location to closest waypoint
                    lineWidth(6.0) // Thicker line for car route
                    lineOpacity(0.8)
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                }
            )
            Log.d(TAG, "Added mapbox route line layer")
            
            Log.d(TAG, "Successfully drew Mapbox route line")
        } catch (e: Exception) {
            Log.e(TAG, "Error drawing Mapbox route line", e)
        }
        
        Log.d(TAG, "=== drawMapboxRouteLine END ===")
    }
    
    private fun drawManualRouteLine(points: List<Point>, style: Style) {
        Log.d(TAG, "=== drawManualRouteLine START ===")
        Log.d(TAG, "Drawing manual route line with ${points.size} points")
        
        if (points.isEmpty()) {
            Log.e(TAG, "No points to draw - returning early")
            return
        }
        
        // Log first few points for debugging
        points.take(3).forEachIndexed { index, point ->
            Log.d(TAG, "  Point $index: ${point.latitude()}, ${point.longitude()}")
        }
        
        try {
            // Remove existing manual route line if it exists
            if (style.styleLayerExists("manual-route-line-layer")) {
                style.removeStyleLayer("manual-route-line-layer")
                Log.d(TAG, "Removed existing manual-route-line-layer")
            }
            if (style.styleSourceExists("manual-route-line")) {
                style.removeStyleSource("manual-route-line")
                Log.d(TAG, "Removed existing manual-route-line source")
            }
            
            // Create LineString from coordinates
            val lineString = LineString.fromLngLats(points)
            val routeFeature = Feature.fromGeometry(lineString)
            
            Log.d(TAG, "Created LineString with ${lineString.coordinates().size} coordinates")
            Log.d(TAG, "Created Feature from LineString")
            
            // Add manual route line source
            style.addSource(
                geoJsonSource("manual-route-line") {
                    feature(routeFeature)
                }
            )
            Log.d(TAG, "Added manual route line source")
            
            // Add manual route line layer with enhanced styling
            style.addLayer(
                lineLayer("manual-route-line-layer", "manual-route-line") {
                    lineColor("#D2B48C") // Beige/brown color for manual route (current location to closest waypoint)
                    lineWidth(6.0) // Thicker line for better visibility
                    lineOpacity(0.9) // Higher opacity
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                }
            )
            Log.d(TAG, "Added manual route line layer")
            
            Log.d(TAG, "✅ Successfully added manual route line with ${points.size} points")
            
            // Force map repaint to ensure visibility
            mapView.getMapboxMap().triggerRepaint()
            Log.d(TAG, "Triggered map repaint for manual route line")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error adding manual route line", e)
            e.printStackTrace()
        }
        
        Log.d(TAG, "=== drawManualRouteLine END ===")
    }
    
    private fun drawBeigePolylineFromMapbox(routeGeometry: String, style: Style) {
        Log.d(TAG, "=== drawBeigePolylineFromMapbox START ===")
        
        try {
            // Decode the polyline geometry
            val coordinates = PolylineUtils.decode(routeGeometry, 6)
            Log.d(TAG, "Decoded beige polyline geometry with ${coordinates.size} points")
            
            if (coordinates.isEmpty()) {
                Log.e(TAG, "No coordinates decoded from beige polyline geometry")
            return
        }
        
            // Convert to Point list
            val points = coordinates.map { coord ->
                Point.fromLngLat(coord.longitude(), coord.latitude())
            }
            
            // Remove existing reduced beige polyline if it exists
            if (style.styleLayerExists("reduced-beige-route-line-layer")) {
                style.removeStyleLayer("reduced-beige-route-line-layer")
                Log.d(TAG, "Removed existing reduced-beige-route-line-layer")
            }
            if (style.styleSourceExists("reduced-beige-route-line")) {
                style.removeStyleSource("reduced-beige-route-line")
                Log.d(TAG, "Removed existing reduced-beige-route-line source")
            }
            
            // Create LineString from coordinates
            val lineString = LineString.fromLngLats(points)
            val routeFeature = Feature.fromGeometry(lineString)
            
            Log.d(TAG, "Created beige LineString with ${lineString.coordinates().size} coordinates")
            
            // Add reduced beige polyline source
            style.addSource(
                geoJsonSource("reduced-beige-route-line") {
                    feature(routeFeature)
                }
            )
            Log.d(TAG, "Added reduced beige route line source")
            
            // Add reduced beige polyline layer
            style.addLayer(
                lineLayer("reduced-beige-route-line-layer", "reduced-beige-route-line") {
                    lineColor("#D2B48C") // Beige color for reduced route
                    lineWidth(4.0)
                    lineOpacity(0.9)
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                }
            )
            Log.d(TAG, "Added reduced beige route line layer")
            
            Log.d(TAG, "Successfully drew beige polyline from Mapbox Directions API")
        } catch (e: Exception) {
            Log.e(TAG, "Error drawing beige polyline from Mapbox", e)
        }
        
        Log.d(TAG, "=== drawBeigePolylineFromMapbox END ===")
    }
    
    private fun drawSimpleBeigeLine(waypoints: List<Point>, style: Style) {
        Log.d(TAG, "=== drawSimpleBeigeLine START ===")
        Log.d(TAG, "Drawing simple beige line with ${waypoints.size} waypoints")
        
        if (waypoints.isEmpty()) {
            Log.e(TAG, "No waypoints to draw - returning early")
            return
        }
        
        try {
            // Remove existing reduced beige polyline if it exists
            if (style.styleLayerExists("reduced-beige-route-line-layer")) {
                style.removeStyleLayer("reduced-beige-route-line-layer")
                Log.d(TAG, "Removed existing reduced-beige-route-line-layer")
            }
            if (style.styleSourceExists("reduced-beige-route-line")) {
                style.removeStyleSource("reduced-beige-route-line")
                Log.d(TAG, "Removed existing reduced-beige-route-line source")
            }
            
            // Create LineString from waypoints
            val lineString = LineString.fromLngLats(waypoints)
            val routeFeature = Feature.fromGeometry(lineString)
            
            Log.d(TAG, "Created simple beige LineString with ${lineString.coordinates().size} coordinates")
            
            // Add reduced beige polyline source
            style.addSource(
                geoJsonSource("reduced-beige-route-line") {
                    feature(routeFeature)
                }
            )
            Log.d(TAG, "Added simple beige route line source")
            
            // Add reduced beige polyline layer
            style.addLayer(
                lineLayer("reduced-beige-route-line-layer", "reduced-beige-route-line") {
                    lineColor("#D2B48C") // Beige color for reduced route
                    lineWidth(4.0)
                    lineOpacity(0.9)
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                }
            )
            Log.d(TAG, "Added simple beige route line layer")
            
            Log.d(TAG, "Successfully drew simple beige line")
        } catch (e: Exception) {
            Log.e(TAG, "Error drawing simple beige line", e)
        }
        
        Log.d(TAG, "=== drawSimpleBeigeLine END ===")
    }
    
    private fun analyzeTrafficConditions(route: com.mapbox.api.directions.v5.models.DirectionsRoute): String {
        Log.d(TAG, "=== analyzeTrafficConditions START ===")
        
        try {
            // Get route duration and distance
            val duration = route.duration() ?: 0.0
            val distance = route.distance() ?: 0.0
            
            if (distance == 0.0) {
                Log.e(TAG, "Route distance is 0, cannot analyze traffic")
                return "No Traffic"
            }
            
            // Calculate average speed (km/h)
            val averageSpeed = (distance / 1000.0) / (duration / 3600.0)
            Log.d(TAG, "Average speed: ${String.format("%.1f", averageSpeed)} km/h")
            
            // Analyze traffic based on average speed
            val trafficCondition = when {
                averageSpeed < 15.0 -> "Heavy Traffic" // Less than 15 km/h
                averageSpeed < 25.0 -> "Moderate Traffic" // 15-25 km/h
                else -> "No Traffic" // Above 25 km/h
            }
            
            Log.d(TAG, "Traffic analysis result: $trafficCondition (Speed: ${String.format("%.1f", averageSpeed)} km/h)")
            Log.d(TAG, "=== analyzeTrafficConditions END ===")
            
            return trafficCondition
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing traffic conditions", e)
            return "No Traffic"
        }
    }
    
    private fun updateTrafficInfo(trafficCondition: String) {
        Log.d(TAG, "=== updateTrafficInfo START ===")
        Log.d(TAG, "Updating traffic info: $trafficCondition")
        
        try {
            // Find the traffic info text view in the route info card
            val trafficInfoView = findViewById<TextView>(R.id.tvTrafficStatus)
            if (trafficInfoView != null) {
                // Update traffic info with color coding
                val (text, color) = when (trafficCondition) {
                    "Heavy Traffic" -> Pair("Heavy Traffic", "#F44336") // Red
                    "Moderate Traffic" -> Pair("Moderate Traffic", "#FF9800") // Orange
                    "No Traffic" -> Pair("No Traffic", "#4CAF50") // Green
                    else -> Pair("Unknown", "#9E9E9E") // Gray
                }
                
                trafficInfoView.text = text
                trafficInfoView.setTextColor(android.graphics.Color.parseColor(color))
                
                Log.d(TAG, "Updated traffic info: $text with color $color")
            } else {
                Log.w(TAG, "Traffic info view not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating traffic info", e)
        }
        
        Log.d(TAG, "=== updateTrafficInfo END ===")
    }
    
    
    
    
    
    
    
    
    
    
    private fun showRouteInfoWithSequence(route: RouteDetails, waypointSequence: List<RouteDetailsPin>) {
        // Update the route info card with calculated metrics from closest waypoint to destination
        if (closestWaypoint == null) {
            Log.e(TAG, "No closest waypoint to show route info")
            return
        }
        
        // Find destination pin
        val destPin = route.mapDetails?.pins?.find { pin ->
            Math.abs(pin.lat - endLat) < 0.0001 && Math.abs(pin.lng - endLng) < 0.0001
        }
        
        if (destPin == null) {
            Log.e(TAG, "No destination pin found")
            return
        }
        
        // Calculate metrics from closest waypoint to destination
        val closestToDestMetrics = calculateClosestToDestinationMetrics(closestWaypoint!!, destPin, waypointSequence)
        
        // Update routes info card
        updateRoutesInfoCard(closestWaypoint!!, destPin, closestToDestMetrics)
        
        Log.d(TAG, "Route info updated: ${closestWaypoint!!.name} to ${destPin.name}, ${String.format("%.1f", closestToDestMetrics.distanceKm)} km, ${closestToDestMetrics.etaMinutes} min, ₱${String.format("%.0f", closestToDestMetrics.fare)}, ${closestToDestMetrics.stops} stops")
    }
    
    private fun calculateClosestToDestinationMetrics(closestWaypoint: RouteDetailsPin, destPin: RouteDetailsPin, waypointSequence: List<RouteDetailsPin>): RouteMetrics {
        Log.d(TAG, "=== calculateClosestToDestinationMetrics START ===")
        Log.d(TAG, "Calculating metrics from ${closestWaypoint.name} to ${destPin.name}")
        
        // Find the waypoints between closest waypoint and destination
        val routeWaypoints = findWaypointsBetween(closestWaypoint, destPin, waypointSequence)
        Log.d(TAG, "Found ${routeWaypoints.size} waypoints between closest and destination: ${routeWaypoints.map { it.name }}")
        
        // Calculate total distance
        var totalDistance = 0.0
        
        // Distance from closest waypoint to first intermediate waypoint
        if (routeWaypoints.isNotEmpty()) {
            val firstWaypoint = routeWaypoints.first()
            val distance = computeHaversineMeters(closestWaypoint.lat, closestWaypoint.lng, firstWaypoint.lat, firstWaypoint.lng) / 1000.0
            totalDistance += distance
            Log.d(TAG, "Distance from ${closestWaypoint.name} to ${firstWaypoint.name}: ${String.format("%.2f", distance)} km")
        }
        
        // Distance between consecutive waypoints
        for (i in 0 until routeWaypoints.size - 1) {
            val current = routeWaypoints[i]
            val next = routeWaypoints[i + 1]
            val distance = computeHaversineMeters(current.lat, current.lng, next.lat, next.lng) / 1000.0
            totalDistance += distance
            Log.d(TAG, "Distance from ${current.name} to ${next.name}: ${String.format("%.2f", distance)} km")
        }
        
        // Distance from last waypoint to destination
        if (routeWaypoints.isNotEmpty()) {
            val lastWaypoint = routeWaypoints.last()
            val distance = computeHaversineMeters(lastWaypoint.lat, lastWaypoint.lng, destPin.lat, destPin.lng) / 1000.0
            totalDistance += distance
            Log.d(TAG, "Distance from ${lastWaypoint.name} to ${destPin.name}: ${String.format("%.2f", distance)} km")
        } else {
            // Direct distance from closest waypoint to destination
            val distance = computeHaversineMeters(closestWaypoint.lat, closestWaypoint.lng, destPin.lat, destPin.lng) / 1000.0
            totalDistance = distance
            Log.d(TAG, "Direct distance from ${closestWaypoint.name} to ${destPin.name}: ${String.format("%.2f", distance)} km")
        }
        
        // Calculate time and fare
        val time = calculateTotalTime(totalDistance)
        val fare = calculateTotalFare(totalDistance)
        
        // Number of stops is the number of intermediate waypoints
        val stops = routeWaypoints.size
        
        Log.d(TAG, "Total metrics: ${String.format("%.2f", totalDistance)} km, ${time} min, ₱${String.format("%.0f", fare)}, ${stops} stops")
        Log.d(TAG, "=== calculateClosestToDestinationMetrics END ===")
        
        return RouteMetrics(
            distanceKm = totalDistance,
            etaMinutes = time,
            fare = fare,
            trafficStatus = "Normal",
            stops = stops
        )
    }
    
    private fun findWaypointsBetween(closestWaypoint: RouteDetailsPin, destPin: RouteDetailsPin, waypointSequence: List<RouteDetailsPin>): List<RouteDetailsPin> {
        Log.d(TAG, "Finding waypoints between ${closestWaypoint.name} and ${destPin.name}")
        
        // Find the indices of closest waypoint and destination in the sequence
        val closestIndex = waypointSequence.indexOfFirst { it.id == closestWaypoint.id }
        val destIndex = waypointSequence.indexOfFirst { it.id == destPin.id }
        
        Log.d(TAG, "Closest waypoint index: $closestIndex, Destination index: $destIndex")
        
        if (closestIndex == -1 || destIndex == -1) {
            Log.w(TAG, "Could not find waypoints in sequence")
            return emptyList()
        }
        
        // Return waypoints between closest and destination (excluding both endpoints)
        return when {
            closestIndex < destIndex -> {
                // Forward direction
                waypointSequence.subList(closestIndex + 1, destIndex)
            }
            closestIndex > destIndex -> {
                // Reverse direction
                waypointSequence.subList(destIndex + 1, closestIndex).reversed()
            }
            else -> {
                // Same waypoint
                emptyList()
            }
        }
    }
    
    private fun showRouteInfo(route: RouteDetails, closestWaypoint: RouteDetailsPin?, destPin: RouteDetailsPin?) {
        // Update the route info card with calculated metrics
        val pins = route.mapDetails?.pins ?: return
        
        // Calculate total distance and time
        val totalDistance = calculateTotalDistance(pins)
        val totalTime = calculateTotalTime(totalDistance)
        val totalFare = calculateTotalFare(totalDistance)
        
        // Route details are now handled by the routes info card
        // No need to update individual UI elements since they were removed
    }
    
    private fun calculateTotalDistanceFromSequence(waypointSequence: List<RouteDetailsPin>): Double {
        if (waypointSequence.isEmpty()) return 0.0
        
        var totalDistance = 0.0
        
        // Distance from current location to first waypoint
        if (currentLat != 0.0 && currentLng != 0.0) {
            val firstWaypoint = waypointSequence.first()
            totalDistance += computeHaversineMeters(currentLat, currentLng, firstWaypoint.lat, firstWaypoint.lng) / 1000.0
            Log.d(TAG, "Distance from current location to ${firstWaypoint.name}: ${String.format("%.2f", totalDistance)} km")
        }
        
        // Distance between consecutive waypoints in sequence
        for (i in 0 until waypointSequence.size - 1) {
            val current = waypointSequence[i]
            val next = waypointSequence[i + 1]
            val distance = computeHaversineMeters(current.lat, current.lng, next.lat, next.lng) / 1000.0
            totalDistance += distance
            Log.d(TAG, "Distance from ${current.name} to ${next.name}: ${String.format("%.2f", distance)} km")
        }
        
        Log.d(TAG, "Total distance for waypoint sequence: ${String.format("%.2f", totalDistance)} km")
        return totalDistance
    }
    
    private fun calculateTotalDistance(pins: List<RouteDetailsPin>): Double {
        if (pins.isEmpty()) return 0.0
        
        val sortedPins = pins.sortedBy { it.number }
        var totalDistance = 0.0
        
        // Distance from current location to first pin
        if (currentLat != 0.0 && currentLng != 0.0 && sortedPins.isNotEmpty()) {
            totalDistance += computeHaversineMeters(currentLat, currentLng, sortedPins[0].lat, sortedPins[0].lng) / 1000.0
        }
        
        // Distance between consecutive pins
        for (i in 0 until sortedPins.size - 1) {
            val distance = computeHaversineMeters(
                sortedPins[i].lat, sortedPins[i].lng,
                sortedPins[i + 1].lat, sortedPins[i + 1].lng
            ) / 1000.0
            totalDistance += distance
        }
        
        return totalDistance
    }
    
    private fun calculateTotalTime(distanceKm: Double): Int {
        // Assume average speed of 30 km/h
        return (distanceKm / 30.0 * 60.0).toInt()
    }
    
    private fun calculateTotalFare(distanceKm: Double): Double {
        // 15 pesos base for first 1 kilometer, 2 pesos for exceeding kilometers
        val base = 15.0
        val extra = if (distanceKm > 1.0) (distanceKm - 1.0) * 2.0 else 0.0
        return base + extra
    }
    
    
    private fun addRoutePinsWithSequence(route: RouteDetails) {
        val pins = route.mapDetails?.pins
        if (pins.isNullOrEmpty() || closestWaypoint == null) {
            Log.e(TAG, "No pins found in route data")
            return
        }
        
        // Find destination pin
        val destPin = pins.find { pin -> 
            Math.abs(pin.lat - endLat) < 0.0001 && Math.abs(pin.lng - endLng) < 0.0001 
        }
        
        if (destPin == null) {
            Log.e(TAG, "Destination pin not found in route data")
            return
        }   
        
        // Create waypoint sequence from closest waypoint to destination
        val waypointSequence = createWaypointSequence(pins, closestWaypoint!!, destPin)
        if (waypointSequence.isEmpty()) {
            Log.e(TAG, "No waypoint sequence created")
            return
        }
        
        Log.d(TAG, "Created waypoint sequence with ${waypointSequence.size} pins")

        val pinFeatures = waypointSequence.mapIndexed { index, pin ->
            Feature.fromGeometry(Point.fromLngLat(pin.lng, pin.lat)).apply {
                addStringProperty("name", pin.name)
                addStringProperty("address", pin.address ?: pin.placeName ?: "")
                addNumberProperty("number", pin.number)
                addStringProperty("type", when {
                    pin == closestWaypoint -> "start"
                    pin == destPin -> "destination"
                    else -> "waypoint"
                })
                addNumberProperty("sequence", index + 1)
            }
        }
        
        mapView.getMapboxMap().getStyle { style ->
            try {
                // Add custom pin image
                val bmp = vectorToBitmap(R.drawable.ic_custom_pin)
                if (bmp != null) style.addImage("route-pin-icon", bmp)
                
                // Source
                style.addSource(
                    geoJsonSource("route-pins") {
                        featureCollection(FeatureCollection.fromFeatures(pinFeatures))
                    }
                )
                
                if (bmp != null) {
                    // Pins layer
                    style.addLayer(
                        symbolLayer("route-pins-layer", "route-pins") {
                            iconImage("route-pin-icon")
                            iconSize(0.8)
                            iconAllowOverlap(true)
                            iconIgnorePlacement(true)
                        }
                    )
                } else {
                    // Fallback: draw circles if icon missing
                    style.addLayer(
                        com.mapbox.maps.extension.style.layers.generated.circleLayer("route-pins-layer", "route-pins") {
                            circleColor("#D32F2F")
                            circleRadius(6.0)
                            circleStrokeColor("#FFFFFF")
                            circleStrokeWidth(2.0)
                        }
                    )
                }
                
                // Text labels above pins
                style.addLayer(
                    symbolLayer("route-pins-text-layer", "route-pins") {
                        textField("{name}")
                        textSize(14.0)
                        textColor("#000000")
                        textHaloColor("#FFFFFF")
                        textHaloWidth(4.0)
                        textHaloBlur(0.7)
                        textOffset(listOf(0.0, -2.4))
                        textAllowOverlap(true)
                        textIgnorePlacement(true)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add pins", e)
            }
        }
    }
    
    private fun createWaypointSequenceFromClosestToDestination(allPins: List<RouteDetailsPin>, closestPin: RouteDetailsPin?, destPin: RouteDetailsPin?): List<RouteDetailsPin> {
        if (closestPin == null || destPin == null) {
            Log.e(TAG, "Missing closest pin or destination pin")
            return allPins // Return all pins if we can't determine sequence
        }
        
        // Sort pins by their number to get the correct sequence
        val sortedPins = allPins.sortedBy { it.number }
        
        val closestIndex = sortedPins.indexOf(closestPin)
        val destIndex = sortedPins.indexOf(destPin)
        
        if (closestIndex == -1 || destIndex == -1) {
            Log.e(TAG, "Invalid indices: closest=$closestIndex, dest=$destIndex")
            return allPins
        }
        
        // Create sequence from closest waypoint to destination
        val result = if (closestIndex <= destIndex) {
            // Normal sequence: closest → ... → destination
            sortedPins.subList(closestIndex, destIndex + 1)
        } else {
            // Reverse sequence: closest → ... → destination (going backwards)
            sortedPins.subList(destIndex, closestIndex + 1).reversed()
        }
        
        Log.d(TAG, "Created waypoint sequence from closest to destination: ${result.map { "${it.name} (${it.number})" }}")
        return result
    }
    
    private fun createWaypointSequenceToCurrentLocation(allPins: List<RouteDetailsPin>, nearestPin: RouteDetailsPin?): List<RouteDetailsPin> {
        if (nearestPin == null) {
            Log.e(TAG, "Missing nearest pin")
            return allPins // Return all pins if we can't determine sequence
        }
        
        // Sort pins by their number to get the correct sequence
        val sortedPins = allPins.sortedBy { it.number }
        
        val nearestIndex = sortedPins.indexOf(nearestPin)
        if (nearestIndex == -1) {
            Log.e(TAG, "Nearest pin not found in sorted pins")
            return allPins
        }
        
        // Return all pins from the nearest pin to the end (including the nearest pin)
        // This creates a sequence from closest waypoint to the last waypoint
        val result = sortedPins.subList(nearestIndex, sortedPins.size)
        
        Log.d(TAG, "Created waypoint sequence to current location: ${result.map { "${it.name} (${it.number})" }}")
        return result
    }
    
    private fun createWaypointSequenceFromNearestToDestination(allPins: List<RouteDetailsPin>, nearestPin: RouteDetailsPin?, destPin: RouteDetailsPin?): List<RouteDetailsPin> {
        if (nearestPin == null || destPin == null) {
            Log.e(TAG, "Missing nearest pin or destination pin")
            return allPins // Return all pins if we can't determine sequence
        }
        
        // Sort pins by their number to get the correct sequence
        val sortedPins = allPins.sortedBy { it.number }
        
        val nearestIndex = sortedPins.indexOf(nearestPin)
        val destIndex = sortedPins.indexOf(destPin)
        
        if (nearestIndex == -1 || destIndex == -1) {
            Log.e(TAG, "Invalid indices: nearest=$nearestIndex, dest=$destIndex")
            return allPins // Return all pins if we can't find the specific pins
        }
        
        // Create sequence from nearest waypoint to destination
        val result = if (nearestIndex <= destIndex) {
            // Normal sequence: nearest -> ... -> destination
            sortedPins.subList(nearestIndex, destIndex + 1)
        } else {
            // Reverse sequence: nearest -> ... -> destination (going backwards)
            sortedPins.subList(destIndex, nearestIndex + 1).reversed()
        }
        
        Log.d(TAG, "Created waypoint sequence from nearest to destination: ${result.map { "${it.name} (${it.number})" }}")
        return result
    }
    
    private fun createWaypointSequence(allPins: List<RouteDetailsPin>, startPin: RouteDetailsPin, destPin: RouteDetailsPin): List<RouteDetailsPin> {
        // Sort pins by their number to get the correct sequence
        val sortedPins = allPins.sortedBy { it.number }
        
        val startIndex = sortedPins.indexOf(startPin)
        val destIndex = sortedPins.indexOf(destPin)
        
        if (startIndex == -1 || destIndex == -1) {
            Log.e(TAG, "Invalid indices: start=$startIndex, dest=$destIndex")
            return emptyList()
        }
        
        // Return pins from start to destination (inclusive)
        val result = if (startIndex <= destIndex) {
            sortedPins.subList(startIndex, destIndex + 1)
        } else {
            sortedPins.subList(destIndex, startIndex + 1).reversed()
        }
        
        Log.d(TAG, "Created waypoint sequence: ${result.map { "${it.name} (${it.number})" }}")
        return result
    }
    
    private fun fitCameraToRouteWithCurrentLocation(route: RouteDetails) {
        val pins = route.mapDetails?.pins
        if (pins == null || pins.isEmpty()) return
        
        var lats = pins.map { it.lat }.toMutableList()
        var lngs = pins.map { it.lng }.toMutableList()
        
        // Include current location in bounds if available
        if (currentLat != 0.0 && currentLng != 0.0) {
            lats.add(currentLat)
            lngs.add(currentLng)
        }
        
        val minLat = lats.minOrNull() ?: 0.0
        val maxLat = lats.maxOrNull() ?: 0.0
        val minLng = lngs.minOrNull() ?: 0.0
        val maxLng = lngs.maxOrNull() ?: 0.0
        
        val centerLat = (minLat + maxLat) / 2
        val centerLng = (minLng + maxLng) / 2
        val centerPoint = Point.fromLngLat(centerLng, centerLat)
        
        val maxRange = maxOf(maxLat - minLat, maxLng - minLng)
        val zoom = when {
            maxRange > 0.1 -> 10.0
            maxRange > 0.05 -> 12.0
            maxRange > 0.02 -> 14.0
            else -> 16.0
        }
        
        mapView.getMapboxMap().setCamera(
            com.mapbox.maps.CameraOptions.Builder().center(centerPoint).zoom(zoom).build()
        )
    }
    
    private fun showClosestWaypointInfo(route: RouteDetails) {
        if (closestWaypoint == null) return
        
        val destPin = route.mapDetails?.pins?.find { pin -> 
            Math.abs(pin.lat - endLat) < 0.0001 && Math.abs(pin.lng - endLng) < 0.0001 
        }
        
        if (destPin == null) return
        
        val waypointSequence = createWaypointSequence(route.mapDetails?.pins ?: emptyList(), closestWaypoint!!, destPin)
        if (waypointSequence.isEmpty()) return
        
        val calculations = calculateRouteMetricsFromCurrentLocation(waypointSequence)
        
        // Route details are now handled by the routes info card
        // No need to update individual UI elements since they were removed
    }
    
    
    private fun calculateRouteMetricsFromCurrentLocation(waypointSequence: List<RouteDetailsPin>): RouteMetrics {
        var totalDistance = 0.0
        var totalTime = 0.0
        
        if (waypointSequence.isEmpty()) {
            return RouteMetrics(0.0, 0, 0.0, "None")
        }
        
        // Calculate distance and time from current location through waypoints to destination
        
        // 1. Distance from current location to first waypoint (closest waypoint)
        if (currentLat != 0.0 && currentLng != 0.0) {
            val firstWaypoint = waypointSequence.first()
            val distanceToFirstWaypoint = computeHaversineMeters(currentLat, currentLng, firstWaypoint.lat, firstWaypoint.lng)
            totalDistance += distanceToFirstWaypoint
            
            val timeHours = (distanceToFirstWaypoint / 1000.0) / 30.0
            totalTime += timeHours
            
            Log.d(TAG, "Added distance from current location to first waypoint (${firstWaypoint.name}): ${distanceToFirstWaypoint/1000.0}km")
        }
        
        // 2. Distance between consecutive waypoints in sequence
        for (i in 0 until waypointSequence.size - 1) {
            val current = waypointSequence[i]
            val next = waypointSequence[i + 1]
            val distance = computeHaversineMeters(current.lat, current.lng, next.lat, next.lng)
            totalDistance += distance
            
            val timeHours = (distance / 1000.0) / 30.0 // 30 km/h average speed
            totalTime += timeHours
            
            Log.d(TAG, "Added distance from ${current.name} to ${next.name}: ${distance/1000.0}km")
        }
        
        val distanceKm = totalDistance / 1000.0
        val etaMinutes = (totalTime * 60).toInt()
        
        // Calculate fare: ₱15 for first kilometer, ₱5 for exceeding kilometers
        val fare = calculateTotalFare(distanceKm)
        
        // Determine traffic status based on time and distance
        val trafficStatus = when {
            etaMinutes > 60 -> "Heavy"
            etaMinutes > 30 -> "Moderate"
            else -> "Light"
        }
        
        Log.d(TAG, "Calculated metrics from current location to destination: ${distanceKm}km, ${etaMinutes}min, ₱${fare}")
        
        return RouteMetrics(distanceKm, etaMinutes, fare, trafficStatus)
    }
    
    data class RouteMetrics(
        val distanceKm: Double,
        val etaMinutes: Int,
        val fare: Double,
        val trafficStatus: String,
        val stops: Int = 0 // number of stops
    )
    
    
    private fun setupRoutesInfoCard() {
        try {
            btnCollapseRoutes.setOnClickListener {
                val isVisible = routesContentContainer.visibility == View.VISIBLE
                routesContentContainer.visibility = if (isVisible) View.GONE else View.VISIBLE
                btnCollapseRoutes.setImageResource(if (isVisible) R.drawable.ic_expand_more else R.drawable.ic_expand_less)
            }
        } catch (_: Exception) { }
    }
    
    private fun updateRoutesInfoCard(closestWaypoint: RouteDetailsPin, destPin: RouteDetailsPin, metrics: RouteMetrics) {
        try {
            // Show the routes info card
            routesInfoCard.visibility = View.VISIBLE

            // Update route information with current waypoint progression
            updateCurrentToWaypointText()
            tvDestinationInfo.text = destPin.name
            tvDistanceInfo.text = "Distance: ${String.format("%.1f", metrics.distanceKm)} km"
            tvEtaInfo.text = "ETA: ${metrics.etaMinutes} min"
            tvFareInfo.text = "₱${String.format("%.0f", metrics.fare)}"
            // Traffic status will be updated by traffic analysis - no manual toggle
            tvWaypointCount.text = "${metrics.stops} stops"

        } catch (e: Exception) {
            Log.e(TAG, "Error updating routes info card", e)
        }
    }
    
    private fun updateCurrentToWaypointText() {
        try {
            if (waypointSequence.isEmpty()) {
                tvCurrentToWaypoint.text = "Current Location → Destination"
                return
            }
            
            if (currentWaypointIndex < waypointSequence.size) {
                val currentWaypoint = waypointSequence[currentWaypointIndex]
                tvCurrentToWaypoint.text = "Current Location → ${currentWaypoint.name}"
            } else {
                // All waypoints completed, heading to destination
                tvCurrentToWaypoint.text = "Current Location → ${destinationPin?.name ?: "Destination"}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating current to waypoint text", e)
        }
    }
    
    private fun setupPinClickListener() {
        mapView.gestures.addOnMapClickListener { tappedPoint ->
            val pins = selectedRoute?.mapDetails?.pins
            if (pins.isNullOrEmpty()) return@addOnMapClickListener true
            
            // Find nearest pin by haversine distance
            var nearest: RouteDetailsPin = pins.first()
            var best = Double.MAX_VALUE
            for (p in pins) {
                val d = computeHaversineMeters(
                    tappedPoint.latitude(), tappedPoint.longitude(), p.lat, p.lng
                )
                if (d < best) { 
                    best = d
                    nearest = p 
                }
            }
            showToastTooltip(nearest)
            true
        }
    }

    private fun showToastTooltip(pin: RouteDetailsPin) {
        val text = "${pin.name}\n${pin.address ?: pin.placeName ?: "Address not available"}"
        val toast = Toast.makeText(this, text, Toast.LENGTH_LONG)
        val view = toast.view
        try {
            view?.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
            val padding = (8 * resources.displayMetrics.density).toInt()
            if (view is android.widget.LinearLayout && view.childCount > 0 && view.getChildAt(0) is TextView) {
                val tv = view.getChildAt(0) as TextView
                tv.setTextColor(android.graphics.Color.parseColor("#000000"))
                tv.setPadding(padding, padding, padding, padding)
            }
        } catch (_: Exception) { }
        toast.show()
    }
    
    private fun createGeofenceForDestination(point: Point) {
        Log.d(TAG, "=== createGeofenceForDestination START ===")
        Log.d(TAG, "Creating geofence for destination: ${point.latitude()}, ${point.longitude()}")
        
        try {
            val appSettings = com.example.gzingapp.utils.AppSettings(this)
            
            // Check if route geofence is enabled
            if (!appSettings.isRouteGeofenceEnabled()) {
                Log.d(TAG, "Route geofence is disabled in settings")
                return
            }
            
            val radius = appSettings.getRouteGeofenceRadiusMeters().toFloat()
            Log.d(TAG, "Geofence radius: ${radius}m")
            
            // Check location permissions
            if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Location permission not granted for geofence creation")
                return
            }
            
            // Check background location permission for Android 10+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val bgGranted = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (!bgGranted) {
                    Log.w(TAG, "Background location permission not granted - geofence may not work properly")
                }
            }
            
            val geofencingClient = com.google.android.gms.location.LocationServices.getGeofencingClient(this)
            
            // Create geofence
            val geofence = com.google.android.gms.location.Geofence.Builder()
                .setRequestId("route_destination_geofence_${System.currentTimeMillis()}") // Unique ID
                .setCircularRegion(point.latitude(), point.longitude(), radius)
                .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER or
                    com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .build()
            
            // Create geofencing request
            val request = com.google.android.gms.location.GeofencingRequest.Builder()
                .setInitialTrigger(com.google.android.gms.location.GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
            
            // Create pending intent for geofence events
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                this,
                3011,
                android.content.Intent(this, com.example.gzingapp.GeofenceBroadcastReceiver::class.java).apply {
                    putExtra("geofence_type", "destination")
                    putExtra("destination_name", destName)
                    putExtra("destination_lat", point.latitude())
                    putExtra("destination_lng", point.longitude())
                },
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            // Remove existing geofences first
            geofencingClient.removeGeofences(pendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully removed existing geofences")
                    
                    // Add new geofence
            geofencingClient.addGeofences(request, pendingIntent)
                        .addOnSuccessListener {
                            Log.d(TAG, "✅ Destination geofence created successfully!")
                            Log.d(TAG, "Location: ${point.latitude()}, ${point.longitude()}")
                            Log.d(TAG, "Radius: ${radius}m")
                            Log.d(TAG, "Destination: $destName")
                            
                            // Show success message to user
                            Toast.makeText(this, "Geofence created for destination: $destName", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to add destination geofence", e)
                            Toast.makeText(this, "Failed to create geofence for destination", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to remove existing geofences", e)
                }
                
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create destination geofence", e)
            e.printStackTrace()
            Toast.makeText(this, "Error creating geofence: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        
        Log.d(TAG, "=== createGeofenceForDestination END ===")
    }
    
    private fun addGeofenceCircleToMap(centerPoint: Point, style: Style) {
        Log.d(TAG, "=== addGeofenceCircleToMap START ===")
        Log.d(TAG, "Adding geofence circle at: ${centerPoint.latitude()}, ${centerPoint.longitude()}")
        
        try {
            val appSettings = com.example.gzingapp.utils.AppSettings(this)
            val radius = appSettings.getRouteGeofenceRadiusMeters().toFloat()
            
            // Create a circle around the destination point
            val circle = com.mapbox.geojson.Polygon.fromLngLats(
                listOf(
                    createCircleCoordinates(centerPoint.longitude(), centerPoint.latitude(), radius.toDouble())
                )
            )
            
            // Remove existing geofence circle if it exists
            if (style.styleLayerExists("geofence-circle-layer")) {
                style.removeStyleLayer("geofence-circle-layer")
                Log.d(TAG, "Removed existing geofence-circle-layer")
            }
            if (style.styleSourceExists("geofence-circle")) {
                style.removeStyleSource("geofence-circle")
                Log.d(TAG, "Removed existing geofence-circle source")
            }
            
            // Add geofence circle source
            style.addSource(
                geoJsonSource("geofence-circle") {
                    feature(Feature.fromGeometry(circle))
                }
            )
            Log.d(TAG, "Added geofence circle source")
            
            // Add geofence circle layer
            style.addLayer(
                com.mapbox.maps.extension.style.layers.generated.fillLayer("geofence-circle-layer", "geofence-circle") {
                    fillColor("#FF5722") // Orange color for geofence
                    fillOpacity(0.2) // Semi-transparent
                }
            )
            Log.d(TAG, "Added geofence circle layer")
            
            // Add geofence circle border
            style.addLayer(
                com.mapbox.maps.extension.style.layers.generated.lineLayer("geofence-circle-border-layer", "geofence-circle") {
                    lineColor("#FF5722") // Orange color for border
                    lineWidth(3.0)
                    lineOpacity(0.8)
                }
            )
            Log.d(TAG, "Added geofence circle border layer")
            
            Log.d(TAG, "✅ Successfully added geofence circle to map")
            Log.d(TAG, "Center: ${centerPoint.latitude()}, ${centerPoint.longitude()}")
            Log.d(TAG, "Radius: ${radius}m")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add geofence circle to map", e)
            e.printStackTrace()
        }
        
        Log.d(TAG, "=== addGeofenceCircleToMap END ===")
    }
    
    private fun createCircleCoordinates(centerLng: Double, centerLat: Double, radiusMeters: Double): List<Point> {
        val coordinates = mutableListOf<Point>()
        val earthRadius = 6371000.0 // Earth radius in meters
        val angularDistance = radiusMeters / earthRadius
        
        // Create circle with 64 points for smooth appearance
        for (i in 0..63) {
            val angle = (i * 2 * Math.PI) / 64
            val lat = Math.asin(
                Math.sin(centerLat * Math.PI / 180) * Math.cos(angularDistance) +
                Math.cos(centerLat * Math.PI / 180) * Math.sin(angularDistance) * Math.cos(angle)
            ) * 180 / Math.PI
            val lng = centerLng + Math.atan2(
                Math.sin(angle) * Math.sin(angularDistance) * Math.cos(centerLat * Math.PI / 180),
                Math.cos(angularDistance) - Math.sin(centerLat * Math.PI / 180) * Math.sin(lat * Math.PI / 180)
            ) * 180 / Math.PI
            
            coordinates.add(Point.fromLngLat(lng, lat))
        }
        
        // Close the circle by adding the first point again
        coordinates.add(coordinates.first())
        
        return coordinates
    }
    
    private fun clearMapAnnotations(style: Style) {
        Log.d(TAG, "=== clearMapAnnotations START ===")
        try {
            // Clear pins (same as RoutesActivity)
            if (style.styleSourceExists("route-pins")) {
                style.removeStyleSource("route-pins")
                Log.d(TAG, "Cleared route-pins source")
            }
            if (style.styleLayerExists("route-pins-layer")) {
                style.removeStyleLayer("route-pins-layer")
                Log.d(TAG, "Cleared route-pins-layer")
            }
            if (style.styleLayerExists("route-pins-text-layer")) {
                style.removeStyleLayer("route-pins-text-layer")
                Log.d(TAG, "Cleared route-pins-text-layer")
            }
            
            // Clear route lines (same as RoutesActivity)
            if (style.styleSourceExists("route-line")) {
                style.removeStyleSource("route-line")
                Log.d(TAG, "Cleared route-line source")
            }
            if (style.styleLayerExists("route-line-layer")) {
                style.removeStyleLayer("route-line-layer")
                Log.d(TAG, "Cleared route-line-layer")
            }
            
            // Clear API response polyline only if not in navigation mode
            // Keep beige polyline visible during navigation for reference
            if (!isNavigationActive) {
                if (style.styleSourceExists("api-route-line")) {
                    style.removeStyleSource("api-route-line")
                    Log.d(TAG, "Cleared api-route-line source")
                }
                if (style.styleLayerExists("api-route-line-layer")) {
                    style.removeStyleLayer("api-route-line-layer")
                    Log.d(TAG, "Cleared api-route-line-layer")
                }
            } else {
                Log.d(TAG, "Keeping API response polyline visible during navigation")
            }
            
            // Clear reduced beige polyline
            if (style.styleSourceExists("reduced-beige-route-line")) {
                style.removeStyleSource("reduced-beige-route-line")
                Log.d(TAG, "Cleared reduced-beige-route-line source")
            }
            if (style.styleLayerExists("reduced-beige-route-line-layer")) {
                style.removeStyleLayer("reduced-beige-route-line-layer")
                Log.d(TAG, "Cleared reduced-beige-route-line-layer")
            }
            
            // Clear new polyline layers
            if (style.styleSourceExists("main-route-line")) {
                style.removeStyleSource("main-route-line")
                Log.d(TAG, "Cleared main-route-line source")
            }
            if (style.styleLayerExists("main-route-line-layer")) {
                style.removeStyleLayer("main-route-line-layer")
                Log.d(TAG, "Cleared main-route-line-layer")
            }
            if (style.styleSourceExists("current-to-closest")) {
                style.removeStyleSource("current-to-closest")
                Log.d(TAG, "Cleared current-to-closest source")
            }
            if (style.styleLayerExists("current-to-closest-layer")) {
                style.removeStyleLayer("current-to-closest-layer")
                Log.d(TAG, "Cleared current-to-closest-layer")
            }
            if (style.styleSourceExists("original-route-line")) {
                style.removeStyleSource("original-route-line")
                Log.d(TAG, "Cleared original-route-line source")
            }
            if (style.styleLayerExists("original-route-line-layer")) {
                style.removeStyleLayer("original-route-line-layer")
                Log.d(TAG, "Cleared original-route-line-layer")
            }
            if (style.styleSourceExists("manual-route-line")) {
                style.removeStyleSource("manual-route-line")
                Log.d(TAG, "Cleared manual-route-line source")
            }
            if (style.styleLayerExists("manual-route-line-layer")) {
                style.removeStyleLayer("manual-route-line-layer")
                Log.d(TAG, "Cleared manual-route-line-layer")
            }
            if (style.styleSourceExists("mapbox-directions")) {
                style.removeStyleSource("mapbox-directions")
                Log.d(TAG, "Cleared mapbox-directions source")
            }
            if (style.styleLayerExists("mapbox-directions-layer")) {
                style.removeStyleLayer("mapbox-directions-layer")
                Log.d(TAG, "Cleared mapbox-directions-layer")
            }
            if (style.styleSourceExists("test-route-line")) {
                style.removeStyleSource("test-route-line")
                Log.d(TAG, "Cleared test-route-line source")
            }
            if (style.styleLayerExists("test-route-line-layer")) {
                style.removeStyleLayer("test-route-line-layer")
                Log.d(TAG, "Cleared test-route-line-layer")
            }
            if (style.styleSourceExists("complete-route-line")) {
                style.removeStyleSource("complete-route-line")
                Log.d(TAG, "Cleared complete-route-line source")
            }
            if (style.styleLayerExists("complete-route-line-layer")) {
                style.removeStyleLayer("complete-route-line-layer")
                Log.d(TAG, "Cleared complete-route-line-layer")
            }
            if (style.styleSourceExists("polyline-sequence")) {
                style.removeStyleSource("polyline-sequence")
                Log.d(TAG, "Cleared polyline-sequence source")
            }
            if (style.styleLayerExists("polyline-sequence-layer")) {
                style.removeStyleLayer("polyline-sequence-layer")
                Log.d(TAG, "Cleared polyline-sequence-layer")
            }
            
            // Note: Using Mapbox built-in location puck, no custom current location marker to clear
            
            // Clear geofence circle
            if (style.styleSourceExists("geofence-circle")) {
                style.removeStyleSource("geofence-circle")
                Log.d(TAG, "Cleared geofence-circle source")
            }
            if (style.styleLayerExists("geofence-circle-layer")) {
                style.removeStyleLayer("geofence-circle-layer")
                Log.d(TAG, "Cleared geofence-circle-layer")
            }
            if (style.styleLayerExists("geofence-circle-border-layer")) {
                style.removeStyleLayer("geofence-circle-border-layer")
                Log.d(TAG, "Cleared geofence-circle-border-layer")
            }
            
            // Clear navigation location marker
            if (style.styleSourceExists("navigation-location")) {
                style.removeStyleSource("navigation-location")
                Log.d(TAG, "Cleared navigation-location source")
            }
            if (style.styleLayerExists("navigation-location-layer")) {
                style.removeStyleLayer("navigation-location-layer")
                Log.d(TAG, "Cleared navigation-location-layer")
            }
            
            // Clear any additional sources that might exist
            if (style.styleSourceExists("route-pins-backup")) {
                style.removeStyleSource("route-pins-backup")
                Log.d(TAG, "Cleared route-pins-backup source")
            }
            if (style.styleLayerExists("route-pins-backup")) {
                style.removeStyleLayer("route-pins-backup")
                Log.d(TAG, "Cleared route-pins-backup layer")
            }
            if (style.styleSourceExists("test-marker")) {
                style.removeStyleSource("test-marker")
                Log.d(TAG, "Cleared test-marker source")
            }
            if (style.styleLayerExists("test-marker-layer")) {
                style.removeStyleLayer("test-marker-layer")
                Log.d(TAG, "Cleared test-marker-layer")
            }
            if (style.styleSourceExists("route-polyline")) {
                style.removeStyleSource("route-polyline")
                Log.d(TAG, "Cleared route-polyline source")
            }
            if (style.styleLayerExists("route-polyline-layer")) {
                style.removeStyleLayer("route-polyline-layer")
                Log.d(TAG, "Cleared route-polyline-layer")
            }
            
            Log.d(TAG, "Cleared existing map annotations")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing map annotations", e)
            e.printStackTrace()
        }
        Log.d(TAG, "=== clearMapAnnotations END ===")
    }
    
    private fun computeHaversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
    
    private fun estimateEtaMinutes(distanceMeters: Double, mode: String): Double {
        val speed = when (mode) {
            "Car" -> 13.9
            "Walk" -> 1.4
            "Motor" -> 8.3
            else -> 13.9
        }
        return distanceMeters / speed / 60.0
    }

    private fun formatDistance(meters: Double): String {
        return if (meters >= 1000) "${String.format("%.1f", meters / 1000)} km" else "${meters.toInt()} m"
    }

    private fun estimateFare(km: Double): Double {
        val base = 15.0
        val extra = if (km > 1.0) (km - 1.0) * 5.0 else 0.0
        return base + extra
    }
    
    private fun startNavigationFromCurrentLocation() {
        if (closestWaypoint != null) {
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("navigation_mode", true)
                putExtra("start_lat", currentLat)
                putExtra("start_lng", currentLng)
                putExtra("end_lat", endLat)
                putExtra("end_lng", endLng)
                putExtra("destination_name", destName.ifBlank { "Destination" })
                putExtra("waypoint_lat", closestWaypoint!!.lat)
                putExtra("waypoint_lng", closestWaypoint!!.lng)
                putExtra("waypoint_name", closestWaypoint!!.name)
            }
            startActivity(intent)
        } else {
            showError("Unable to determine route waypoints")
        }
    }
    
    // ==================== NAVIGATION STATE METHODS ====================
    
    private fun startNavigationMode() {
        Log.d(TAG, "=== startNavigationMode START ===")
        
        if (closestWaypoint == null) {
            showError("Unable to determine route waypoints")
            return
        }
        
        // Check location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showError("Location permission required for navigation")
            return
        }
        
        // Generate navigation route and instructions
        generateNavigationRoute()
        
        // Start navigation state
        isNavigationActive = true
        navigationStartTime = System.currentTimeMillis()
        currentNavigationStep = 0
        hasAnnouncedArrival = false
        announcedWaypoints.clear()
        
        // Update UI to navigation mode
        showNavigationUI()
        
        // Keep location puck active during navigation (don't disable it)
        // Location puck remains visible for better navigation experience
        
        // Re-render the map with reduced beige polyline during navigation
        reRenderMapForNavigation()
        
        // Start location updates for navigation
        startNavigationLocationUpdates()
        
        // Navigation display removed - only button state is updated
        
        Log.d(TAG, "Navigation mode started successfully")
        Log.d(TAG, "=== startNavigationMode END ===")
    }
    
    private fun stopNavigationMode() {
        Log.d(TAG, "=== stopNavigationMode START ===")
        
        isNavigationActive = false
        navigationRoute = null
        navigationInstructions = emptyList()
        currentNavigationStep = 0
        hasAnnouncedArrival = false
        announcedWaypoints.clear()
        
        // Stop location updates
        stopNavigationLocationUpdates()
        
        // Stop TTS
        try { 
            navTts?.stop()
            navTts?.shutdown() 
        } catch (_: Exception) { }
        navTts = null
        
        // Cancel navigation notifications
        try {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(3001)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(3002)
        } catch (_: Exception) { }
        
        // Stop alarm service if running
        try {
            stopService(Intent(this, com.example.gzingapp.AlarmSoundService::class.java))
        } catch (_: Exception) { }
        
        // Hide navigation UI and show route details
        hideNavigationUI()
        
        // Location puck remains active (no need to re-enable since it was never disabled)
        
        // Reset waypoint progression
        currentWaypointIndex = 0
        
        Log.d(TAG, "Navigation mode stopped")
        Log.d(TAG, "=== stopNavigationMode END ===")
    }
    
    private fun reRenderMapForNavigation() {
        Log.d(TAG, "=== reRenderMapForNavigation START ===")
        
        // Get the current route details
        val route = selectedRoute
        if (route == null) {
            Log.e(TAG, "No route details available for navigation re-render")
            return
        }
        
        // Re-render the map with navigation-specific polylines
        mapView.getMapboxMap().getStyle { style ->
            Log.d(TAG, "Re-rendering map for navigation mode")
            
            // Clear existing polylines but keep pins
            clearNavigationPolylines(style)
            
            // Add reduced beige polyline (closest waypoint to destination)
            addReducedBeigePolyline(route!!, style)
            
            // Blue polyline (current location to closest waypoint) is already handled by addDirectRoutePolyline
            // which is called in updateMapLayers
            
            Log.d(TAG, "Map re-rendered for navigation mode")
        }
        
        Log.d(TAG, "=== reRenderMapForNavigation END ===")
    }
    
    private fun clearNavigationPolylines(style: Style) {
        Log.d(TAG, "=== clearNavigationPolylines START ===")
        
        try {
            // Clear full API response polyline (we'll replace it with reduced version)
            if (style.styleSourceExists("api-route-line")) {
                style.removeStyleSource("api-route-line")
                Log.d(TAG, "Cleared full api-route-line source")
            }
            if (style.styleLayerExists("api-route-line-layer")) {
                style.removeStyleLayer("api-route-line-layer")
                Log.d(TAG, "Cleared full api-route-line-layer")
            }
            
            // Clear any existing reduced beige polyline
            if (style.styleSourceExists("reduced-beige-route-line")) {
                style.removeStyleSource("reduced-beige-route-line")
                Log.d(TAG, "Cleared existing reduced-beige-route-line source")
            }
            if (style.styleLayerExists("reduced-beige-route-line-layer")) {
                style.removeStyleLayer("reduced-beige-route-line-layer")
                Log.d(TAG, "Cleared existing reduced-beige-route-line-layer")
            }
            
            Log.d(TAG, "Navigation polylines cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing navigation polylines", e)
        }
        
        Log.d(TAG, "=== clearNavigationPolylines END ===")
    }
    
    private fun stopNavigationFromAlarm() {
        Log.d(TAG, "=== stopNavigationFromAlarm START ===")
        
        // Stop navigation mode
        stopNavigationMode()
        
        // Show save route dialog after a short delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            showSaveRouteDialog()
        }, 3000) // 3 second delay
        
        Log.d(TAG, "=== stopNavigationFromAlarm END ===")
    }
    
    private fun showSaveRouteDialog() {
        // Check if activity is still valid
        if (isFinishing || isDestroyed) {
            Log.d(TAG, "Activity is finishing or destroyed, skipping dialog")
            return
        }
        
        val currentLoc = lastKnownLocation
        val destLoc = destinationPin
        
        if (currentLoc == null || destLoc == null) {
            Toast.makeText(this, "Cannot save route: Location data not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_save_route, null)
        val etRouteName = dialogView.findViewById<EditText>(R.id.etRouteName)
        val etRouteDescription = dialogView.findViewById<EditText>(R.id.etRouteDescription)
        val cbIsFavorite = dialogView.findViewById<CheckBox>(R.id.cbIsFavorite)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        
        val destinationName = destName.ifBlank { destLoc.name }
        etRouteName.setText("Route to $destinationName")
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("Save Navigation Route")
            .setMessage("Would you like to save this route for future use?")
            .setView(dialogView)
            .setPositiveButton("Save Route") { _, _ ->
                saveNavigationRoute(
                    routeName = etRouteName.text.toString().trim(),
                    routeDescription = etRouteDescription.text.toString().trim(),
                    isFavorite = cbIsFavorite.isChecked,
                    userRating = ratingBar.rating.toInt()
                )
            }
            .setNegativeButton("Don't Save") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun saveNavigationRoute(
        routeName: String,
        routeDescription: String,
        isFavorite: Boolean,
        userRating: Int
    ) {
        val currentLoc = lastKnownLocation
        val destLoc = destinationPin
        
        if (currentLoc == null || destLoc == null) {
            Toast.makeText(this, "Cannot save route: Location data not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (routeName.isBlank()) {
            Toast.makeText(this, "Please enter a route name", Toast.LENGTH_SHORT).show()
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val distance = computeHaversineMeters(
                    currentLoc.latitude, currentLoc.longitude,
                    destLoc.lat, destLoc.lng
                ) / 1000.0 // Convert to kilometers
                
                val estimatedDuration = (distance / 30.0 * 60.0).toInt() // 30 km/h average speed
                val averageSpeed = if (estimatedDuration > 0) distance / (estimatedDuration / 60.0) else 0.0
                val estimatedFare = calculateTotalFare(distance)
                
                val routeCoordinates = listOf(
                    mapOf("lat" to currentLoc.latitude, "lng" to currentLoc.longitude),
                    mapOf("lat" to destLoc.lat, "lng" to destLoc.lng)
                )
                
                val request = CreateNavigationRouteRequest(
                    userId = com.example.gzingapp.utils.AppSettings(this@RoutesMapsActivity).getUserId() ?: 0,
                    routeName = routeName,
                    routeDescription = routeDescription.ifBlank { null },
                    startLatitude = currentLoc.latitude,
                    startLongitude = currentLoc.longitude,
                    endLatitude = destLoc.lat,
                    endLongitude = destLoc.lng,
                    destinationName = destName.ifBlank { destLoc.name },
                    destinationAddress = destLoc.address,
                    routeDistance = distance,
                    estimatedDuration = estimatedDuration,
                    estimatedFare = estimatedFare,
                    transportMode = "car", // Default to car for route navigation
                    routeQuality = when (userRating) {
                        5 -> "excellent"
                        4 -> "good"
                        3 -> "fair"
                        else -> "poor"
                    },
                    trafficCondition = "Light", // Default traffic condition
                    averageSpeed = averageSpeed,
                    waypointsCount = waypointSequence.size,
                    routeCoordinates = routeCoordinates,
                    isFavorite = if (isFavorite) 1 else 0,
                    isPublic = 0 // Default to private
                )
                
                Log.d("SaveRoute", "Saving navigation route: $routeName")
                
                val result = navigationRouteRepository.createNavigationRoute(request)
                
                result.onSuccess { response ->
                    Log.d("SaveRoute", "Route saved successfully: ${response.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RoutesMapsActivity,
                            "Route '$routeName' saved successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }.onFailure { error ->
                    Log.e("SaveRoute", "Failed to save route: ${error.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RoutesMapsActivity,
                            "Failed to save route: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                
            } catch (e: Exception) {
                Log.e("SaveRoute", "Error saving route", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RoutesMapsActivity,
                        "Error saving route: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun generateNavigationRoute() {
        Log.d(TAG, "=== generateNavigationRoute START ===")
        
        val pins = selectedRoute?.mapDetails?.pins ?: return
        val sortedPins = pins.sortedBy { it.number }
        
        // Create waypoints from current location to destination
        val waypoints = mutableListOf<Point>()
        
        // Add current location if available
        if (currentLat != 0.0 && currentLng != 0.0) {
            waypoints.add(Point.fromLngLat(currentLng, currentLat))
        }
        
        // Add waypoints from closest to destination
        val closest = closestWaypoint ?: return
        val destPin = pins.find { pin -> 
            Math.abs(pin.lat - endLat) < 0.0001 && Math.abs(pin.lng - endLng) < 0.0001 
        } ?: return
        
        val waypointSequence = createWaypointSequenceFromClosestToDestination(pins, closest, destPin)
        waypoints.addAll(waypointSequence.map { Point.fromLngLat(it.lng, it.lat) })
        
        navigationRoute = waypoints
        
        // Generate navigation instructions
        generateNavigationInstructions(waypoints)
        
        // Calculate total distance and duration
        calculateNavigationMetrics(waypoints)
        
        Log.d(TAG, "Generated navigation route with ${waypoints.size} waypoints")
        Log.d(TAG, "Generated ${navigationInstructions.size} navigation instructions")
        Log.d(TAG, "=== generateNavigationRoute END ===")
    }
    
    private fun generateNavigationInstructions(waypoints: List<Point>) {
        Log.d(TAG, "=== generateNavigationInstructions START ===")
        
        val instructions = mutableListOf<String>()
        
        if (waypoints.size < 2) {
            instructions.add("Route to destination")
            navigationInstructions = instructions
            return
        }
        
        // Generate instructions for each segment
        for (i in 0 until waypoints.size - 1) {
            val current = waypoints[i]
            val next = waypoints[i + 1]
            
            val distance = computeHaversineMeters(
                current.latitude(), current.longitude(),
                next.latitude(), next.longitude()
            )
            
            val distanceKm = distance / 1000.0
            val distanceText = if (distanceKm >= 1.0) {
                "${String.format("%.1f", distanceKm)} km"
            } else {
                "${distance.toInt()} m"
            }
            
            // Determine direction (simplified)
            val instruction = when {
                i == 0 && currentLat != 0.0 && currentLng != 0.0 -> {
                    "Start navigation to ${destName.ifBlank { "destination" }}"
                }
                i == waypoints.size - 2 -> {
                    "Arrive at ${destName.ifBlank { "destination" }}"
                }
                else -> {
                    "Continue for $distanceText"
                }
            }
            
            instructions.add(instruction)
        }
        
        navigationInstructions = instructions
        Log.d(TAG, "Generated instructions: ${instructions.joinToString(" -> ")}")
        Log.d(TAG, "=== generateNavigationInstructions END ===")
    }
    
    private fun calculateNavigationMetrics(waypoints: List<Point>) {
        Log.d(TAG, "=== calculateNavigationMetrics START ===")
        
        var totalDistance = 0.0
        
        for (i in 0 until waypoints.size - 1) {
            val distance = computeHaversineMeters(
                waypoints[i].latitude(), waypoints[i].longitude(),
                waypoints[i + 1].latitude(), waypoints[i + 1].longitude()
            )
            totalDistance += distance
        }
        
        navigationDistance = totalDistance / 1000.0 // Convert to km
        navigationDuration = (navigationDistance / 30.0 * 60.0).toInt() // 30 km/h average speed
        
        Log.d(TAG, "Navigation distance: ${String.format("%.2f", navigationDistance)} km")
        Log.d(TAG, "Navigation duration: $navigationDuration minutes")
        Log.d(TAG, "=== calculateNavigationMetrics END ===")
    }
    
    private fun showNavigationUI() {
        Log.d(TAG, "Showing navigation UI")
        
        // Hide start button and show stop button
        btnStartNavigationRoutes.visibility = android.view.View.GONE
        btnStopNavigationRoutes.visibility = android.view.View.VISIBLE
        
        // Update routes info card to show navigation state
        updateRoutesInfoCardForNavigation()
    }
    
    private fun hideNavigationUI() {
        Log.d(TAG, "Hiding navigation UI")
        
        // Show start button and hide stop button
        btnStartNavigationRoutes.visibility = android.view.View.VISIBLE
        btnStopNavigationRoutes.visibility = android.view.View.GONE
        
        // Reset routes info card to normal state
        updateRoutesInfoCardForNormal()
    }
    
    private fun updateRoutesInfoCardForNavigation() {
        try {
            // Update the card to show navigation is active
            tvCurrentToWaypoint.text = "🚗 Navigation Active - ${tvCurrentToWaypoint.text}"
            
            // Add navigation status indicator
            tvTrafficStatus.text = "Navigation: Active"
            try {
                val color = android.graphics.Color.parseColor("#4CAF50") // Green for active navigation
                (trafficDot.background as? android.graphics.drawable.GradientDrawable)?.setColor(color)
            } catch (_: Exception) { }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating routes info card for navigation", e)
        }
    }
    
    private fun updateRoutesInfoCardForNormal() {
        try {
            // Reset the card to normal state
            updateCurrentToWaypointText()
            
            // Traffic status will be updated by traffic analysis - no manual toggle
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating routes info card for normal state", e)
        }
    }
    
    
    
    private fun startNavigationLocationUpdates() {
        Log.d(TAG, "Starting navigation location updates")
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted for navigation updates")
            return
        }
        
        // Create location request
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateDistanceMeters(MIN_DISTANCE_CHANGE)
        }.build()
        
        // Start location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            navigationLocationCallback,
            android.os.Looper.getMainLooper()
        )
        
        Log.d(TAG, "Navigation location updates started")
    }
    
    private fun stopNavigationLocationUpdates() {
        Log.d(TAG, "Stopping navigation location updates")
        fusedLocationClient.removeLocationUpdates(navigationLocationCallback)
    }
    
    private val navigationLocationCallback = object : com.google.android.gms.location.LocationCallback() {
        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
            if (!isNavigationActive) return
            
            val location = locationResult.lastLocation ?: return
            lastKnownLocation = location
            
            Log.d(TAG, "Navigation location update: ${location.latitude}, ${location.longitude}")
            
            // Note: Using Mapbox built-in location puck, no custom location marker update needed
            
            // Check if user has reached any waypoint in sequence (within 100m)
            checkSequentialWaypointProximity(location)
            
            // Check waypoint progression for route display
            checkWaypointProgression(location)
        }
    }
    
    private fun checkWaypointProgression(location: android.location.Location) {
        // This method is now handled by checkSequentialWaypointProximity
        // which properly handles waypoint announcements and progression
        checkSequentialWaypointProximity(location)
    }
    
    // Note: Using Mapbox built-in location puck, no custom navigation location marker needed
    
    private fun checkWaypointProximity(location: android.location.Location) {
        if (navigationRoute == null || currentNavigationStep >= navigationRoute!!.size) return
        
        val currentWaypoint = navigationRoute!![currentNavigationStep]
        val distance = computeHaversineMeters(
            location.latitude, location.longitude,
            currentWaypoint.latitude(), currentWaypoint.longitude()
        )
        
        // Check if we're within 100 meters of current waypoint
        if (distance < 100.0) {
            Log.d(TAG, "Reached waypoint ${currentNavigationStep + 1}, distance: ${String.format("%.1f", distance)}m")
            
            // Check if this is the destination (last waypoint)
            val isDestination = currentNavigationStep == navigationRoute!!.size - 1
            
            if (isDestination) {
                // Handle destination arrival
                handleDestinationArrival()
            } else {
                // Handle waypoint arrival
                handleWaypointArrival(currentNavigationStep)
            }
            
            // Navigation instruction methods removed - only logging waypoint progression
        }
    }
    
    private fun checkSequentialWaypointProximity(location: android.location.Location) {
        // Get the waypoint sequence from the route
        val pins = selectedRoute?.mapDetails?.pins ?: return
        val destPin = pins.find { pin -> 
            Math.abs(pin.lat - endLat) < 0.0001 && Math.abs(pin.lng - endLng) < 0.0001 
        } ?: return
        
        val waypointSequence = createWaypointSequenceFromClosestToDestination(pins, closestWaypoint, destPin)
        
        // Check proximity to each waypoint in sequence
        waypointSequence.forEachIndexed { index, waypoint ->
            val distance = computeHaversineMeters(
                location.latitude, location.longitude,
                waypoint.lat, waypoint.lng
            )
            
            // If within 100m of a waypoint and haven't announced it yet
            if (distance < 100.0 && !announcedWaypoints.contains(index)) {
                Log.d(TAG, "Within 100m of waypoint ${index + 1}: ${waypoint.name}, distance: ${String.format("%.1f", distance)}m")
                
                // Update current waypoint index for UI progression
                if (index >= currentWaypointIndex) {
                    currentWaypointIndex = index + 1
                    updateCurrentToWaypointText()
                }
                
                // Check if this is the destination (last waypoint in sequence)
                val isDestination = index == waypointSequence.size - 1
                
                if (isDestination) {
                    // Handle destination arrival
                    handleDestinationArrival()
                } else {
                    // Handle waypoint arrival
                    handleWaypointArrival(index)
                }
            }
        }
    }
    
    private fun handleWaypointArrival(waypointIndex: Int) {
        if (announcedWaypoints.contains(waypointIndex)) {
            Log.d(TAG, "Waypoint $waypointIndex already announced, skipping")
            return
        }
        
        announcedWaypoints.add(waypointIndex)
        
        // Get the actual waypoint from the sequence (not from original pins array)
        val pins = selectedRoute?.mapDetails?.pins ?: return
        val destPin = pins.find { pin -> 
            Math.abs(pin.lat - endLat) < 0.0001 && Math.abs(pin.lng - endLng) < 0.0001 
        } ?: return
        
        val waypointSequence = createWaypointSequenceFromClosestToDestination(pins, closestWaypoint, destPin)
        val actualWaypoint = if (waypointIndex < waypointSequence.size) {
            waypointSequence[waypointIndex]
        } else {
            return
        }
        
        val waypointName = actualWaypoint.name
        val waypointNumber = actualWaypoint.number // Use the actual waypoint number, not sequence index
        
        Log.d(TAG, "Handling waypoint arrival: $waypointName (Waypoint $waypointNumber)")
        
        // Send waypoint arrival notification
        sendWaypointArrivalNotification(waypointName, waypointNumber)
        
        // Enhanced voice announcement for waypoint stops
        announceWaypointStop(waypointName, waypointNumber)
        
        // Show toast
        Toast.makeText(this, "Arriving at $waypointName", Toast.LENGTH_SHORT).show()
        
        // AUTO ADVANCE: Update navigation step to next waypoint
        updateNavigationStepToNext(waypointIndex)
    }
    
    private fun announceWaypointStop(waypointName: String, waypointNumber: Int) {
        try {
            val settings = com.example.gzingapp.utils.AppSettings(this)
            if (settings.isVoiceAnnouncementsEnabled()) {
                try { navTts?.stop(); navTts?.shutdown() } catch (_: Exception) { }
                navTts = android.speech.tts.TextToSpeech(this) { status ->
                    if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                        try {
                            navTts?.language = java.util.Locale.getDefault()
                            
                            // Check if this is the first waypoint in the sequence (closest waypoint)
                            val pins = selectedRoute?.mapDetails?.pins ?: return@TextToSpeech
                            val destPin = pins.find { pin -> 
                                Math.abs(pin.lat - endLat) < 0.0001 && Math.abs(pin.lng - endLng) < 0.0001 
                            } ?: return@TextToSpeech
                            
                            val waypointSequence = createWaypointSequenceFromClosestToDestination(pins, closestWaypoint, destPin)
                            val isFirstStop = waypointSequence.isNotEmpty() && waypointSequence.first().number == waypointNumber
                            
                            // Enhanced announcement for waypoint stops
                            val announcement = if (isFirstStop) {
                                "First stop: $waypointName. Please prepare to alight."
                            } else {
                                "Stop $waypointNumber: $waypointName. Please prepare to alight."
                            }
                            
                            navTts?.speak(announcement, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "waypoint_stop_$waypointNumber")
                            
                            Log.d(TAG, "Announced waypoint stop: $announcement")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in waypoint announcement", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up waypoint announcement", e)
        }
    }
    
    private fun updateNavigationStepToNext(currentWaypointIndex: Int) {
        val pins = selectedRoute?.mapDetails?.pins ?: return
        val destPin = pins.find { pin -> 
            Math.abs(pin.lat - endLat) < 0.0001 && Math.abs(pin.lng - endLng) < 0.0001 
        } ?: return
        
        val waypointSequence = createWaypointSequenceFromClosestToDestination(pins, closestWaypoint, destPin)
        val nextWaypointIndex = currentWaypointIndex + 1
        
        if (nextWaypointIndex < waypointSequence.size) {
            // Update current navigation step
            currentNavigationStep = nextWaypointIndex
            
            // Update navigation UI with next waypoint info
            val nextWaypoint = waypointSequence[nextWaypointIndex]
            val isDestination = nextWaypointIndex == waypointSequence.size - 1
            
            runOnUiThread {
                if (isDestination) {
                    Log.d(TAG, "🎯 Auto-advanced to destination: ${nextWaypoint.name}")
                } else {
                    Log.d(TAG, "🎯 Auto-advanced to waypoint ${nextWaypointIndex + 1}: ${nextWaypoint.name}")
                }
            }
        } else {
            Log.d(TAG, "🎯 Reached final waypoint, no more auto-advance")
        }
    }
    
    private fun handleDestinationArrival() {
        if (hasAnnouncedArrival) {
            Log.d(TAG, "Destination already announced, skipping")
            return
        }
        
        hasAnnouncedArrival = true
        
        Log.d(TAG, "Handling destination arrival: $destName")
        
        // Send destination arrival notification with alarm
        sendDestinationArrivalNotification()
        
        // Start alarm service
        try {
            val svc = Intent(this, com.example.gzingapp.AlarmSoundService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(this, svc)
        } catch (_: Exception) { }
        
        // Enhanced destination announcement
        announceDestinationArrival()
        
        // Show toast
        Toast.makeText(this, "Arrived at your destination: $destName", Toast.LENGTH_LONG).show()
        
        // Show dialog with option to stop navigation
        showDestinationArrivalDialog()
    }
    
    private fun announceDestinationArrival() {
        try {
            val settings = com.example.gzingapp.utils.AppSettings(this)
            if (settings.isVoiceAnnouncementsEnabled()) {
                try { navTts?.stop(); navTts?.shutdown() } catch (_: Exception) { }
                navTts = android.speech.tts.TextToSpeech(this) { status ->
                    if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                        try {
                            navTts?.language = java.util.Locale.getDefault()
                            val announcement = "Final destination: $destName. You have arrived. Please alight now."
                            navTts?.speak(announcement, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "destination_voice")
                            Log.d(TAG, "Announced destination arrival: $announcement")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in destination announcement", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up destination announcement", e)
        }
    }
    
    private fun showDestinationArrivalDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Destination Reached")
            .setMessage("You have arrived at your destination: $destName\n\nStop navigation?")
            .setPositiveButton("Stop Navigation") { dialog, _ ->
                stopNavigationFromAlarm()
                dialog.dismiss()
            }
            .setNegativeButton("Continue") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun ensureNavigationNotificationChannel() {
        val channelId = "gzing_route_nav_channel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = nm.getNotificationChannel(channelId)
            if (existing == null) {
                val ch = NotificationChannel(channelId, "Route Navigation", NotificationManager.IMPORTANCE_HIGH)
                ch.description = "Route navigation progress and waypoint alerts"
                nm.createNotificationChannel(ch)
            }
        }
    }
    
    private fun sendWaypointArrivalNotification(waypointName: String, waypointNumber: Int) {
        ensureNavigationNotificationChannel()
        
        val channelId = "gzing_route_nav_channel"
        val intent = Intent(this, RoutesMapsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pi = PendingIntent.getActivity(this, 3000 + waypointNumber, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        val text = "Arriving at $waypointName"
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_navigation)
            .setContentTitle("Waypoint $waypointNumber")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(3000 + waypointNumber, notif)
        Log.d(TAG, "Waypoint arrival notification sent for: $waypointName")
    }
    
    private fun sendDestinationArrivalNotification() {
        ensureNavigationNotificationChannel()
        
        val channelId = "gzing_route_nav_channel"
        val intent = Intent(this, RoutesMapsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pi = PendingIntent.getActivity(this, 3001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        // Stop action via broadcast (if you have a StopNavigationReceiver)
        val stopIntent = Intent(this, com.example.gzingapp.StopNavigationReceiver::class.java).apply {
            action = com.example.gzingapp.StopNavigationReceiver.ACTION_STOP_NAV
        }
        val stopPi = PendingIntent.getBroadcast(this, 3002, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        val text = "You have arrived at your destination: $destName"
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_navigation)
            .setContentTitle("Destination Reached!")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)
            .addAction(R.drawable.ic_close, "Stop Navigation", stopPi)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(3001, notif)
        Log.d(TAG, "Destination arrival notification sent for: $destName")
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
    
    private fun showLoading(show: Boolean) {
        loadingIndicator.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, message)
    }
    
    override fun onStart() {
        super.onStart()
        mapView.onStart()
        mapStyleLoaded = false // Reset style loaded flag
    }
    
    override fun onStop() {
        super.onStop()
        mapView.onStop()
        
        // Stop navigation location updates when activity stops
        if (isNavigationActive) {
            stopNavigationLocationUpdates()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        currentTooltip?.let { tooltip ->
            mapView.removeView(tooltip)
        }
        
        // Clean up navigation state
        if (isNavigationActive) {
            stopNavigationLocationUpdates()
        }
        
        // Stop TTS
        try { 
            navTts?.stop()
            navTts?.shutdown() 
        } catch (_: Exception) { }
        
        // Unregister broadcast receiver
        try {
            unregisterReceiver(alarmStopReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering alarm stop receiver", e)
        }
        
        mapView.onDestroy()
    }
    
    override fun onPause() {
        super.onPause()
        currentTooltip?.let { tooltip ->
            mapView.removeView(tooltip)
            currentTooltip = null
        }
        
        // Pause navigation location updates when activity pauses
        if (isNavigationActive) {
            stopNavigationLocationUpdates()
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Resume navigation location updates if navigation is active
        if (isNavigationActive) {
            startNavigationLocationUpdates()
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("stop_navigation", false)) {
            stopNavigationMode()
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
    
    // SOS Help Dialog
    private fun showSosHelpDialog() {
        // Get current user ID from AppSettings
        val appSettings = com.example.gzingapp.utils.AppSettings(this)
        val userId = appSettings.getUserId()
        
        Log.d("RoutesMapsActivity", "Showing SOS dialog for user ID: $userId")
        
        val sosDialog = com.example.gzingapp.ui.SosHelpDialog(
            context = this,
            onSosSent = {
                // Handle successful SOS sent
                Toast.makeText(this, "Emergency SMS sent to all contacts!", Toast.LENGTH_LONG).show()
            },
            onDismiss = {
                // Handle dialog dismissal
                Log.d("SosDialog", "SOS dialog dismissed")
            }
        )
        sosDialog.show()
    }
    
    // Traffic is now always enabled
    private fun getCurrentStyleUri(): String {
        return Style.TRAFFIC_DAY // Always use traffic style
    }

    private fun applyCurrentStyleAndRedraw() {
        if (!::mapView.isInitialized) return
        
        // Traffic is always enabled, just update layers
        mapView.getMapboxMap().getStyle { style ->
        if (selectedRoute != null) {
                updateMapLayers(style, selectedRoute!!)
            }
        }
    }
    
    private fun updateNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        val navHeaderEmail = headerView.findViewById<TextView>(R.id.navHeaderEmail)
        val navHeaderName = headerView.findViewById<TextView>(R.id.navHeaderName)
        
        // Get user info from AppSettings
        val appSettings = com.example.gzingapp.utils.AppSettings(this)
        val userEmail = appSettings.getUserEmail()
        val userId = appSettings.getUserId()
        
        navHeaderEmail.text = userEmail ?: "user@example.com"
        navHeaderName.text = "User #${userId ?: "Unknown"}"
    }
    
    private fun logout() {
        // Clear user session
        val appSettings = com.example.gzingapp.utils.AppSettings(this)
        appSettings.clearUserData()
        
        // Navigate to AuthActivity
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}