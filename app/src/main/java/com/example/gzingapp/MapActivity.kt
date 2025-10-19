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
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
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
import android.os.Build
import android.app.AlertDialog
import android.widget.EditText
import android.widget.CheckBox
import android.widget.RatingBar
import android.telephony.SmsManager

class MapActivity : AppCompatActivity() {

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
    private lateinit var tvFailureRate: TextView
    private lateinit var failureRateDot: View
    private lateinit var rowFailureRate: LinearLayout
    private lateinit var btnStartNavigation: MaterialButton
    private lateinit var fabMyLocation: FloatingActionButton
    private lateinit var fabAddLocation: FloatingActionButton
    private lateinit var fabTraffic: FloatingActionButton
    private lateinit var fabAlternateRoutes: FloatingActionButton
    private lateinit var fabSosHelp: com.example.gzingapp.ui.SosHoldButton
    private lateinit var btnCollapse: ImageView
    private lateinit var cardLocationInfo: androidx.cardview.widget.CardView
    private lateinit var rowCurrent: LinearLayout
    private lateinit var rowPinned: LinearLayout
    private lateinit var rowStats: LinearLayout
    private lateinit var bottomNavBar: LinearLayout
    
    // Bottom Navigation
    private lateinit var tabDashboard: LinearLayout
    private lateinit var tabRoutes: LinearLayout
    private lateinit var tabPlaces: LinearLayout
    private lateinit var tabLandmarks: LinearLayout
    
    // Current location
    private var currentLocation: Point? = null
    
    // Pinned location
    private var pinnedLocation: Point? = null
    private var isPinningMode = false
    
    // Route data
    private var routeCoordinates: List<Point>? = null
    
    // Map state
    private var isMapStyleLoaded = false
    private var selectedTransportMode: String = "Car"
    private var trafficEnabled: Boolean = false
    private var currentStyleIndex: Int = 0 // 0: Streets, 1: Satellite
    private var alternativeRoutes: List<LineString> = emptyList()
    private var alternativeDurationsSec: List<Double> = emptyList()
    private var currentRouteIndex: Int = 0
    private var isNavigating: Boolean = false
    private var hasAnnouncedArrival: Boolean = false
    private var navigationStartTime: Long = 0
    private var navTts: android.speech.tts.TextToSpeech? = null
    private var lastTrafficState: Boolean = false
    private var offRouteLine: List<Point>? = null
    
    // Failure Rate Monitoring
    private var failureRate: Int = 0
    private var lastLocationOnRoute: Boolean = true
    private var consecutiveOffRouteCount: Int = 0
    private var emergencySmsSent: Boolean = false
    private val DEVIATION_THRESHOLD_METERS = 50.0
    private val FAILURE_RATE_INCREMENT = 10
    private val ALTERNATIVE_ROUTE_PENALTY = 40
    
    // Off-Route SOS Warning System
    private var offRouteWarningTriggered: Boolean = false
    private var offRouteStartTime: Long = 0
    private val OFF_ROUTE_CONFIRMATION_THRESHOLD = 5 // seconds before triggering SOS
    // On-route debounce to avoid dialog flash when GPS briefly returns to route
    private val ON_ROUTE_DEBOUNCE_SECONDS = 3
    private var onRouteSinceTime: Long = 0L
    private var sosWarningDialog: com.example.gzingapp.ui.SosWarningDialog? = null
    
    // New State Management Variables
    private var offRoute: Boolean = false
    private var isSafe: Boolean = false
    private var cooldownEndTime: Long = 0L
    private val COOLDOWN_DURATION_SECONDS = 10 // 10 seconds cooldown
    
    // Distance Increase SOS Warning System
    private var initialDistanceToDestination: Double = 0.0
    private val DISTANCE_INCREASE_THRESHOLD_METERS = 500.0
    private var distanceIncreaseStartTime: Long = 0
    private val DISTANCE_INCREASE_CONFIRMATION_SECONDS = 10
    
    // SOS Spam Prevention
    private var lastSosSentTime: Long = 0
    private val SOS_COOLDOWN_MINUTES = 5 // 5 minutes between SOS triggers
    
    // One-time SOS Warning per Navigation Session
    private var sosWarningShownThisSession: Boolean = false
    
    // Navigation History Creation
    private lateinit var navigationHistoryRepository: com.example.gzingapp.repository.NavigationHistoryRepository
    private lateinit var routesRepository: com.example.gzingapp.repository.RoutesRepository
    private lateinit var navigationRouteRepository: com.example.gzingapp.repository.NavigationRouteRepository
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted
                getCurrentLocation()
            }
            else -> {
                // No location access granted
                Toast.makeText(this, "Location permission is required to show your current location", Toast.LENGTH_LONG).show()
                showDefaultLocation()
            }
        }
    }
    
    private val smsPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MapActivity", "SMS permission granted")
        } else {
            Log.w("MapActivity", "SMS permission denied - emergency alerts may not work")
            Toast.makeText(this, "SMS permission required for emergency alerts", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        
        // Initialize UI components
        initializeViews()
        
        // Handle selected place from PlacesActivity
        handleSelectedPlace()
        
        // Initialize MapView
        mapView = findViewById(R.id.mapView)
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        
        // Initialize navigation history repository
        navigationHistoryRepository = com.example.gzingapp.repository.NavigationHistoryRepository(this)
        routesRepository = com.example.gzingapp.repository.RoutesRepository(this)
        navigationRouteRepository = com.example.gzingapp.repository.NavigationRouteRepository(this)
        
        // Initialize location callback for real-time updates
        setupLocationCallback()
        
        // Setup map
        setupMap()
        
        // Setup navigation drawer
        setupNavigationDrawer()
        
        // Setup click listeners
        setupClickListeners()
        
        // Request location permission and get current location
        requestLocationPermission()
        // Request SMS permission for emergency alerts
        requestSmsPermission()
        // Default collapsed state: only show current & start button
        setCardCollapsed(true)
    }
    
    private fun initializeViews() {
        // Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        
        // tvLocationAddress is optional (legacy hidden field)
        tvLocationAddress = try { findViewById(R.id.tvLocationAddress) } catch (_: Exception) { null }
        tvCurrentLocation = findViewById(R.id.tvCurrentLocation)
        tvPinnedLocation = findViewById(R.id.tvPinnedLocation)
        tvDistance = findViewById(R.id.tvDistance)
        tvEta = findViewById(R.id.tvEta)
        tvFare = findViewById(R.id.tvFare)
        tvTrafficStatus = findViewById(R.id.tvTrafficStatus)
        trafficDot = findViewById(R.id.trafficDot)
        tvFailureRate = findViewById(R.id.tvFailureRate)
        failureRateDot = findViewById(R.id.failureRateDot)
        rowFailureRate = findViewById(R.id.rowFailureRate)
        btnStartNavigation = findViewById(R.id.btnStartNavigation)
        fabMyLocation = findViewById(R.id.fabMyLocation)
        fabAddLocation = findViewById(R.id.fabAddLocation)
        fabTraffic = findViewById(R.id.fabTraffic)
        fabAlternateRoutes = findViewById(R.id.fabAlternateRoutes)
        
        // Initialize SOS Hold Button
        fabSosHelp = findViewById<com.example.gzingapp.ui.SosHoldButton>(R.id.fabSosHelp)
        
        btnCollapse = findViewById(R.id.btnCollapse)
        cardLocationInfo = findViewById(R.id.cardLocationInfo)
        // Hide add-pin FAB; we'll pin on first tap automatically
        fabAddLocation.visibility = View.GONE
        rowCurrent = findViewById(R.id.rowCurrent)
        rowPinned = findViewById(R.id.rowPinned)
        rowStats = findViewById(R.id.rowStats)
        
        // Bottom Navigation
        tabDashboard = findViewById(R.id.tabDashboard)
        tabRoutes = findViewById(R.id.tabRoutes)
        tabPlaces = findViewById(R.id.tabPlaces)
        tabLandmarks = findViewById(R.id.tabLandmarks)
        bottomNavBar = findViewById(R.id.bottomNavBar)
    }
    
    private fun setupClickListeners() {
        // My Location FAB
        fabMyLocation.setOnClickListener {
            getCurrentLocation()
            // Center camera on current location if available
            currentLocation?.let { point ->
                mapView.mapboxMap.setCamera(
                    com.mapbox.maps.CameraOptions.Builder()
                        .center(point)
                        .zoom(15.0) // Good zoom level for city navigation
                        .build()
                )
            } ?: run {
                // If no current location, show a message
                Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Add Location FAB - Toggle pinning mode
        fabAddLocation.setOnClickListener {

            togglePinningMode()
        }
        
        // Start Navigation Button
        btnStartNavigation.setOnClickListener { toggleNavigationMode(true) }
        
        // Update navigation button state
        updateNavigationButtonState()
        
        // Transport mode is selected via dialog on pin
        
        // Collapse/Expand Location Info
        btnCollapse.setOnClickListener {
            toggleLocationCard()
        }

        // Toggle traffic (re-fetch routes with traffic profile and restyle)
        fabTraffic.setOnClickListener {
            trafficEnabled = !trafficEnabled
            tvTrafficStatus.text = if (trafficEnabled) "On" else "Off"
            try {
                val color = if (trafficEnabled) android.graphics.Color.parseColor("#E74C3C") else android.graphics.Color.parseColor("#BDC3C7")
                (trafficDot.background as? android.graphics.drawable.GradientDrawable)?.setColor(color)
            } catch (_: Exception) { }
            // Apply current style (traffic/non-traffic) and redraw overlays
            applyCurrentStyleAndRedraw()
            if (currentLocation != null && pinnedLocation != null) {
                updateRoute(currentLocation!!, pinnedLocation!!)
            }
        }

        // Map style FAB removed

        // Cycle alternative routes if available
        fabAlternateRoutes.setOnClickListener {
            if (alternativeRoutes.isNotEmpty()) {
                currentRouteIndex = (currentRouteIndex + 1) % alternativeRoutes.size
                val line = alternativeRoutes[currentRouteIndex]
                routeCoordinates = line.coordinates()
                
                // Increase failure rate for using alternative routes
                if (isNavigating) {
                    increaseFailureRate(ALTERNATIVE_ROUTE_PENALTY, "Alternative route selected")
                }
                
                // Update route by redrawing layers
                redrawAllLayers()
                
                val distanceMetersAlt = estimateLineStringDistanceMeters(line)
                tvDistance.text = "Distance: ${formatDistance(distanceMetersAlt)}"
                tvEta.text = "ETA: ${estimateEtaMinutes(distanceMetersAlt, selectedTransportMode).toInt()} min"
                tvFare.text = formatFare(distanceMetersAlt)
            } else {
                Toast.makeText(this, "No alternative routes available", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Bottom Navigation
        tabDashboard.setOnClickListener {
            // Map is already the dashboard, just show a toast
            Toast.makeText(this, "You are on the Map Dashboard", Toast.LENGTH_SHORT).show()
        }
        
        tabRoutes.setOnClickListener {
            val intent = Intent(this, RoutesActivity::class.java)
            startActivity(intent)
        }
        
        tabPlaces.setOnClickListener {
            val intent = Intent(this, PlacesActivity::class.java)
            startActivity(intent)
        }
        
        tabLandmarks.setOnClickListener {
            val intent = Intent(this, LandmarksActivity::class.java)
            startActivity(intent)
        }
        
        // SOS Help Hold Button - Set up 3-second hold listener
        fabSosHelp.setOnSosActivatedListener {
            showSosHelpDialog()
        }
    }

    private var isCardCollapsed: Boolean = true
    private fun setCardCollapsed(collapsed: Boolean) {
        isCardCollapsed = collapsed
        // When collapsed: show only current row and start button; hide chips and detail rows
        rowPinned.visibility = if (collapsed) View.GONE else View.VISIBLE
        rowStats.visibility = if (collapsed) View.GONE else View.VISIBLE
        
        // Show failure rate row only when navigating, not collapsed, and SOS warning is disabled
        val appSettings = com.example.gzingapp.utils.AppSettings(this)
        val showFailureRate = !collapsed && isNavigating && !appSettings.isSosWarningEnabled()
        rowFailureRate.visibility = if (showFailureRate) View.VISIBLE else View.GONE
        
        rowCurrent.visibility = View.VISIBLE
        btnStartNavigation.visibility = View.VISIBLE
        btnCollapse.setImageResource(if (collapsed) R.drawable.ic_expand_more else R.drawable.ic_expand_less)
    }

    private fun toggleLocationCard() { setCardCollapsed(!isCardCollapsed) }
    
    private fun toggleNavigationMode(enable: Boolean) {
        // Check if user is trying to start navigation without a pinned location
        if (enable && pinnedLocation == null) {
            Toast.makeText(this, "Please pin a location first before starting navigation", Toast.LENGTH_LONG).show()
            return
        }
        
        // Check if user has a valid current location
        if (enable && currentLocation == null) {
            Toast.makeText(this, "Please wait for GPS location before starting navigation", Toast.LENGTH_LONG).show()
            return
        }
        
        isNavigating = enable
        
        if (enable) {
            // Disable drawer and bottom navigation during navigation
            disableDrawerAndBottomNav()
            // Auto-save the route at navigation start
            try { createRouteFromNavigation() } catch (_: Exception) { }
            
            // Capture initial distance to destination when starting navigation
            if (currentLocation != null && pinnedLocation != null) {
                initialDistanceToDestination = haversineMeters(
                    currentLocation!!.latitude(), currentLocation!!.longitude(),
                    pinnedLocation!!.latitude(), pinnedLocation!!.longitude()
                )
                distanceIncreaseStartTime = 0  // Reset timer
                Log.d("MapActivity", "Navigation started - Initial distance to destination: ${String.format("%.2f", initialDistanceToDestination)}m")
            }
        } else {
            // Re-enable drawer and bottom navigation after navigation stops
            enableDrawerAndBottomNav()
            // Clear off-route indicator when navigation stops
            offRouteLine = null
            redrawAllLayers()
            // Cancel any active SOS warning dialog
            cancelOffRouteSosWarning()
            // Reset distance tracking when navigation stops
            distanceIncreaseStartTime = 0
            // Reset SOS cooldown when navigation stops
            lastSosSentTime = 0
            // Reset SOS warning flag when navigation stops
            sosWarningShownThisSession = false
            // Reset new state management variables
            offRoute = false
            isSafe = false
            cooldownEndTime = 0L
        }
        
        btnStartNavigation.text = if (enable) "Stop Navigation" else "Start Navigation"
        btnStartNavigation.setOnClickListener { toggleNavigationMode(!isNavigating) }
        if (enable) {
            // Reset arrival flag for new navigation session
            hasAnnouncedArrival = false
            navigationStartTime = System.currentTimeMillis() // Record navigation start time
            
            // Reset failure rate monitoring for new navigation session
            resetFailureRateMonitoring()
            
            // Reset SOS warning flag for new navigation session
            sosWarningShownThisSession = false
            // Reset new state management variables for new navigation session
            offRoute = false
            isSafe = false
            cooldownEndTime = 0L
            
            try { 
                android.util.Log.d("NavigationMode", "Starting navigation - hasAnnouncedArrival reset to false") 
            } catch (_: Exception) { }
            sendNavigationStartNotification()
        } else {
            // Cancel nav notification
            try {
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(2001)
            } catch (_: Exception) { }
            // Stop alarm service if running
            try {
                stopService(Intent(this, AlarmSoundService::class.java))
            } catch (_: Exception) { }
            // Stop TTS
            try { navTts?.stop(); navTts?.shutdown() } catch (_: Exception) { }
            navTts = null
        }
        Toast.makeText(this, if (enable) "Navigating..." else "Navigation stopped", Toast.LENGTH_SHORT).show()
    }
    
    private fun disableDrawerAndBottomNav() {
        Log.d("MapActivity", "=== disableDrawerAndBottomNav START ===")
        
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
            tabPlaces.isEnabled = false
            
            Log.d("MapActivity", "Drawer and bottom navigation disabled for navigation mode")
        } catch (e: Exception) {
            Log.e("MapActivity", "Error disabling drawer and bottom navigation", e)
        }
        
        Log.d("MapActivity", "=== disableDrawerAndBottomNav END ===")
    }
    
    private fun enableDrawerAndBottomNav() {
        Log.d("MapActivity", "=== enableDrawerAndBottomNav START ===")
        
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
            tabPlaces.isEnabled = true
            
            Log.d("MapActivity", "Drawer and bottom navigation re-enabled after navigation")
        } catch (e: Exception) {
            Log.e("MapActivity", "Error enabling drawer and bottom navigation", e)
        }
        
        Log.d("MapActivity", "=== enableDrawerAndBottomNav END ===")
    }

    private fun ensureNavNotificationChannel() {
        val channelId = "gzing_nav_channel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = nm.getNotificationChannel(channelId)
            if (existing == null) {
                val ch = NotificationChannel(channelId, "Navigation", NotificationManager.IMPORTANCE_HIGH)
                ch.description = "Navigation progress"
                nm.createNotificationChannel(ch)
            }
        }
    }

    private fun sendNavigationStartNotification() {
        val user = currentLocation
        val dest = pinnedLocation
        if (user == null || dest == null) return
        val meters = haversineMeters(user.latitude(), user.longitude(), dest.latitude(), dest.longitude())
        ensureNavNotificationChannel()
        CoroutineScope(Dispatchers.IO).launch {
            val name = try {
                val addr = getAddressFromCoordinates(dest.latitude(), dest.longitude())
                addr
            } catch (_: Exception) { "Destination" }
            withContext(Dispatchers.Main) {
                val channelId = "gzing_nav_channel"
                val intent = Intent(this@MapActivity, MapActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                val pi = PendingIntent.getActivity(this@MapActivity, 200, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                // Stop action via broadcast
                val stopIntent = Intent(this@MapActivity, StopNavigationReceiver::class.java).apply {
                    action = StopNavigationReceiver.ACTION_STOP_NAV
                }
                val stopPi = PendingIntent.getBroadcast(this@MapActivity, 201, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val text = "You are ${meters.toInt()} m close to the destination (${name})"
                val notif = NotificationCompat.Builder(this@MapActivity, channelId)
                    .setSmallIcon(R.drawable.ic_navigation)
                    .setContentTitle("Navigation started")
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setContentIntent(pi)
                    .addAction(R.drawable.ic_close, "Stop", stopPi)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(2001, notif)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("stop_navigation", false)) {
            toggleNavigationMode(false)
        }
    }
    
    private fun handleSelectedPlace() {
        val lat = intent.getDoubleExtra("selected_place_lat", 0.0)
        val lng = intent.getDoubleExtra("selected_place_lng", 0.0)
        val name = intent.getStringExtra("selected_place_name")
        val address = intent.getStringExtra("selected_place_address")
        val transportMode = intent.getStringExtra("transport_mode") ?: "Car"
        val fromNavigationHistory = intent.getBooleanExtra("from_navigation_history", false)
        val navigationHistoryId = intent.getIntExtra("navigation_history_id", 0)
        val fromSavedRoute = intent.getBooleanExtra("from_saved_route", false)
        val routeId = intent.getIntExtra("route_id", 0)
        val autoStartNavigation = intent.getBooleanExtra("auto_start_navigation", false)
        
        if (lat != 0.0 && lng != 0.0) {
            val point = Point.fromLngLat(lng, lat)
            
            // Check if the selected place is within allowed bounds
            if (!isWithinAllowedBounds(point)) {
                Toast.makeText(this, "Selected place is outside Antipolo/Marikina area. Please select a location within these cities.", Toast.LENGTH_LONG).show()
                return
            }
            
            pinnedLocation = point
            
            // Update UI with selected place info
            tvPinnedLocation?.text = name ?: "Selected Location"
            tvLocationAddress?.text = address ?: "Address not available"
            
            // Set transport mode
            selectedTransportMode = transportMode
            
            // Show appropriate toast message
            if (fromNavigationHistory) {
                Toast.makeText(this, "Navigation history loaded: $name", Toast.LENGTH_SHORT).show()
                Log.d("MapActivity", "Loaded navigation history ID: $navigationHistoryId")
            } else if (fromSavedRoute) {
                Toast.makeText(this, "Saved route loaded: $name", Toast.LENGTH_SHORT).show()
                Log.d("MapActivity", "Loaded saved route ID: $routeId")
            } else {
                Toast.makeText(this, "Place selected: $name", Toast.LENGTH_SHORT).show()
            }
            
            // If coming from navigation history or saved route, automatically set up the pin and route
            if (fromNavigationHistory) {
                setupNavigationFromHistory(point, name, address, transportMode)
            } else if (fromSavedRoute) {
                setupNavigationFromSavedRoute(point, name, address, transportMode, autoStartNavigation)
            }
            
            // Clear the intent extras to prevent re-processing
            intent.removeExtra("selected_place_lat")
            intent.removeExtra("selected_place_lng")
            intent.removeExtra("selected_place_name")
            intent.removeExtra("selected_place_address")
            intent.removeExtra("transport_mode")
            intent.removeExtra("from_navigation_history")
            intent.removeExtra("navigation_history_id")
            intent.removeExtra("from_saved_route")
            intent.removeExtra("route_id")
            intent.removeExtra("auto_start_navigation")
        }
    }
    
    private fun setupNavigationFromHistory(
        destinationPoint: Point, 
        destinationName: String?, 
        destinationAddress: String?, 
        transportMode: String
    ) {
        Log.d("MapActivity", "Setting up navigation from history: $destinationName")
        
        // Wait for map to be ready and current location to be available
        if (isMapStyleLoaded && currentLocation != null) {
            setupNavigationFromHistoryInternal(destinationPoint, destinationName, destinationAddress, transportMode)
        } else {
            // If map or location not ready, wait and retry
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isMapStyleLoaded && currentLocation != null) {
                    setupNavigationFromHistoryInternal(destinationPoint, destinationName, destinationAddress, transportMode)
                } else {
                    Log.w("MapActivity", "Map or location still not ready, navigation setup may be incomplete")
                }
            }, 2000) // Wait 2 seconds and retry
        }
    }
    
    private fun setupNavigationFromHistoryInternal(
        destinationPoint: Point, 
        destinationName: String?, 
        destinationAddress: String?, 
        transportMode: String
    ) {
        try {
            Log.d("MapActivity", "Setting up navigation from history internal")
            
            // Center the map on the destination point
            mapView.mapboxMap.setCamera(
                com.mapbox.maps.CameraOptions.Builder()
                    .center(destinationPoint)
                    .zoom(15.0)
                    .build()
            )
            
            // Update pinned location on map
            updatePinnedLocation(destinationPoint)
            
            // Perform reverse geocoding for the pinned location
            performReverseGeocodingForPin(destinationPoint)
            
            // Draw route if user location is available
            currentLocation?.let { userPoint ->
                updateRoute(userPoint, destinationPoint)
                Log.d("MapActivity", "Route updated from current location to destination")
                
                // Center camera to show both current location and destination
                // Calculate midpoint between user and destination
                val midLat = (userPoint.latitude() + destinationPoint.latitude()) / 2.0
                val midLng = (userPoint.longitude() + destinationPoint.longitude()) / 2.0
                val midPoint = Point.fromLngLat(midLng, midLat)
                
                mapView.mapboxMap.setCamera(
                    com.mapbox.maps.CameraOptions.Builder()
                        .center(midPoint)
                        .zoom(13.0) // Adjust zoom level to show both points
                        .build()
                )
            } ?: run {
                // If no current location, just center on destination
                Log.w("MapActivity", "No current location available, centering on destination only")
            }
            
            // Create geofence for the pinned location
            createGeofenceForPin(destinationPoint)
            Log.d("MapActivity", "Geofence created for destination")
            
            // Expand the location card to show all details
            setCardCollapsed(false)
            
            // Show success message
            Toast.makeText(this, "Navigation ready! Tap 'Start Navigation' to begin.", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Log.e("MapActivity", "Error setting up navigation from history", e)
            Toast.makeText(this, "Error setting up navigation: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupNavigationFromSavedRoute(
        destinationPoint: Point, 
        destinationName: String?, 
        destinationAddress: String?, 
        transportMode: String,
        autoStartNavigation: Boolean
    ) {
        Log.d("MapActivity", "Setting up navigation from saved route: $destinationName")
        
        // Wait for map to be ready and current location to be available
        if (isMapStyleLoaded && currentLocation != null) {
            setupNavigationFromSavedRouteInternal(destinationPoint, destinationName, destinationAddress, transportMode, autoStartNavigation)
        } else {
            // If map or location not ready, wait and retry
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isMapStyleLoaded && currentLocation != null) {
                    setupNavigationFromSavedRouteInternal(destinationPoint, destinationName, destinationAddress, transportMode, autoStartNavigation)
                } else {
                    Log.w("MapActivity", "Map or location still not ready, navigation setup may be incomplete")
                }
            }, 2000) // Wait 2 seconds and retry
        }
    }
    
    private fun setupNavigationFromSavedRouteInternal(
        destinationPoint: Point, 
        destinationName: String?, 
        destinationAddress: String?, 
        transportMode: String,
        autoStartNavigation: Boolean
    ) {
        try {
            Log.d("MapActivity", "Setting up navigation from saved route internal")
            
            // Center the map on the destination point
            mapView.mapboxMap.setCamera(
                com.mapbox.maps.CameraOptions.Builder()
                    .center(destinationPoint)
                    .zoom(15.0)
                    .build()
            )
            
            // Update pinned location on map
            updatePinnedLocation(destinationPoint)
            
            // Perform reverse geocoding for the pinned location
            performReverseGeocodingForPin(destinationPoint)
            
            // Draw route if user location is available
            currentLocation?.let { userPoint ->
                updateRoute(userPoint, destinationPoint)
                Log.d("MapActivity", "Route updated from current location to destination")
                
                // Center camera to show both current location and destination
                // Calculate midpoint between user and destination
                val midLat = (userPoint.latitude() + destinationPoint.latitude()) / 2.0
                val midLng = (userPoint.longitude() + destinationPoint.longitude()) / 2.0
                val midPoint = Point.fromLngLat(midLng, midLat)
                
                mapView.mapboxMap.setCamera(
                    com.mapbox.maps.CameraOptions.Builder()
                        .center(midPoint)
                        .zoom(13.0) // Adjust zoom level to show both points
                        .build()
                )
            } ?: run {
                // If no current location, just center on destination
                Log.w("MapActivity", "No current location available, centering on destination only")
            }
            
            // Create geofence for the pinned location
            createGeofenceForPin(destinationPoint)
            Log.d("MapActivity", "Geofence created for destination")
            
            // Expand the location card to show all details
            setCardCollapsed(false)
            
            // Auto-start navigation if requested
            if (autoStartNavigation) {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    toggleNavigationMode(true)
                    Toast.makeText(this, "Navigation started automatically!", Toast.LENGTH_LONG).show()
                }, 1000) // Start navigation 1 second after setup
            } else {
                // Show success message
                Toast.makeText(this, "Route ready! Tap 'Start Navigation' to begin.", Toast.LENGTH_LONG).show()
            }
            
        } catch (e: Exception) {
            Log.e("MapActivity", "Error setting up navigation from saved route", e)
            Toast.makeText(this, "Error setting up navigation: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupNavigationDrawer() {
        // Attach Toolbar as ActionBar
        setSupportActionBar(toolbar)

        // Setup ActionBarDrawerToggle with Toolbar for hamburger icon
        actionBarDrawerToggle = ActionBarDrawerToggle(
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
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            } else {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        
        // Setup navigation menu item selection
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_places -> {
                    val intent = Intent(this, PlacesActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_saved_routes -> {
                    val intent = Intent(this, NavigationRoutesActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    logout()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
        
        // Update navigation header with user info
        updateNavigationHeader()
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
    
    private fun updateNavigationButtonState() {
        val canNavigate = pinnedLocation != null && currentLocation != null
        
        btnStartNavigation.isEnabled = canNavigate
        btnStartNavigation.alpha = if (canNavigate) 1.0f else 0.5f
        
        if (!canNavigate) {
            btnStartNavigation.text = if (pinnedLocation == null) "Pin a location first" else "Waiting for GPS..."
        } else {
            btnStartNavigation.text = if (isNavigating) "Stop Navigation" else "Start Navigation"
        }
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
    
    private fun setupLocationCallback() {
        locationRequest = LocationRequest.Builder(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            3000L // 3 seconds for faster updates
        ).apply {
            setMinUpdateIntervalMillis(1000L) // 1 second minimum for real-time proximity
            setMaxUpdateDelayMillis(5000L) // 5 seconds maximum
        }.build()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d("MapActivity", "Location update: ${location.latitude}, ${location.longitude}")
                    Log.d("MapActivity", "Location accuracy: ${location.accuracy}m")
                    
                    // Accept any valid location (not just non-zero coordinates)
                    if (location.latitude != 0.0 || location.longitude != 0.0) {
                        val point = Point.fromLngLat(location.longitude, location.latitude)
                        updateUserLocation(point)
                        Log.d("MapActivity", "✅ Real-time GPS location updated: ${location.latitude}, ${location.longitude}")
                        
                        // Real-time proximity check for geofence
                        if (isNavigating && pinnedLocation != null) {
                            checkRealTimeProximity(point, pinnedLocation!!)
                            // Monitor route deviation for failure rate
                            checkRouteDeviation(point)
                            // Monitor distance increase to destination
                            checkDistanceIncrease(point)
                        }
                    } else {
                        Log.w("MapActivity", "Real-time location has zero coordinates")
                    }
                }
            }
        }
    }
    
    private fun setupMap() {
        mapView.mapboxMap.loadStyle(
            style(getCurrentStyleUri()) {
                // Map style is loaded
                isMapStyleLoaded = true
                
                // Add custom pin image for markers
                vectorToBitmap(R.drawable.ic_custom_pin)?.let { bmp ->
                    +image("pin-icon", bmp)
                }

                // Add user location source
                +geoJsonSource("user-location") {
                    geometry(Point.fromLngLat(0.0, 0.0)) // Initial dummy point
                }
                
                // Add user location layer
                +circleLayer("user-location-layer", "user-location") {
                    circleRadius(12.0)
                    circleColor("#1976D2")
                    circleStrokeColor("#FFFFFF")
                    circleStrokeWidth(3.0)
                    circleOpacity(0.9)
                }
                
                // Add pinned location source
                +geoJsonSource("pinned-location") {
                    geometry(Point.fromLngLat(0.0, 0.0)) // Initial dummy point
                }
                
                // Add pinned location as a symbol with custom pin icon
                +symbolLayer("pinned-symbol-layer", "pinned-location") {
                    iconImage("pin-icon")
                    iconSize(0.8)
                    iconAllowOverlap(true)
                    iconIgnorePlacement(true)
                }
                
                // Add route source
                +geoJsonSource("route") {
                    geometry(LineString.fromLngLats(listOf())) // Initial empty line
                }
                
                // Add route layer
                +lineLayer("route-layer", "route") {
                    lineColor("#FF5722")
                    lineWidth(6.0)
                    lineOpacity(0.8)
                }

                // Add off-route deviation source and layer (red)
                +geoJsonSource("offroute") {
                    geometry(LineString.fromLngLats(listOf()))
                }
                +lineLayer("offroute-layer", "offroute") {
                    lineColor("#E74C3C")
                    lineWidth(6.0)
                    lineOpacity(0.9)
                    lineCap(LineCap.ROUND)
                    lineJoin(LineJoin.ROUND)
                }
                
                // Note: Allowed area boundary removed to avoid API conflicts
            }
        )

        // Enable built-in blue location puck (version-agnostic)
        try {
            mapView.location.updateSettings { enabled = true }
        } catch (_: Exception) { }
        
        // Map click toggles pin: if exists -> replace; if none -> create
        mapView.gestures.addOnMapClickListener { point ->
            if (isNavigating) {
                Toast.makeText(this, "Cannot pin while navigating", Toast.LENGTH_SHORT).show()
                return@addOnMapClickListener true
            }
                pinLocation(point)
            true
        }
    }

    
    
    private fun setupMapLayers() {
        if (!isMapStyleLoaded) return
        
        // This method will be called when the style is loaded
        // The layers will be added in the style block
    }
    
    
    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                getCurrentLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // Show explanation and request permission
                Toast.makeText(this, "Location permission is needed to show your current location", Toast.LENGTH_LONG).show()
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            else -> {
                // Request permission
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getCurrentStyleUri(): String {
        return if (trafficEnabled) Style.TRAFFIC_DAY else Style.MAPBOX_STREETS
    }

    private fun applyCurrentStyleAndRedraw() {
        if (!::mapView.isInitialized) return
        
        // Only reload style if traffic setting changed, otherwise update layers directly
        if (trafficEnabled != lastTrafficState) {
            lastTrafficState = trafficEnabled
            mapView.mapboxMap.loadStyle(
                style(getCurrentStyleUri()) {
                    vectorToBitmap(R.drawable.ic_custom_pin)?.let { bmp ->
                        +image("pin-icon", bmp)
                    }
                    redrawAllLayers()
                }
            )
        } else {
            // Just redraw layers without reloading style
            redrawAllLayers()
        }
    }
    
    private fun redrawAllLayers() {
        if (!isMapStyleLoaded) return
        
        // For now, just reload the style with current data to avoid API complexity
        // This is still more efficient than the previous approach since we only do it when traffic changes
        mapView.mapboxMap.loadStyle(
            style(getCurrentStyleUri()) {
                vectorToBitmap(R.drawable.ic_custom_pin)?.let { bmp ->
                    +image("pin-icon", bmp)
                }
                currentLocation?.let { userPoint ->
                    +geoJsonSource("user-location") { geometry(userPoint) }
                    +circleLayer("user-location-layer", "user-location") {
                        circleRadius(12.0)
                        circleColor("#1976D2")
                        circleStrokeColor("#FFFFFF")
                        circleStrokeWidth(3.0)
                        circleOpacity(0.9)
                    }
                }
                pinnedLocation?.let { pinPoint ->
                    +geoJsonSource("pinned-location") { geometry(pinPoint) }
                    +symbolLayer("pinned-symbol-layer", "pinned-location") {
                        iconImage("pin-icon")
                        iconSize(0.8)
                        iconAllowOverlap(true)
                        iconIgnorePlacement(true)
                    }
                    // Geofence overlay (visual)
                    try {
                        val radiusMeters = com.example.gzingapp.utils.AppSettings(this@MapActivity).getAlarmFenceRadiusMeters().toDouble()
                        val polygon = generateGeofencePolygon(pinPoint, radiusMeters)
                        +geoJsonSource("geofence") { geometry(polygon) }
                        +fillLayer("geofence-layer", "geofence") {
                            fillColor("#1E90FF")
                            fillOpacity(0.2)
                        }
                        +lineLayer("geofence-outline", "geofence") {
                            lineColor("#1E90FF")
                            lineWidth(2.0)
                        }
                    } catch (_: Exception) { }
                }
                routeCoordinates?.let { coordinates ->
                    if (coordinates.size >= 2) {
                        +geoJsonSource("route") { geometry(LineString.fromLngLats(coordinates)) }
                        +lineLayer("route-layer", "route") {
                            // ORIGINAL ROUTE: BLUE
                            lineColor("#1976D2")
                            lineWidth(6.0)
                            lineOpacity(0.9)
                        }
                    }
                }
                // OFF-ROUTE: RED indicator
                offRouteLine?.let { offPoints ->
                    if (offPoints.size >= 2) {
                        +geoJsonSource("offroute") { geometry(LineString.fromLngLats(offPoints)) }
                        +lineLayer("offroute-layer", "offroute") {
                            lineColor("#E74C3C")
                            lineWidth(6.0)
                            lineOpacity(0.9)
                            lineCap(LineCap.ROUND)
                            lineJoin(LineJoin.ROUND)
                        }
                    } else {
                        +geoJsonSource("offroute") { geometry(LineString.fromLngLats(listOf())) }
                    }
                } ?: run {
                    +geoJsonSource("offroute") { geometry(LineString.fromLngLats(listOf())) }
                }
                
                // Note: Allowed area boundary removed to avoid API conflicts
            }
        )
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
            return
        }
        
        Log.d("MapActivity", "Requesting current location...")
        
        // Get last known location first
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d("MapActivity", "Last known location: ${location.latitude}, ${location.longitude}")
                    Log.d("MapActivity", "Location accuracy: ${location.accuracy}m")
                    Log.d("MapActivity", "Location time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(location.time))}")
                    
                    // Accept any valid location (not just non-zero coordinates)
                    if (location.latitude != 0.0 || location.longitude != 0.0) {
                        val point = Point.fromLngLat(location.longitude, location.latitude)
                        updateUserLocation(point)
                        performReverseGeocoding(point)
                        Log.d("MapActivity", "✅ GPS location captured: ${location.latitude}, ${location.longitude}")
                    } else {
                        Log.w("MapActivity", "Last known location has zero coordinates, will rely on real-time updates")
                        showDefaultLocation()
                    }
                } else {
                    Log.w("MapActivity", "No last known location, will rely on real-time updates")
                    showDefaultLocation()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MapActivity", "Failed to get last known location", exception)
                showDefaultLocation()
            }
        
        // Start real-time location updates
        startLocationUpdates()
        // Start foreground notification service after permissions are granted
        try {
            val svcIntent = Intent(this, LocationNotificationService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(this, svcIntent)
        } catch (_: Exception) { }
    }
    
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission required for real-time updates", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    private fun updateUserLocation(point: Point) {
        currentLocation = point
        
        // Update navigation button state when current location changes
        updateNavigationButtonState()
        
        if (!isMapStyleLoaded) return
        
        // Update user location by redrawing layers
        redrawAllLayers()
        
        // Auto-center functionality removed - user can manually center using FAB

        // If a pin exists, fetch and redraw the route from the refreshed location
        pinnedLocation?.let { pinPoint ->
            updateRoute(point, pinPoint)
        }
    }
    
    private fun showUserLocation(point: Point) {
        currentLocation = point
        
        if (!isMapStyleLoaded) {
            // If map style not loaded, just update the location
            updateUserLocation(point)
            return
        }
        
        // Update location without moving camera
        updateUserLocation(point)
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
                    Toast.makeText(this@MapActivity, "Failed to get address: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private suspend fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                // Using OpenStreetMap Nominatim API for reverse geocoding (free)
                val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&zoom=18&addressdetails=1"
                val connection = URL(url).openConnection()
                connection.setRequestProperty("User-Agent", "GzingApp/1.0")
                
                val response = connection.getInputStream().bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                
                val displayName = jsonObject.optString("display_name", "")
                val address = jsonObject.optJSONObject("address")
                
                if (displayName.isNotEmpty()) {
                    // Format the address nicely
                    val parts = displayName.split(", ")
                    if (parts.size >= 3) {
                        "${parts[0]}, ${parts[1]}, ${parts[2]}"
                    } else {
                        displayName
                    }
                } else {
                    "Location: $lat, $lng"
                }
            } catch (e: Exception) {
                "Location: $lat, $lng"
            }
        }
    }
    
    private fun showDefaultLocation() {
        // Default to Manila, Philippines
        val defaultPoint = Point.fromLngLat(120.9842, 14.5995)
        
        // Move camera to default location
        mapView.mapboxMap.setCamera(
            com.mapbox.maps.CameraOptions.Builder()
                .center(defaultPoint)
                .zoom(10.0)
                .build()
        )
        
        // Show default location info
        tvLocationAddress?.text = "Manila, Philippines"
    }
    
    private fun togglePinningMode() {
        isPinningMode = !isPinningMode
        
        if (isPinningMode) {
            fabAddLocation.setImageResource(R.drawable.ic_close)

        } else {
            fabAddLocation.setImageResource(R.drawable.ic_add)
            if (pinnedLocation != null) {
                clearPin()

            } else {
                Toast.makeText(this, "Pinning mode disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun pinLocation(point: Point) {
        // Check if the location is within Antipolo/Marikina bounds
        if (!isWithinAllowedBounds(point)) {
            Toast.makeText(this, "Location is outside Antipolo/Marikina area. Please select a location within these cities.", Toast.LENGTH_LONG).show()
            return
        }
        
        // Remove existing pin if any
        clearPin()
        
        // Set new pinned location
        pinnedLocation = point
        
        // Update navigation button state when pin is set
        updateNavigationButtonState()
        
        // Ask for transport mode
        showTransportModeDialog { mode ->
            selectedTransportMode = mode
            Log.d("MapActivity", "Transportation mode selected: $mode")
            // Update pinned location on map
            updatePinnedLocation(point)
        // Perform reverse geocoding for the pinned location
        performReverseGeocodingForPin(point)
        // Draw route if user location is available
        currentLocation?.let { userPoint ->
                updateRoute(userPoint, point)
            }
        }
        
        // Disable pinning mode after placing pin
        isPinningMode = false
        fabAddLocation.setImageResource(R.drawable.ic_add)
        
        // Create geofence for the pinned location
        createGeofenceForPin(point)
        

    }
    
    private fun isWithinAllowedBounds(point: Point): Boolean {
        val lat = point.latitude()
        val lng = point.longitude()
        
        // Define bounds for Antipolo and Marikina areas
        // Antipolo bounds (approximate)
        val antipoloNorth = 14.75
        val antipoloSouth = 14.55
        val antipoloEast = 121.25
        val antipoloWest = 121.10
        
        // Marikina bounds (approximate)
        val marikinaNorth = 14.70
        val marikinaSouth = 14.60
        val marikinaEast = 121.15
        val marikinaWest = 121.00
        
        // Check if point is within Antipolo bounds
        val withinAntipolo = lat >= antipoloSouth && lat <= antipoloNorth && 
                           lng >= antipoloWest && lng <= antipoloEast
        
        // Check if point is within Marikina bounds
        val withinMarikina = lat >= marikinaSouth && lat <= marikinaNorth && 
                           lng >= marikinaWest && lng <= marikinaEast
        
        return withinAntipolo || withinMarikina
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

    // Map style chooser removed
    
    private fun updatePinnedLocation(point: Point) {
        if (!isMapStyleLoaded) return
        
        // Update pinned location by redrawing layers
        redrawAllLayers()
    }
    
    private fun updateRoute(startPoint: Point, endPoint: Point) {
        if (!isMapStyleLoaded) return
        
        Log.d("MapActivity", "Updating route with transportation mode: $selectedTransportMode")
        
        // Fetch real route from Mapbox Directions API
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val routeLineString = fetchRouteLineString(startPoint, endPoint)
                withContext(Dispatchers.Main) {
                    if (routeLineString != null) {
                        // Compute distance and ETA (approx) from geometry
                        val distanceMeters = estimateLineStringDistanceMeters(routeLineString)
                        tvDistance.text = "Distance: ${formatDistance(distanceMeters)}"
                        val etaMin = estimateEtaMinutes(distanceMeters, currentTransportMode())
                        tvEta.text = "ETA: ${etaMin.toInt()} min"
                        tvFare.text = formatFare(distanceMeters)
                        Log.d("MapActivity", "Route updated - Mode: $selectedTransportMode, Distance: ${formatDistance(distanceMeters)}, ETA: ${etaMin.toInt()} min, Fare: ${formatFare(distanceMeters)}")
                        // Traffic severity from ratio of first route duration to best alternative
                        val severity = computeTrafficSeverity()
                        tvTrafficStatus.text = severity
                        try {
                            val color = when (severity) {
                                "Heavy" -> android.graphics.Color.parseColor("#E74C3C")
                                "Moderate" -> android.graphics.Color.parseColor("#F39C12")
                                else -> android.graphics.Color.parseColor("#27AE60")
                            }
                            (trafficDot.background as? android.graphics.drawable.GradientDrawable)?.setColor(color)
                        } catch (_: Exception) { }
                        // Update route by redrawing layers
                        routeCoordinates = routeLineString.coordinates()
                        redrawAllLayers()

                        // Near detection each route update
                        checkArrivalProximity(distanceMeters)
                    } else {

                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapActivity, "Route error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Rough distance computation over a LineString in meters
    private fun estimateLineStringDistanceMeters(line: LineString): Double {
        val coords = line.coordinates()
        var total = 0.0
        for (i in 1 until coords.size) {
            total += haversineMeters(
                coords[i - 1].latitude(), coords[i - 1].longitude(),
                coords[i].latitude(), coords[i].longitude()
            )
        }
        return total
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun formatDistance(meters: Double): String {
        return if (meters >= 1000) "${String.format("%.1f", meters / 1000)} km" else "${meters.toInt()} m"
    }

    // Return selected transport mode average speed (m/s)
    private fun currentTransportMode(): String {
        return selectedTransportMode
    }

    private fun estimateEtaMinutes(distanceMeters: Double, mode: String): Double {
        val speed = when (mode.lowercase()) {
            "car", "driving" -> 13.9 // ~50 km/h
            "walk", "walking" -> 1.4 // ~5 km/h
            "motor", "cycling", "bike" -> 8.3 // ~30 km/h
            "public_transport", "transit" -> 6.9 // ~25 km/h
            else -> 13.9 // Default to car speed
        }
        return distanceMeters / speed / 60.0
    }

    private fun formatFare(distanceMeters: Double): String {
        val km = distanceMeters / 1000.0
        val fare = when (selectedTransportMode.lowercase()) {
            "car", "driving" -> {
                // First 5 km is 15 pesos, exceeding kilometers is 5 pesos per km
                val baseFare = 15.0
                val extraKm = if (km > 5.0) km - 5.0 else 0.0
                baseFare + (extraKm * 5.0)
            }
            "walk", "walking" -> 0.0 // Walking is free
            "motor", "cycling", "bike" -> {
                // First 5 km is 15 pesos, exceeding kilometers is 5 pesos per km
                val baseFare = 15.0
                val extraKm = if (km > 5.0) km - 5.0 else 0.0
                baseFare + (extraKm * 5.0)
            }
            "public_transport", "transit" -> {
                // First 5 km is 15 pesos, exceeding kilometers is 5 pesos per km
                val baseFare = 15.0
                val extraKm = if (km > 5.0) km - 5.0 else 0.0
                baseFare + (extraKm * 5.0)
            }
            else -> {
                // First 5 km is 15 pesos, exceeding kilometers is 5 pesos per km
                val baseFare = 15.0
                val extraKm = if (km > 5.0) km - 5.0 else 0.0
                baseFare + (extraKm * 5.0)
            }
        }
        return if (fare > 0) "₱ ${String.format("%.2f", fare)}" else "Free"
    }
    
    private fun checkArrivalProximity(distanceMeters: Double) {
        if (!isNavigating || hasAnnouncedArrival) return
        val radius = com.example.gzingapp.utils.AppSettings(this).getAlarmFenceRadiusMeters().toDouble()
        try { android.util.Log.d("ArrivalCheck", "distance=${String.format("%.2f", distanceMeters)} radius=$radius navigating=$isNavigating") } catch (_: Exception) { }
        if (distanceMeters <= radius) {
            hasAnnouncedArrival = true
            createNavigationHistoryOnDestinationReached() // Create navigation history when destination reached
            // Start alarm if not already
            try {
                val svc = Intent(this, AlarmSoundService::class.java)
                androidx.core.content.ContextCompat.startForegroundService(this, svc)
            } catch (_: Exception) { }
            // Voice announcement if enabled
            try {
                val settings = com.example.gzingapp.utils.AppSettings(this)
                if (settings.isVoiceAnnouncementsEnabled()) {
                    val name = tvPinnedLocation.text?.toString()?.ifBlank { "Destination" } ?: "Destination"
                    try { navTts?.stop(); navTts?.shutdown() } catch (_: Exception) { }
                    navTts = android.speech.tts.TextToSpeech(this) { status ->
                        if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                            try {
                                navTts?.language = java.util.Locale.getDefault()
                                navTts?.speak("Arriving at ${name} Destination", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "arrive_voice")
                            } catch (_: Exception) { }
                        }
                    }
                }
            } catch (_: Exception) { }
            // Heads-up arrival notification (Stop allowed via receiver)
            try {
                val channelId = "gzing_geofence_channel"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val existing = nm.getNotificationChannel(channelId)
                    if (existing == null) {
                        val ch = NotificationChannel(channelId, "Geofence", NotificationManager.IMPORTANCE_HIGH)
                        ch.description = "Geofence alerts"
                        nm.createNotificationChannel(ch)
                    }
                }
                val stopIntent = Intent(this, StopNavigationReceiver::class.java).apply { action = StopNavigationReceiver.ACTION_STOP_NAV }
                val stopPi = PendingIntent.getBroadcast(this, 2202, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val notif = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_navigation)
                    .setContentTitle("You are near your destination")
                    .setContentText("Tap Stop to end navigation")
                    .addAction(R.drawable.ic_close, "Stop", stopPi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(2202, notif)
            } catch (_: Exception) { }
            
            // Show save route dialog after a short delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                showSaveRouteDialog()
            }, 3000) // Show dialog 3 seconds after arrival
        }
    }
    
    private fun checkRealTimeProximity(userPoint: Point, destinationPoint: Point) {
        if (!isNavigating) {
            try { 
                android.util.Log.d("RealTimeProximity", "Not navigating - skipping proximity check") 
            } catch (_: Exception) { }
            return
        }
        if (hasAnnouncedArrival) {
            try { 
                android.util.Log.d("RealTimeProximity", "Already announced arrival - skipping proximity check") 
            } catch (_: Exception) { }
            return
        }
        
        // Calculate direct distance between user and destination
        val distanceMeters = calculateDirectDistance(userPoint, destinationPoint)
        val radius = com.example.gzingapp.utils.AppSettings(this).getAlarmFenceRadiusMeters().toDouble()
        
        try { 
            android.util.Log.d("RealTimeProximity", "Direct distance=${String.format("%.2f", distanceMeters)}m, radius=${radius}m, navigating=$isNavigating") 
        } catch (_: Exception) { }
        
        if (distanceMeters <= radius) {
            hasAnnouncedArrival = true
            try { 
                android.util.Log.d("RealTimeProximity", "TRIGGERING ARRIVAL - User entered geofence!") 
            } catch (_: Exception) { }
            
            // Start alarm if not already
            try {
                val svc = Intent(this, AlarmSoundService::class.java)
                androidx.core.content.ContextCompat.startForegroundService(this, svc)
            } catch (_: Exception) { }
            
            // Voice announcement if enabled
            try {
                val settings = com.example.gzingapp.utils.AppSettings(this)
                if (settings.isVoiceAnnouncementsEnabled()) {
                    val name = tvPinnedLocation.text?.toString()?.ifBlank { "Destination" } ?: "Destination"
                    try { navTts?.stop(); navTts?.shutdown() } catch (_: Exception) { }
                    navTts = android.speech.tts.TextToSpeech(this) { status ->
                        if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                            try {
                                navTts?.language = java.util.Locale.getDefault()
                                navTts?.speak("Arriving at ${name} Destination", android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "arrive_voice")
                            } catch (_: Exception) { }
                        }
                    }
                }
            } catch (_: Exception) { }
            
            // Heads-up arrival notification (Stop allowed via receiver)
            try {
                val channelId = "gzing_geofence_channel"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val existing = nm.getNotificationChannel(channelId)
                    if (existing == null) {
                        val ch = NotificationChannel(channelId, "Geofence", NotificationManager.IMPORTANCE_HIGH)
                        ch.description = "Geofence alerts"
                        nm.createNotificationChannel(ch)
                    }
                }
                val stopIntent = Intent(this, StopNavigationReceiver::class.java).apply { action = StopNavigationReceiver.ACTION_STOP_NAV }
                val stopPi = PendingIntent.getBroadcast(this, 2202, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val notif = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_navigation)
                    .setContentTitle("You are near your destination")
                    .setContentText("Tap Stop to end navigation")
                    .addAction(R.drawable.ic_close, "Stop", stopPi)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(2202, notif)
            } catch (_: Exception) { }
            
            // Show save route dialog after a short delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                showSaveRouteDialog()
            }, 3000) // Show dialog 3 seconds after arrival
        }
    }
    
    private fun calculateDirectDistance(point1: Point, point2: Point): Double {
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
    
    private fun createGeofenceForPin(point: Point) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // Request background location on Android 10+ for reliable geofencing
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val bgGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!bgGranted) {
                Toast.makeText(this, "Enable 'Allow all the time' location for geofence alerts", Toast.LENGTH_LONG).show()
                try {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 2005)
                } catch (_: Exception) { }
            }
        }
        
        val appSettings = com.example.gzingapp.utils.AppSettings(this)
        val radius = appSettings.getAlarmFenceRadiusMeters().toFloat()
        
        val geofence = Geofence.Builder()
            .setRequestId("pinned_location_geofence")
            .setCircularRegion(point.latitude(), point.longitude(), radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        
        val pendingIntent = getGeofencePendingIntent()
        
        // Replace any existing geofence for the pin
        geofencingClient.removeGeofences(pendingIntent)
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Toast.makeText(this, "Geofence created (${radius.toInt()}m). Will alert on enter.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create geofence: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun generateGeofencePolygon(center: Point, radiusMeters: Double): Polygon {
        val points = mutableListOf<Point>()
        val steps = 64
        val lat = center.latitude()
        val lon = center.longitude()
        val earthRadius = 6371000.0
        for (i in 0..steps) {
            val theta = 2.0 * Math.PI * i / steps
            val dx = radiusMeters * Math.cos(theta)
            val dy = radiusMeters * Math.sin(theta)
            val dLat = (dy / earthRadius) * (180.0 / Math.PI)
            val dLon = (dx / (earthRadius * Math.cos(Math.toRadians(lat)))) * (180.0 / Math.PI)
            points.add(Point.fromLngLat(lon + dLon, lat + dLat))
        }
        return Polygon.fromLngLats(listOf(points))
    }

    private fun computeTrafficSeverity(): String {
        if (alternativeDurationsSec.isEmpty()) return if (trafficEnabled) "Moderate" else "No Traffic"
        val sorted = alternativeDurationsSec.filter { !it.isNaN() }.sorted()
        if (sorted.isEmpty()) return if (trafficEnabled) "Moderate" else "No Traffic"
        val best = sorted.first()
        val primary = alternativeDurationsSec.firstOrNull { !it.isNaN() } ?: best
        val ratio = if (best > 0) primary / best else 1.0
        return when {
            ratio >= 1.3 -> "Heavy"
            ratio >= 1.1 -> "Moderate"
            else -> "No Traffic"
        }
    }
    
    private fun clearPin() {
        if (pinnedLocation != null) {
            pinnedLocation = null
            
            // Update navigation button state when pin is cleared
            updateNavigationButtonState()
            
            // Clear route
            routeCoordinates = null
            clearRoute()
            // Remove system geofence
            try {
                geofencingClient.removeGeofences(getGeofencePendingIntent())
            } catch (_: Exception) { }
            
            tvLocationAddress?.text = "Tap to get your location"
        }
    }
    
    private fun clearRoute() {
        if (!isMapStyleLoaded) return
        
        // Clear route by redrawing layers
        routeCoordinates = null
        redrawAllLayers()
    }

    // Convert vector drawable to Bitmap for Mapbox symbol image
    private fun vectorToBitmap(drawableResId: Int): Bitmap? {
        val drawable: Drawable = ResourcesCompat.getDrawable(resources, drawableResId, theme)
            ?: return null
        val wrappedDrawable = DrawableCompat.wrap(drawable).mutate()
        val width = if (wrappedDrawable.intrinsicWidth > 0) wrappedDrawable.intrinsicWidth else 96
        val height = if (wrappedDrawable.intrinsicHeight > 0) wrappedDrawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        wrappedDrawable.setBounds(0, 0, canvas.width, canvas.height)
        wrappedDrawable.draw(canvas)
        return bitmap
    }

    // Fetch a real route LineString from Mapbox Directions API
    private suspend fun fetchRouteLineString(start: Point, end: Point): LineString? {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = getString(R.string.mapbox_access_token)
                val profile = when (selectedTransportMode.lowercase()) {
                    "car", "driving" -> if (trafficEnabled) "driving-traffic" else "driving"
                    "walk", "walking" -> "walking"
                    "motor", "cycling", "bike" -> "cycling"
                    "public_transport", "transit" -> "driving" // Use driving for public transport as fallback
                    else -> if (trafficEnabled) "driving-traffic" else "driving"
                }
                Log.d("MapActivity", "Using Mapbox profile: $profile for transport mode: $selectedTransportMode")
                val url =
                    "https://api.mapbox.com/directions/v5/mapbox/$profile/" +
                        "${start.longitude()},${start.latitude()};${end.longitude()},${end.latitude()}" +
                        "?alternatives=true&geometries=geojson&steps=false&overview=full&annotations=duration&access_token=$accessToken"
                val connection = URL(url).openConnection()
                connection.setRequestProperty("User-Agent", "GzingApp/1.0")
                val response = connection.getInputStream().bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val routes = json.optJSONArray("routes")
                if (routes != null && routes.length() > 0) {
                    // Collect alternatives
                    val lines = mutableListOf<LineString>()
                    val durations = mutableListOf<Double>()
                    for (r in 0 until routes.length()) {
                        val routeObj = routes.getJSONObject(r)
                        val geometry = routeObj.getJSONObject("geometry")
                        val coords = geometry.getJSONArray("coordinates")
                        val points = mutableListOf<Point>()
                        for (i in 0 until coords.length()) {
                            val pair = coords.getJSONArray(i)
                            val lng = pair.getDouble(0)
                            val lat = pair.getDouble(1)
                            points.add(Point.fromLngLat(lng, lat))
                        }
                        lines.add(LineString.fromLngLats(points))
                        durations.add(routeObj.optDouble("duration", Double.NaN))
                    }
                    alternativeRoutes = lines
                    alternativeDurationsSec = durations
                    currentRouteIndex = 0
                    lines.first()
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // Lifecycle methods to manage location updates
    override fun onResume() {
        super.onResume()
        if (::locationCallback.isInitialized) {
            startLocationUpdates()
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (::locationCallback.isInitialized) {
            stopLocationUpdates()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::locationCallback.isInitialized) {
            stopLocationUpdates()
        }
    }
    
    private fun performReverseGeocodingForPin(point: Point) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val address = getAddressFromCoordinates(point.latitude(), point.longitude())
                withContext(Dispatchers.Main) {
                    // Update the location card with pinned location info
                    tvLocationAddress?.text = "📍 Pinned: $address"
                    tvPinnedLocation.text = address
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val fallback = "📍 Pinned: ${point.latitude()}, ${point.longitude()}"
                    tvLocationAddress?.text = fallback
                    tvPinnedLocation.text = fallback
                    Toast.makeText(this@MapActivity, "Failed to get address: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
        
        Log.d("MapActivity", "Showing SOS dialog for user ID: $userId")
        
        // Get current location data
        val currentLoc = currentLocation
        val latitude = currentLoc?.latitude()
        val longitude = currentLoc?.longitude()
        val locationString = tvCurrentLocation.text?.toString() ?: "Location not available"
        
        Log.d("MapActivity", "SOS Dialog - Current location: $currentLoc")
        Log.d("MapActivity", "SOS Dialog - Coordinates: $latitude, $longitude")
        Log.d("MapActivity", "SOS Dialog - Location string: $locationString")
        
        val sosDialog = com.example.gzingapp.ui.SosHelpDialog(
            context = this,
            onSosSent = {
                // Handle successful SOS sent
                Toast.makeText(this, "Emergency SMS sent to all contacts!", Toast.LENGTH_LONG).show()
            },
            onDismiss = {
                // Handle dialog dismissal
                Log.d("SosDialog", "SOS dialog dismissed")
            },
            initialLatitude = latitude,
            initialLongitude = longitude,
            initialLocation = locationString
        )
        sosDialog.show()
    }
    
    private fun sendEmergencySMS() {
        // Check if location is available
        if (currentLocation == null) {
            Toast.makeText(this, "Location not available. Please wait for GPS signal.", Toast.LENGTH_LONG).show()
            return
        }
        
        // Get emergency contacts
        val emergencySMSService = com.example.gzingapp.service.EmergencySMSService(this)
        val contacts = emergencySMSService.getEmergencyContacts()
        
        if (contacts.isEmpty()) {
            Toast.makeText(this, "No emergency contacts configured. Please add contacts in settings.", Toast.LENGTH_LONG).show()
            return
        }
        
        // Show sending message
        Toast.makeText(this, "🚨 Sending emergency SMS...", Toast.LENGTH_SHORT).show()
        
        // Send SMS
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = emergencySMSService.sendEmergencySMSWithDefaultMessage(
                    latitude = currentLocation!!.latitude(),
                    longitude = currentLocation!!.longitude(),
                    contacts = contacts
                )
                
                result.onSuccess { response ->
                    val successCount = response.data?.successfulSends ?: 0
                    val totalCount = response.data?.totalContacts ?: 0
                    
                    if (successCount > 0) {
                        Toast.makeText(
                            this@MapActivity,
                            "🚨 Emergency SMS sent to $successCount out of $totalCount contacts!",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d("MapActivity", "Emergency SMS sent successfully to $successCount contacts")
                        
                        // Log location info
                        response.data?.locationInfo?.let { locationInfo ->
                            Log.d("MapActivity", "Location: ${locationInfo.formattedAddress}")
                        }
                    } else {
                        Toast.makeText(
                            this@MapActivity,
                            "❌ Failed to send emergency SMS. Please check your contacts and try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("MapActivity", "Failed to send emergency SMS to any contacts")
                    }
                }.onFailure { error ->
                    Toast.makeText(
                        this@MapActivity,
                        "❌ Emergency SMS failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("MapActivity", "Error sending emergency SMS", error)
                }
                
        } catch (e: Exception) {
                Toast.makeText(
                    this@MapActivity,
                    "❌ Error sending emergency SMS: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("MapActivity", "Exception sending emergency SMS", e)
            }
        }
    }
    
    
    // Navigation History Creation Methods
    private fun createNavigationHistoryEntry(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        destinationName: String,
        destinationAddress: String?,
        routeDistance: Double,
        actualDuration: Int,
        estimatedFare: Double,
        actualFare: Double,
        transportMode: String,
        waypointsCount: Int,
        startTime: String,
        endTime: String,
        completionTime: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = com.example.gzingapp.utils.AppSettings(this@MapActivity).getUserId() ?: 0
                
                val request = com.example.gzingapp.data.CreateNavigationHistoryRequest(
                    userId = userId,
                    startLatitude = startLat,
                    startLongitude = startLng,
                    endLatitude = endLat,
                    endLongitude = endLng,
                    destinationName = destinationName,
                    destinationAddress = destinationAddress,
                    routeDistance = routeDistance,
                    estimatedDuration = null,
                    actualDuration = actualDuration,
                    estimatedFare = estimatedFare,
                    actualFare = actualFare,
                    transportMode = transportMode.lowercase(),
                    waypointsCount = waypointsCount,
                    trafficCondition = if (trafficEnabled) "Heavy" else "Light",
                    averageSpeed = calculateAverageSpeed(routeDistance, actualDuration),
                    startTime = startTime,
                    endTime = endTime,
                    completionTime = completionTime
                )
                
                Log.d("MapActivity", "Creating navigation history with transport mode: ${transportMode.lowercase()}")
                
                Log.d("NavigationHistory", "Creating navigation history entry:")
                Log.d("NavigationHistory", "  - User ID: $userId")
                Log.d("NavigationHistory", "  - Destination: $destinationName")
                Log.d("NavigationHistory", "  - Distance: $routeDistance km")
                Log.d("NavigationHistory", "  - Duration: $actualDuration minutes")
                Log.d("NavigationHistory", "  - Estimated Fare: $estimatedFare pesos")
                
                val result = navigationHistoryRepository.createNavigationHistoryStandalone(request)
                
                result.onSuccess { response ->
                    Log.d("NavigationHistory", "Navigation history created successfully: ${response.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MapActivity,
                            "Navigation saved to history!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.onFailure { error ->
                    Log.e("NavigationHistory", "Failed to create navigation history: ${error.message}")
                    error.printStackTrace()
                }
                
        } catch (e: Exception) {
                Log.e("NavigationHistory", "Error creating navigation history", e)
            }
        }
    }
    
    private fun calculateAverageSpeed(distanceKm: Double, durationMinutes: Int): Double {
        return if (durationMinutes > 0) {
            (distanceKm / (durationMinutes / 60.0))
        } else {
            0.0
        }
    }
    
    private fun createNavigationHistoryOnDestinationReached() {
                val currentLoc = currentLocation
                val destLoc = pinnedLocation
                
                if (currentLoc != null && destLoc != null) {
            CoroutineScope(Dispatchers.IO).launch {
                    val distance = haversineMeters(
                        currentLoc.latitude(), currentLoc.longitude(),
                        destLoc.latitude(), destLoc.longitude()
                    ) / 1000.0 // Convert to kilometers
                
                val actualDuration = if (navigationStartTime > 0) {
                    ((System.currentTimeMillis() - navigationStartTime) / 60000).toInt() // Convert to minutes
                } else {
                    0
                }
                
                val estimatedFare = calculateEstimatedFare(distance)
                val actualFare = estimatedFare // For now, use estimated fare as actual fare
                    
                    val destinationName = try {
                        withContext(Dispatchers.Main) {
                        getAddressFromCoordinates(destLoc.latitude(), destLoc.longitude())
                        }
                    } catch (e: Exception) {
                        "Unknown Destination"
                    }
                    
                val currentTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                
                createNavigationHistoryEntry(
                    startLat = currentLoc.latitude(),
                    startLng = currentLoc.longitude(),
                    endLat = destLoc.latitude(),
                    endLng = destLoc.longitude(),
                        destinationName = destinationName,
                        destinationAddress = destinationName,
                        routeDistance = distance,
                    actualDuration = actualDuration,
                    estimatedFare = estimatedFare,
                    actualFare = actualFare,
                    transportMode = selectedTransportMode,
                    waypointsCount = alternativeRoutes.size,
                    startTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(navigationStartTime)),
                    endTime = currentTime,
                    completionTime = currentTime
                )
            }
        }
    }
    
    private fun calculateEstimatedFare(distanceKm: Double): Double {
        return when (selectedTransportMode.lowercase()) {
            "car", "driving" -> {
                // First 5 km is 15 pesos, exceeding kilometers is 5 pesos per km
                val baseFare = 15.0
                val extraKm = if (distanceKm > 5.0) distanceKm - 5.0 else 0.0
                baseFare + (extraKm * 5.0)
            }
            "walk", "walking" -> 0.0 // Walking is free
            "motor", "cycling", "bike" -> {
                // First 5 km is 15 pesos, exceeding kilometers is 5 pesos per km
                val baseFare = 15.0
                val extraKm = if (distanceKm > 5.0) distanceKm - 5.0 else 0.0
                baseFare + (extraKm * 5.0)
            }
            "public_transport", "transit" -> {
                // First 5 km is 15 pesos, exceeding kilometers is 5 pesos per km
                val baseFare = 15.0
                val extraKm = if (distanceKm > 5.0) distanceKm - 5.0 else 0.0
                baseFare + (extraKm * 5.0)
            }
            else -> {
                // First 5 km is 15 pesos, exceeding kilometers is 5 pesos per km
                val baseFare = 15.0
                val extraKm = if (distanceKm > 5.0) distanceKm - 5.0 else 0.0
                baseFare + (extraKm * 5.0)
            }
        }
    }
    
    // Route Creation Methods
    private fun createRouteFromNavigation() {
        val currentLoc = currentLocation
        val destLoc = pinnedLocation
        
        if (currentLoc != null && destLoc != null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                    val distance = haversineMeters(
                        currentLoc.latitude(), currentLoc.longitude(),
                        destLoc.latitude(), destLoc.longitude()
                    ) / 1000.0 // Convert to kilometers
                    
                    val estimatedFare = calculateEstimatedFare(distance)
                    
                    // Get destination name (coordinates format)
                    val destinationName = String.format("%.6f, %.6f", destLoc.latitude(), destLoc.longitude())
                    
                    // Create route name
                    val routeName = "Route to $destinationName"
                    val routeDescription = "Navigation route from current location to $destinationName"
                    
                    // Create map details with only 1 waypoint (destination)
                    val mapDetails = createMapDetailsWithSingleWaypoint(currentLoc, destLoc, destinationName)
                    
                    val result = routesRepository.createRoute(
                        name = routeName,
                        description = routeDescription,
                        pinCount = 1, // Only 1 waypoint (destination)
                        kilometer = distance,
                        estimatedTotalFare = estimatedFare,
                        mapDetails = mapDetails,
                        status = "active"
                    )
                    
                    result.onSuccess { route ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MapActivity, "Route created successfully: ${route.name}", Toast.LENGTH_LONG).show()
                            Log.d("RouteCreation", "Route created successfully with ID: ${route.id}")
                        }
                    }.onFailure { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MapActivity, "Failed to create route: ${error.message}", Toast.LENGTH_LONG).show()
                            Log.e("RouteCreation", "Failed to create route", error)
                        }
                    }
                    
            } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapActivity, "Error creating route: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("RouteCreation", "Error creating route", e)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please set both current location and destination to create a route", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun createMapDetailsWithSingleWaypoint(
        startPoint: Point,
        endPoint: Point,
        destinationName: String
    ): com.example.gzingapp.data.MapDetails {
        // Create a single pin for the destination
        val destinationPin = com.example.gzingapp.data.Pin(
            id = "destination-pin",
            number = 1,
            name = destinationName,
            coordinates = listOf(endPoint.longitude(), endPoint.latitude()),
            lng = endPoint.longitude(),
            lat = endPoint.latitude(),
            addedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date()),
            placeName = destinationName,
            address = destinationName
        )
        
        // Create center point (midpoint between start and end)
        val centerLng = (startPoint.longitude() + endPoint.longitude()) / 2.0
        val centerLat = (startPoint.latitude() + endPoint.latitude()) / 2.0
        
        val center = com.example.gzingapp.data.Center(
            lng = centerLng,
            lat = centerLat
        )
        
        // Create route line from start to end
        val routeLine = com.example.gzingapp.data.RouteLine(
            type = "Feature",
            properties = emptyList(),
            geometry = com.example.gzingapp.data.RouteGeometry(
                type = "Feature",
                properties = emptyList(),
                geometry = com.example.gzingapp.data.GeometryData(
                    type = "LineString",
                    coordinates = listOf(
                        listOf(startPoint.longitude(), startPoint.latitude()),
                        listOf(endPoint.longitude(), endPoint.latitude())
                    )
                )
            )
        )
        
        return com.example.gzingapp.data.MapDetails(
            pins = listOf(destinationPin),
            center = center,
            zoom = 13.0,
            fifoOrder = listOf("destination-pin"),
            routeLine = routeLine
        )
    }
    
    // Show save route dialog after successful navigation
    private fun showSaveRouteDialog() {
        val currentLoc = currentLocation
        val destLoc = pinnedLocation
        
        if (currentLoc == null || destLoc == null) {
            Toast.makeText(this, "Cannot save route: Location data not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_save_route, null)
        val etRouteName = dialogView.findViewById<EditText>(R.id.etRouteName)
        val etRouteDescription = dialogView.findViewById<EditText>(R.id.etRouteDescription)
        val cbIsFavorite = dialogView.findViewById<CheckBox>(R.id.cbIsFavorite)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        
        // Set default route name
        val destinationName = tvPinnedLocation.text?.toString() ?: "Unknown Destination"
        etRouteName.setText("Route to $destinationName")
        
        // Create and show dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Save Navigation Route")
            .setMessage("Would you like to save this route for future use?")
            .setView(dialogView)
            .setPositiveButton("Save Route") { _, _ ->
                val routeName = etRouteName.text.toString().trim()
                val routeDescription = etRouteDescription.text.toString().trim()
                val isFavorite = cbIsFavorite.isChecked
                val userRating = ratingBar.rating.toInt()
                
                Log.d("SaveRoute", "Dialog form data captured:")
                Log.d("SaveRoute", "  - Route Name: '$routeName'")
                Log.d("SaveRoute", "  - Route Description: '$routeDescription'")
                Log.d("SaveRoute", "  - Is Favorite: $isFavorite")
                Log.d("SaveRoute", "  - User Rating: $userRating")
                
                saveNavigationRoute(
                    routeName = routeName,
                    routeDescription = routeDescription,
                    isFavorite = isFavorite,
                    userRating = userRating
                )
            }
            .setNegativeButton("Don't Save") { dialog, _ ->
                Log.d("SaveRoute", "User chose not to save route")
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    // Save the navigation route to database
    private fun saveNavigationRoute(
        routeName: String,
        routeDescription: String,
        isFavorite: Boolean,
        userRating: Int
    ) {
        Log.d("SaveRoute", "=== SAVE ROUTE DIALOG SUBMITTED ===")
        Log.d("SaveRoute", "Route Name: '$routeName'")
        Log.d("SaveRoute", "Route Description: '$routeDescription'")
        Log.d("SaveRoute", "Is Favorite: $isFavorite")
        Log.d("SaveRoute", "User Rating: $userRating")
        
        val currentLoc = currentLocation
        val destLoc = pinnedLocation
        
        Log.d("SaveRoute", "Current Location: $currentLoc")
        Log.d("SaveRoute", "Destination Location: $destLoc")
        
        if (currentLoc == null || destLoc == null) {
            Log.e("SaveRoute", "Location data not available - currentLoc: $currentLoc, destLoc: $destLoc")
            Toast.makeText(this, "Cannot save route: Location data not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (routeName.isBlank()) {
            Log.e("SaveRoute", "Route name is blank")
            Toast.makeText(this, "Please enter a route name", Toast.LENGTH_SHORT).show()
            return
        }
        
        Log.d("SaveRoute", "Starting route save process...")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val distance = haversineMeters(
                    currentLoc.latitude(), currentLoc.longitude(),
                    destLoc.latitude(), destLoc.longitude()
                ) / 1000.0 // Convert to kilometers
                
                val estimatedDuration = estimateEtaMinutes(distance * 1000, selectedTransportMode).toInt()
                val averageSpeed = calculateAverageSpeed(distance, estimatedDuration)
                val estimatedFare = calculateEstimatedFare(distance)
                
                Log.d("SaveRoute", "Calculated values:")
                Log.d("SaveRoute", "  - Distance: $distance km")
                Log.d("SaveRoute", "  - Estimated Duration: $estimatedDuration minutes")
                Log.d("SaveRoute", "  - Average Speed: $averageSpeed km/h")
                Log.d("SaveRoute", "  - Estimated Fare: $estimatedFare")
                Log.d("SaveRoute", "  - Transport Mode: $selectedTransportMode")
                
                // Prepare route coordinates (simplified - just start and end points)
                val routeCoordinates = listOf(
                    mapOf("lat" to currentLoc.latitude(), "lng" to currentLoc.longitude()),
                    mapOf("lat" to destLoc.latitude(), "lng" to destLoc.longitude())
                )
                
                val userId = com.example.gzingapp.utils.AppSettings(this@MapActivity).getUserId() ?: 0
                Log.d("SaveRoute", "User ID: $userId")
                
                val request = CreateNavigationRouteRequest(
                    userId = userId,
                    routeName = routeName,
                    routeDescription = routeDescription.ifBlank { null },
                    startLatitude = currentLoc.latitude(),
                    startLongitude = currentLoc.longitude(),
                    endLatitude = destLoc.latitude(),
                    endLongitude = destLoc.longitude(),
                    destinationName = tvPinnedLocation.text?.toString() ?: "Unknown Destination",
                    destinationAddress = tvLocationAddress?.text?.toString(),
                    routeDistance = distance,
                    estimatedDuration = estimatedDuration,
                    estimatedFare = estimatedFare,
                    transportMode = selectedTransportMode.lowercase(),
                    routeQuality = when (userRating) {
                        5 -> "excellent"
                        4 -> "good"
                        3 -> "fair"
                        else -> "poor"
                    },
                    trafficCondition = if (trafficEnabled) "Heavy" else "Light",
                    averageSpeed = averageSpeed,
                    waypointsCount = alternativeRoutes.size,
                    routeCoordinates = routeCoordinates,
                    isFavorite = if (isFavorite) 1 else 0,
                    isPublic = 0 // Default to private
                )
                
                Log.d("SaveRoute", "Request object created successfully")
                Log.d("SaveRoute", "Request details:")
                Log.d("SaveRoute", "  - User ID: ${request.userId}")
                Log.d("SaveRoute", "  - Route Name: ${request.routeName}")
                Log.d("SaveRoute", "  - Start: ${request.startLatitude}, ${request.startLongitude}")
                Log.d("SaveRoute", "  - End: ${request.endLatitude}, ${request.endLongitude}")
                Log.d("SaveRoute", "  - Destination: ${request.destinationName}")
                Log.d("SaveRoute", "  - Distance: ${request.routeDistance}")
                Log.d("SaveRoute", "  - Transport Mode: ${request.transportMode}")
                Log.d("SaveRoute", "  - Is Favorite: ${request.isFavorite}")
                
                Log.d("SaveRoute", "Calling API to create navigation route...")
                val result = navigationRouteRepository.createNavigationRoute(request)
                
                result.onSuccess { response ->
                    Log.d("SaveRoute", "✅ Route saved successfully!")
                    Log.d("SaveRoute", "Response: ${response.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MapActivity,
                            "Route '$routeName' saved successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }.onFailure { error ->
                    Log.e("SaveRoute", "❌ Failed to save route")
                    Log.e("SaveRoute", "Error: ${error.message}")
                    Log.e("SaveRoute", "Error type: ${error.javaClass.simpleName}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MapActivity,
                            "Failed to save route: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                
            } catch (e: Exception) {
                Log.e("SaveRoute", "❌ Exception during route save", e)
                Log.e("SaveRoute", "Exception message: ${e.message}")
                Log.e("SaveRoute", "Exception stack trace:", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MapActivity,
                        "Error saving route: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    // ===== FAILURE RATE MONITORING METHODS =====
    
    private fun resetFailureRateMonitoring() {
        failureRate = 0
        lastLocationOnRoute = true
        consecutiveOffRouteCount = 0
        emergencySmsSent = false
        
        // Reset distance tracking
        initialDistanceToDestination = 0.0
        distanceIncreaseStartTime = 0
        
        // Only update failure rate UI if SOS warning is disabled
        val appSettings = com.example.gzingapp.utils.AppSettings(this)
        if (!appSettings.isSosWarningEnabled()) {
            updateFailureRateUI()
        }
        
        Log.d("MapActivity", "Failure rate monitoring reset for new navigation session")
    }
    
    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            smsPermissionRequest.launch(Manifest.permission.SEND_SMS)
        }
    }
    
    private fun checkRouteDeviation(userPoint: Point) {
        if (routeCoordinates == null || routeCoordinates!!.isEmpty()) {
            Log.d("MapActivity", "No route coordinates available for deviation check")
            return
        }
        
        try {
            // Calculate distance from user location to the nearest point on the route
            var minDistance = Double.MAX_VALUE
            var nearestRoutePoint: Point? = null
            
            // Find the nearest point on the route polyline
            for (routePoint in routeCoordinates!!) {
                val distance = haversineMeters(
                    userPoint.latitude(), userPoint.longitude(),
                    routePoint.latitude(), routePoint.longitude()
                )
                if (distance < minDistance) {
                    minDistance = distance
                    nearestRoutePoint = routePoint
                }
            }
            
            val distanceMeters = minDistance
            Log.d("MapActivity", "Distance to route: ${String.format("%.2f", distanceMeters)}m, threshold: ${DEVIATION_THRESHOLD_METERS}m")
            
            val isOnRoute = distanceMeters <= DEVIATION_THRESHOLD_METERS
            
            if (!isOnRoute) {
                consecutiveOffRouteCount++
                
                // Track when off-route started
                if (offRouteStartTime == 0L) {
                    offRouteStartTime = System.currentTimeMillis()
                    Log.d("MapActivity", "User went off-route - starting timer")
                }
                
                // Check if off-route for confirmation period
                val offRouteTimeSeconds = (System.currentTimeMillis() - offRouteStartTime) / 1000
                Log.d("MapActivity", "Off-route time: ${offRouteTimeSeconds}s, threshold: ${OFF_ROUTE_CONFIRMATION_THRESHOLD}s")
                
                if (offRouteTimeSeconds >= OFF_ROUTE_CONFIRMATION_THRESHOLD && !offRouteWarningTriggered) {
                    // User is definitely off-route, trigger SOS warning
                    Log.w("MapActivity", "🚨 SUSTAINED OFF-ROUTE DETECTED - Triggering SOS warning")
                    triggerOffRouteSosWarning(distanceMeters)
                }
                
                // Build/extend off-route line for visualization
                if (offRouteLine == null) offRouteLine = mutableListOf()
                (offRouteLine as MutableList<Point>).add(userPoint)
                
                // Update failure rate only if SOS warning is disabled
                val appSettings = com.example.gzingapp.utils.AppSettings(this)
                if (!appSettings.isSosWarningEnabled()) {
                    if (lastLocationOnRoute || consecutiveOffRouteCount >= 3) {
                        increaseFailureRate(FAILURE_RATE_INCREMENT, "Route deviation detected: ${String.format("%.1f", distanceMeters)}m")
                    }
                }
                
                Log.d("MapActivity", "User off route - consecutive count: $consecutiveOffRouteCount")
                redrawAllLayers()
            } else {
                // User is back on route; apply debounce before cancelling to prevent dialog flash
                if (onRouteSinceTime == 0L) {
                    onRouteSinceTime = System.currentTimeMillis()
                }
                val onRouteSeconds = (System.currentTimeMillis() - onRouteSinceTime) / 1000
                if ((offRouteStartTime > 0 || offRouteWarningTriggered) && onRouteSeconds >= ON_ROUTE_DEBOUNCE_SECONDS) {
                    Log.d("MapActivity", "User back on route for ${onRouteSeconds}s (>= ${ON_ROUTE_DEBOUNCE_SECONDS}s) - cancelling SOS warning")
                    cancelOffRouteSosWarning()
                }
                offRouteStartTime = 0
                
                // Reset consecutive off-route count when back on route
                if (consecutiveOffRouteCount > 0) {
                    Log.d("MapActivity", "User back on route - resetting consecutive count")
                    consecutiveOffRouteCount = 0
                }
                // Clear off-route breadcrumb when back on route to reduce clutter
                if (offRouteLine != null) {
                    offRouteLine = null
                    redrawAllLayers()
                }
            }
            
            lastLocationOnRoute = isOnRoute
            
        } catch (e: Exception) {
            Log.e("MapActivity", "Error checking route deviation", e)
        }
    }

    private fun checkDistanceIncrease(userPoint: Point) {
        if (!isNavigating || pinnedLocation == null) {
            // Reset timer if not navigating
            if (distanceIncreaseStartTime > 0) {
                distanceIncreaseStartTime = 0
                Log.d("MapActivity", "Distance increase timer reset - not navigating")
            }
            return
        }

        // Skip if SOS warning is disabled in settings
        val appSettings = com.example.gzingapp.utils.AppSettings(this)
        if (!appSettings.isSosWarningEnabled()) {
            return
        }

        // Skip if SOS already triggered
        if (offRouteWarningTriggered) {
            return
        }

        // Calculate current distance to destination
        val currentDistanceToDestination = haversineMeters(
            userPoint.latitude(), userPoint.longitude(),
            pinnedLocation!!.latitude(), pinnedLocation!!.longitude()
        )

        // Calculate how much the distance has increased from initial
        val distanceIncrease = currentDistanceToDestination - initialDistanceToDestination

        Log.d("MapActivity", "Distance check - Initial: ${String.format("%.2f", initialDistanceToDestination)}m, Current: ${String.format("%.2f", currentDistanceToDestination)}m, Increase: ${String.format("%.2f", distanceIncrease)}m")

        // Check if distance increase exceeds threshold
        if (distanceIncrease >= DISTANCE_INCREASE_THRESHOLD_METERS) {
            // Start or continue timing the sustained increase
            if (distanceIncreaseStartTime == 0L) {
                distanceIncreaseStartTime = System.currentTimeMillis()
                Log.d("MapActivity", "Distance increase detected (${String.format("%.2f", distanceIncrease)}m) - starting ${DISTANCE_INCREASE_CONFIRMATION_SECONDS}s confirmation timer")
            }

            // Check if the increase has been sustained for the required duration
            val sustainedTimeSeconds = (System.currentTimeMillis() - distanceIncreaseStartTime) / 1000
            Log.d("MapActivity", "Distance increase sustained for ${sustainedTimeSeconds}s (threshold: ${DISTANCE_INCREASE_CONFIRMATION_SECONDS}s)")

            if (sustainedTimeSeconds >= DISTANCE_INCREASE_CONFIRMATION_SECONDS) {
                Log.w("MapActivity", "🚨 DISTANCE INCREASE THRESHOLD EXCEEDED FOR ${DISTANCE_INCREASE_CONFIRMATION_SECONDS}s - Triggering SOS warning")
                Log.w("MapActivity", "Distance increased by ${String.format("%.2f", distanceIncrease)}m (threshold: ${DISTANCE_INCREASE_THRESHOLD_METERS}m)")
                triggerOffRouteSosWarning(distanceIncrease)
            }
        } else {
            // Distance increase dropped below threshold - reset timer
            if (distanceIncreaseStartTime > 0) {
                Log.d("MapActivity", "Distance increase dropped below threshold (${String.format("%.2f", distanceIncrease)}m) - resetting timer")
                distanceIncreaseStartTime = 0
            }
        }
    }

    /**
     * Checks if user has deviated 500+ meters from the initial origin-to-destination path segment.
     * This is different from route polyline deviation - it checks perpendicular distance from
     * the straight line connecting the starting point to the destination.
     */
    private fun checkOriginSegmentDeviation(userPoint: Point) {
        if (!isNavigating || pinnedLocation == null || currentLocation == null) {
            return
        }

        // Skip if SOS warning is disabled in settings
        val appSettings = com.example.gzingapp.utils.AppSettings(this)
        if (!appSettings.isSosWarningEnabled()) {
            return
        }

        // Skip if SOS already triggered
        if (offRouteWarningTriggered) {
            return
        }

        // Calculate perpendicular distance from user to the origin-destination line segment
        val perpendicularDistance = calculatePerpendicularDistanceToSegment(
            userPoint,
            currentLocation!!,  // Origin (start point)
            pinnedLocation!!    // Destination (end point)
        )

        Log.d("MapActivity", "Origin segment deviation check - Perpendicular distance: ${String.format("%.2f", perpendicularDistance)}m (threshold: 500m)")

        // Check if deviation exceeds 500 meters
        if (perpendicularDistance >= 500.0) {
            Log.w("MapActivity", "🚨 ORIGIN SEGMENT DEVIATION DETECTED - User is ${String.format("%.2f", perpendicularDistance)}m away from origin-destination path")
            triggerOffRouteSosWarning(perpendicularDistance)
        }
    }

    /**
     * Calculates the perpendicular distance from a point to a line segment.
     * Uses the cross product method for accurate geometric calculation.
     *
     * @param point The user's current location
     * @param segmentStart The origin point (where navigation started)
     * @param segmentEnd The destination point
     * @return Distance in meters from the point to the line segment
     */
    private fun calculatePerpendicularDistanceToSegment(
        point: Point,
        segmentStart: Point,
        segmentEnd: Point
    ): Double {
        val R = 6371000.0 // Earth's radius in meters

        // Convert to radians
        val lat1 = Math.toRadians(segmentStart.latitude())
        val lon1 = Math.toRadians(segmentStart.longitude())
        val lat2 = Math.toRadians(segmentEnd.latitude())
        val lon2 = Math.toRadians(segmentEnd.longitude())
        val latP = Math.toRadians(point.latitude())
        val lonP = Math.toRadians(point.longitude())


        val x1 = R * Math.cos(lat1) * Math.cos(lon1)
        val y1 = R * Math.cos(lat1) * Math.sin(lon1)
        val z1 = R * Math.sin(lat1)

        val x2 = R * Math.cos(lat2) * Math.cos(lon2)
        val y2 = R * Math.cos(lat2) * Math.sin(lon2)
        val z2 = R * Math.sin(lat2)

        val xP = R * Math.cos(latP) * Math.cos(lonP)
        val yP = R * Math.cos(latP) * Math.sin(lonP)
        val zP = R * Math.sin(latP)

        val dx = x2 - x1
        val dy = y2 - y1
        val dz = z2 - z1

        val dpx = xP - x1
        val dpy = yP - y1
        val dpz = zP - z1

        val segmentLengthSquared = dx * dx + dy * dy + dz * dz

        if (segmentLengthSquared == 0.0) {
            return haversineMeters(
                point.latitude(), point.longitude(),
                segmentStart.latitude(), segmentStart.longitude()
            )
        }


        val t = ((dpx * dx + dpy * dy + dpz * dz) / segmentLengthSquared).coerceIn(0.0, 1.0)

        val closestX = x1 + t * dx
        val closestY = y1 + t * dy
        val closestZ = z1 + t * dz


        val distance = Math.sqrt(
            (xP - closestX) * (xP - closestX) +
                    (yP - closestY) * (yP - closestY) +
                    (zP - closestZ) * (zP - closestZ)
        )

        return distance
    }
    
    private fun increaseFailureRate(increment: Int, reason: String) {
        if (emergencySmsSent) {
            Log.d("MapActivity", "Emergency SMS already sent - skipping failure rate increase")
            return
        }
        
        val oldFailureRate = failureRate
        failureRate = (failureRate + increment).coerceAtMost(100)
        
        Log.d("MapActivity", "Failure rate increased: $oldFailureRate% -> $failureRate% (Reason: $reason)")
        
        updateFailureRateUI()
        
        // Check if failure rate reached 100%
        if (failureRate >= 100 && !emergencySmsSent) {
            triggerEmergencyAlert()
        }
    }
    
    private fun updateFailureRateUI() {
        runOnUiThread {
            // Check if SOS warning is enabled - if so, hide failure rate monitoring
            val appSettings = com.example.gzingapp.utils.AppSettings(this)
            if (appSettings.isSosWarningEnabled()) {
                rowFailureRate.visibility = View.GONE
                return@runOnUiThread
            }
            
            if (isNavigating) {
                rowFailureRate.visibility = View.VISIBLE
                
                val statusText = when {
                    failureRate >= 100 -> "EMERGENCY - Off Route"
                    failureRate >= 70 -> "Critical Deviation"
                    failureRate >= 40 -> "Route Deviation"
                    failureRate >= 20 -> "Minor Deviation"
                    else -> "On Route"
                }
                
                tvFailureRate.text = statusText
                
                // Update dot color based on failure rate
                val color = when {
                    failureRate >= 100 -> android.graphics.Color.parseColor("#E74C3C") // Red
                    failureRate >= 70 -> android.graphics.Color.parseColor("#F39C12") // Orange
                    failureRate >= 40 -> android.graphics.Color.parseColor("#F1C40F") // Yellow
                    else -> android.graphics.Color.parseColor("#27AE60") // Green
                }
                
                try {
                    (failureRateDot.background as? android.graphics.drawable.GradientDrawable)?.setColor(color)
                } catch (_: Exception) { }
                
            } else {
                rowFailureRate.visibility = View.GONE
            }
        }
    }
    
    private fun triggerEmergencyAlert() {
        if (emergencySmsSent) return
        
        emergencySmsSent = true
        Log.e("MapActivity", "🚨 EMERGENCY ALERT TRIGGERED - Failure rate reached 100%!")
        
        runOnUiThread {
            // Show emergency alert dialog
            AlertDialog.Builder(this)
                .setTitle("🚨 Emergency Alert")
                .setMessage("Navigation failure detected! Emergency SMS will be sent to your emergency contacts.")
                .setPositiveButton("OK") { _, _ ->
                    sendEmergencySmsAlert()
                }
                .setCancelable(false)
                .show()
        }
    }
    
    private fun sendEmergencySmsAlert() {
        if (currentLocation == null) {
            Log.e("MapActivity", "Cannot send emergency SMS - no current location")
            Toast.makeText(this, "Cannot send emergency SMS - location not available", Toast.LENGTH_LONG).show()
            return
        }
        
        // Check SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("MapActivity", "SMS permission not granted")
            Toast.makeText(this, "SMS permission required for emergency alerts", Toast.LENGTH_LONG).show()
            return
        }
        
        try {
            // Get emergency contacts
            val emergencySMSService = com.example.gzingapp.service.EmergencySMSService(this)
            val contacts = emergencySMSService.getEmergencyContacts()
            
            if (contacts.isEmpty()) {
                Log.e("MapActivity", "No emergency contacts configured")
                Toast.makeText(this, "No emergency contacts configured for emergency alerts", Toast.LENGTH_LONG).show()
                return
            }
            
            val lat = currentLocation!!.latitude()
            val lng = currentLocation!!.longitude()
            val message = "⚠️ Emergency Alert: Navigation failure detected. User may be off-route. Location: $lat, $lng. Please check on user immediately."
            
            Log.d("MapActivity", "Sending emergency SMS to ${contacts.size} contacts")
            Log.d("MapActivity", "Message: $message")
            
            // Send SMS to all emergency contacts
            val smsManager = SmsManager.getDefault()
            for (phoneNumber in contacts) {
                try {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                    Log.d("MapActivity", "Emergency SMS sent to: $phoneNumber")
                } catch (e: Exception) {
                    Log.e("MapActivity", "Failed to send SMS to $phoneNumber", e)
                }
            }
            
            Toast.makeText(this, "🚨 Emergency SMS sent to all contacts!", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Log.e("MapActivity", "Error sending emergency SMS", e)
            Toast.makeText(this, "Failed to send emergency SMS: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    // ===== OFF-ROUTE SOS WARNING SYSTEM =====
    
    private fun triggerOffRouteSosWarning(deviationDistance: Double) {
        if (offRouteWarningTriggered) return
        if (!isNavigating) return
        
        // Check if user is already marked as safe
        if (isSafe) {
            Log.d("MapActivity", "User marked as safe - skipping SOS warning")
            return
        }
        
        // Check if we're in cooldown period
        val currentTime = System.currentTimeMillis()
        if (cooldownEndTime > currentTime) {
            val remainingCooldown = (cooldownEndTime - currentTime) / 1000
            Log.d("MapActivity", "SOS warning in cooldown - ${remainingCooldown}s remaining")
            return
        }
        
        // Check if SOS warning is enabled in settings
        val appSettings = com.example.gzingapp.utils.AppSettings(this)
        if (!appSettings.isSosWarningEnabled()) {
            Log.d("MapActivity", "SOS warning disabled in settings - skipping")
            return
        }
        
        // Set state variables
        offRoute = true
        offRouteWarningTriggered = true
        
        Log.w("MapActivity", "🚨 OFF-ROUTE SOS WARNING TRIGGERED - Deviation: ${String.format("%.1f", deviationDistance)}m")
        Log.d("MapActivity", "State: offRoute=$offRoute, isSafe=$isSafe")
        
        runOnUiThread {
            // Fixed 5-second countdown for simple behavior
            val countdownSeconds = 5
            
            sosWarningDialog = com.example.gzingapp.ui.SosWarningDialog(
                context = this,
                countdownSeconds = countdownSeconds,
                onCancelled = {
                    handleSosWarningCancelled()
                },
                onSendNow = {
                    handleSosSendNow()
                },
                onAutoSend = {
                    handleSosAutoSend()
                },
                currentLocation = currentLocation,
                destinationLocation = pinnedLocation,
                deviationDistance = deviationDistance
            )
            
            sosWarningDialog?.show()
            sosWarningDialog?.startCountdown()
        }
    }
    
    private fun cancelOffRouteSosWarning() {
        if (!offRouteWarningTriggered) return
        
        Log.d("MapActivity", "User returned to route - but keeping SOS warning active until user action")
        
        // Don't dismiss the dialog automatically - let user decide
        // Just update the offRoute state
        offRoute = false
        
        // The dialog will remain visible until user clicks "I'm Safe" or "Send Now"
    }
    
    private fun handleSosWarningCancelled() {
        Log.d("MapActivity", "User clicked 'I'm Safe' - cancelling SOS warning")
        
        // Dismiss the SOS warning dialog
        sosWarningDialog?.dismiss()
        sosWarningDialog = null
        
        // Set user as safe and start cooldown
        isSafe = true
        offRoute = false
        offRouteWarningTriggered = false
        
        // Start 10-second cooldown
        cooldownEndTime = System.currentTimeMillis() + (COOLDOWN_DURATION_SECONDS * 1000)
        
        // Reset other state
        offRouteStartTime = 0
        distanceIncreaseStartTime = 0
        consecutiveOffRouteCount = 0
        
        Log.d("MapActivity", "State updated: offRoute=$offRoute, isSafe=$isSafe, cooldown until ${cooldownEndTime}")
        
        Toast.makeText(this, "✅ Safety confirmed. Stay safe!", Toast.LENGTH_SHORT).show()
        
        // Reset isSafe after cooldown period
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            isSafe = false
            Log.d("MapActivity", "Cooldown ended - isSafe reset to false")
        }, COOLDOWN_DURATION_SECONDS * 1000L)
    }
    
    
    
    
    
    private fun handleSosSendNow() {
        Log.w("MapActivity", "User manually triggered SOS send")
        sosWarningDialog?.dismiss()
        sosWarningDialog = null
        
        sendOffRouteEmergencySms()
        
        // Reset SOS warning state after sending SMS
        offRouteWarningTriggered = false
        offRoute = false
        isSafe = false
        offRouteStartTime = 0
        distanceIncreaseStartTime = 0
        consecutiveOffRouteCount = 0
        lastSosSentTime = System.currentTimeMillis()
        
        // Start cooldown period
        cooldownEndTime = System.currentTimeMillis() + (COOLDOWN_DURATION_SECONDS * 1000)
        
        Log.d("MapActivity", "SOS sent - State reset, cooldown started")
    }
    
    private fun handleSosAutoSend() {
        Log.e("MapActivity", "🚨 SOS AUTO-SEND TRIGGERED - Countdown reached zero")
        sosWarningDialog?.dismiss()
        sosWarningDialog = null
        
        sendOffRouteEmergencySms()
        
        // Reset SOS warning state after sending SMS
        offRouteWarningTriggered = false
        offRoute = false
        isSafe = false
        offRouteStartTime = 0
        distanceIncreaseStartTime = 0
        consecutiveOffRouteCount = 0
        lastSosSentTime = System.currentTimeMillis()
        
        // Start cooldown period
        cooldownEndTime = System.currentTimeMillis() + (COOLDOWN_DURATION_SECONDS * 1000)
        
        Log.d("MapActivity", "SOS auto-sent - State reset, cooldown started")
    }

// Replace the sendOffRouteEmergencySms() method in MapActivity with this:

    private fun sendOffRouteEmergencySms() {
        if (currentLocation == null) {
            Log.e("MapActivity", "Cannot send off-route SMS - no location")
            Toast.makeText(this, "Location not available for emergency SMS", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress message
        Toast.makeText(this, "🚨 Sending emergency SMS...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val emergencySMSService = com.example.gzingapp.service.EmergencySMSService(this@MapActivity)

                // Get emergency contacts
                val appSettings = com.example.gzingapp.utils.AppSettings(this@MapActivity)
                val userId = appSettings.getUserId() ?: 0

                Log.d("MapActivity", "Fetching emergency contacts for user: $userId")

                // Fetch emergency contacts from API
                val contactsResult = withContext(Dispatchers.IO) {
                    try {
                        val response = com.example.gzingapp.network.RetrofitClient.apiService
                            .getUserEmergencyContacts(userId)

                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            if (body.success) {
                                body.data?.contacts ?: emptyList()
                            } else {
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    } catch (e: Exception) {
                        Log.e("MapActivity", "Error fetching emergency contacts", e)
                        emptyList()
                    }
                }

                if (contactsResult.isEmpty()) {
                    Toast.makeText(
                        this@MapActivity,
                        "❌ No emergency contacts found. Please add contacts in settings.",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("MapActivity", "No emergency contacts available")
                    return@launch
                }

                // Extract phone numbers from contacts
                val phoneNumbers = contactsResult.mapNotNull { it.phoneNumber }

                if (phoneNumbers.isEmpty()) {
                    Toast.makeText(
                        this@MapActivity,
                        "❌ No valid phone numbers in emergency contacts",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                Log.d("MapActivity", "Sending emergency SMS to ${phoneNumbers.size} contacts: $phoneNumbers")

                val lat = currentLocation!!.latitude()
                val lng = currentLocation!!.longitude()
                val destinationName = tvPinnedLocation.text?.toString() ?: "Unknown Destination"

                // Create custom message for off-route emergency
                val customMessage = "User went OFF-ROUTE during navigation to $destinationName. Please check on user immediately!"

                Log.d("MapActivity", "Preparing emergency SMS request:")
                Log.d("MapActivity", "  - User ID: $userId")
                Log.d("MapActivity", "  - Latitude: $lat")
                Log.d("MapActivity", "  - Longitude: $lng")
                Log.d("MapActivity", "  - Emergency Type: off_route")
                Log.d("MapActivity", "  - Custom Message: $customMessage")
                Log.d("MapActivity", "  - Contacts: $phoneNumbers")

                // Send emergency SMS using the service - matching SosSmsService pattern
                val result = emergencySMSService.sendEmergencySMS(
                    latitude = lat,
                    longitude = lng,
                    emergencyType = "off_route",
                    customMessage = customMessage,
                    contacts = phoneNumbers
                )

                result.onSuccess { response ->
                    val successCount = response.data?.successfulSends ?: 0
                    val totalCount = response.data?.totalContacts ?: 0

                    Log.d("MapActivity", "Emergency SMS API response:")
                    Log.d("MapActivity", "  - Success: ${response.success}")
                    Log.d("MapActivity", "  - Message: ${response.message}")
                    Log.d("MapActivity", "  - Successful sends: $successCount")
                    Log.d("MapActivity", "  - Failed sends: ${response.data?.failedSends}")
                    Log.d("MapActivity", "  - Total contacts: $totalCount")

                    if (successCount > 0) {
                        Toast.makeText(
                            this@MapActivity,
                            "🚨 Off-route emergency SMS sent to $successCount out of $totalCount contacts!",
                            Toast.LENGTH_LONG
                        ).show()

                        // Show notification
                        showOffRouteEmergencyNotification()

                        // Log results for debugging
                        response.data?.results?.forEach { result ->
                            Log.d("MapActivity", "SMS to ${result.phone}: ${if (result.success) "✅ Sent" else "❌ Failed"} - ${result.message}")
                        }
                    } else {
                        Toast.makeText(
                            this@MapActivity,
                            "❌ Failed to send emergency SMS to any contacts. Please check your network connection.",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("MapActivity", "Failed to send SMS to any contacts")
                    }
                }.onFailure { error ->
                    Toast.makeText(
                        this@MapActivity,
                        "❌ Emergency SMS failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("MapActivity", "Emergency SMS API error", error)
                }

            } catch (e: Exception) {
                Log.e("MapActivity", "Exception sending off-route emergency SMS", e)
                Toast.makeText(
                    this@MapActivity,
                    "❌ Error sending emergency SMS: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Also add this helper method to show notification after successful SMS send
    private fun showOffRouteEmergencyNotification() {
        val channelId = "gzing_offroute_emergency"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Off-Route Emergency",
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.description = "Alerts for off-route emergencies"
                nm.createNotificationChannel(channel)
            }
        }

        val intent = Intent(this, MapActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            3000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("🚨 Emergency SMS Sent")
            .setContentText("Off-route alert sent to emergency contacts")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Emergency SMS sent to your contacts. They have been notified of your off-route situation and current location."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(3001, notif)
    }
}
