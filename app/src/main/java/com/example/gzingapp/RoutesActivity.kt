package com.example.gzingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
// use already imported android.view.View further down
import android.widget.ArrayAdapter
// use already imported android.widget.TextView further down
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.gzingapp.data.Route
import com.example.gzingapp.data.RoutesResponse
import com.example.gzingapp.data.RoutesApiResponse
import com.example.gzingapp.data.Pin
import com.example.gzingapp.network.ApiService
import com.example.gzingapp.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.geojson.Point
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.image.image
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.LinearLayout
// remove duplicate ArrayAdapter import
import android.Manifest
import android.content.pm.PackageManager
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.ScreenCoordinate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoutesActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var routeDropdown: MaterialAutoCompleteTextView
    private lateinit var btnEnterRoute: MaterialButton
    private lateinit var mapView: MapView
    private lateinit var loadingIndicator: View
    
    private lateinit var apiService: ApiService
    private var selectedRoute: Route? = null
    private val routes = mutableListOf<Route>()
    
    // Closest waypoint card views
    private var cardContainer: android.view.ViewGroup? = null
    // Removed location info overlay for RoutesActivity per request
    
    // Location/geofence
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient
    private lateinit var geofencingClient: com.google.android.gms.location.GeofencingClient
    
    // Tooltip related
    private var currentTooltip: View? = null
    
    // Map annotation managers (simplified for now)
    // private var pointAnnotationManager: PointAnnotationManager? = null
    // private var polylineAnnotationManager: PolylineAnnotationManager? = null
    
    companion object {
        private const val TAG = "RoutesActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routes)
        
        initializeViews()
        setupToolbar()
        setupApiService()
        setupClickListeners()
        loadRoutes()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        routeDropdown = findViewById(R.id.routeDropdown)
        btnEnterRoute = findViewById(R.id.btnEnterRoute)
        mapView = findViewById(R.id.mapView)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        
        // Init location and geofence helpers
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = com.google.android.gms.location.LocationServices.getGeofencingClient(this)
        
        // Removed overlay card inflation per request
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Routes"
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupApiService() {
        apiService = RetrofitClient.apiService
    }
    
    private fun setupClickListeners() {
        btnEnterRoute.setOnClickListener {
            selectedRoute?.let { route ->
                val pins = route.mapDetails?.pins
                if (pins.isNullOrEmpty()) {
                    showError("No pins available for this route")
                    return@setOnClickListener
                }
                val names = pins.map { it.name }.toTypedArray()
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Choose destination")
                    .setItems(names) { _, which ->
                        val dest = pins[which]
                        val firstPin = pins.first()
                        val intent = Intent(this, RoutesMapsActivity::class.java).apply {
                            // Pass route information
                            putExtra("route_id", route.id)
                            putExtra("route_name", route.name)
                            putExtra("route_description", route.description ?: "")
                            putExtra("route_pin_count", route.pinCount)
                            putExtra("route_kilometer", route.kilometer)
                            putExtra("route_estimated_fare", route.estimatedTotalFare)
                            putExtra("route_status", route.status)
                            
                            // Pass map details
                            putExtra("map_center_lng", route.mapDetails?.center?.lng ?: 0.0)
                            putExtra("map_center_lat", route.mapDetails?.center?.lat ?: 0.0)
                            putExtra("map_zoom", route.mapDetails?.zoom ?: 13.0)
                            
                            // Pass route line data if available
                            val routeLine = route.mapDetails?.routeLine
                            if (routeLine != null) {
                                putExtra("route_line_type", routeLine.type)
                                putExtra("route_line_geometry_type", routeLine.geometry?.type ?: "")
                                putExtra("route_line_geometry_data_type", routeLine.geometry?.geometry?.type ?: "")
                                // Pass coordinates as JSON string for complex data
                                val coordinates = routeLine.geometry?.geometry?.coordinates
                                if (coordinates != null) {
                                    val coordinatesJson = com.google.gson.Gson().toJson(coordinates)
                                    putExtra("route_line_coordinates", coordinatesJson)
                                }
                            }
                            
                            // Pass pins data as JSON string
                            val pinsJson = com.google.gson.Gson().toJson(pins)
                            putExtra("route_pins", pinsJson)
                            
                            // Pass fifo order if available
                            val fifoOrder = route.mapDetails?.fifoOrder
                            if (fifoOrder != null) {
                                val fifoOrderJson = com.google.gson.Gson().toJson(fifoOrder)
                                putExtra("route_fifo_order", fifoOrderJson)
                            }
                            
                            // Pass start and end coordinates
                            putExtra("start_lat", firstPin.lat)
                            putExtra("start_lng", firstPin.lng)
                            putExtra("end_lat", dest.lat)
                            putExtra("end_lng", dest.lng)
                            putExtra("dest_name", dest.name)
                        }
                        startActivity(intent)
                    }
                    .show()
            }
        }
        
        routeDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedRoute = routes[position]
            btnEnterRoute.isEnabled = true
            loadRouteOnMap(selectedRoute!!)
        }
    }
    
    private fun loadRoutes() {
        showLoading(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Attempting to load routes from API...")
                
                // Add a small delay to ensure network is ready
                kotlinx.coroutines.delay(1000)
                
                val response = apiService.getRoutes(status = "active")
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    
                    Log.d(TAG, "Response received - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")
                    
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        Log.d(TAG, "Response body: $apiResponse")
                        
                        if (apiResponse != null && apiResponse.success) {
                            routes.clear()
                            apiResponse.data?.routes?.let { routeList ->
                                Log.d(TAG, "Loaded ${routeList.size} routes from API")
                                // Log each route for debugging
                                routeList.forEach { route ->
                                    Log.d(TAG, "Route: ${route.name}, Pins: ${route.mapDetails?.pins?.size ?: 0}")
                                }
                                routes.addAll(routeList)
                                setupDropdown()
                            } ?: run {
                                Log.w(TAG, "No routes found in API response")
                                showError("No routes available")
                            }
                        } else {
                            Log.e(TAG, "API returned error: ${apiResponse?.message ?: "Unknown error"}")
                            showError("Failed to load routes: ${apiResponse?.message ?: "Unknown error"}")
                        }
                    } else {
                        Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Error body: $errorBody")
                        
                        // Try fallback routes for HTTP errors too
                        loadFallbackRoutes()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.e(TAG, "Exception loading routes: ${e.javaClass.simpleName} - ${e.message}")
                    Log.e(TAG, "Error loading routes", e)
                    
                    // Try to load fallback routes if API fails
                    loadFallbackRoutes()
                }
            }
        }
    }
    
    private fun loadFallbackRoutes() {
        Log.d(TAG, "Loading fallback routes...")
        try {
            // Create some sample routes as fallback
            val fallbackRoutes = listOf(
                Route(
                    id = 1,
                    name = "Sample Route 1",
                    description = "A sample route for testing",
                    pinCount = 3,
                    kilometer = "5.2",
                    estimatedTotalFare = "25.0",
                    status = "active",
                    mapDetails = null
                ),
                Route(
                    id = 2,
                    name = "Sample Route 2", 
                    description = "Another sample route",
                    pinCount = 4,
                    kilometer = "7.8",
                    estimatedTotalFare = "35.0",
                    status = "active",
                    mapDetails = null
                )
            )
            
            routes.clear()
            routes.addAll(fallbackRoutes)
            setupDropdown()
            
            Toast.makeText(this, "Using offline routes", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Fallback routes loaded successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load fallback routes", e)
            showError("Unable to load routes. Please check your internet connection.")
        }
    }
    
    private fun setupDropdown() {
        val routeNames = routes.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, routeNames)
        routeDropdown.setAdapter(adapter)
        
        if (routes.isNotEmpty()) {
            routeDropdown.setText(routeNames[0], false)
            selectedRoute = routes[0]
            btnEnterRoute.isEnabled = true
            loadRouteOnMap(selectedRoute!!)
        }
    }
    
    private fun loadRouteOnMap(route: Route) {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            // Initialize annotation managers
            initializeAnnotationManagers()
            
            // Clear existing annotations
            clearMapAnnotations()
            
            // Draw polyline FIRST (so it sits below)
            addRoutePolyline(route, style)
            
            // Then add pins and labels ABOVE the polyline
            addRoutePins(route, style)
            
            // Fit camera to show the entire route
            fitCameraToRoute(route)
            
            // Populate closest waypoint card
            populateClosestWaypointCard()
            
            // Allow choosing a waypoint from dropdown via button
            btnEnterRoute.setOnClickListener { showWaypointChooser() }
        }
    }
    
    private fun initializeAnnotationManagers() {
        // Simplified implementation - just log for now
        Log.d(TAG, "Initializing annotation managers (simplified)")
    }
    
    private fun clearMapAnnotations() {
        mapView.getMapboxMap().getStyle { style ->
            try {
                // Remove existing sources and layers
                if (style.styleSourceExists("route-pins")) {
                    style.removeStyleSource("route-pins")
                }
                if (style.styleLayerExists("route-pins-layer")) {
                    style.removeStyleLayer("route-pins-layer")
                }
                if (style.styleLayerExists("route-pins-text-layer")) {
                    style.removeStyleLayer("route-pins-text-layer")
                }
                if (style.styleSourceExists("route-line")) {
                    style.removeStyleSource("route-line")
                }
                if (style.styleLayerExists("route-line-layer")) {
                    style.removeStyleLayer("route-line-layer")
                }
                Log.d(TAG, "Cleared existing map annotations")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing map annotations", e)
            }
        }
    }
    
    private fun addRoutePins(route: Route, style: Style) {
        val pins = route.mapDetails?.pins
        Log.d(TAG, "=== PIN RENDERING DEBUG ===")
        Log.d(TAG, "Route: ${route.name}")
        Log.d(TAG, "MapDetails present: ${route.mapDetails != null}")
        Log.d(TAG, "Pins present: ${pins != null}")
        Log.d(TAG, "Pins count: ${pins?.size ?: 0}")
        
        if (pins != null && pins.isNotEmpty()) {
            Log.d(TAG, "Adding ${pins.size} pins for route: ${route.name}")
            
            // Log each pin with detailed information
            pins.forEachIndexed { index, pin ->
                Log.d(TAG, "Pin $index: ${pin.name} at ${pin.lat}, ${pin.lng}")
                Log.d(TAG, "  - ID: ${pin.id}")
                Log.d(TAG, "  - Number: ${pin.number}")
                Log.d(TAG, "  - Address: ${pin.address}")
                Log.d(TAG, "  - Place: ${pin.placeName}")
            }
            
            // Create features for pins with properties for text labels
            val pinFeatures = pins.map { pin ->
                Feature.fromGeometry(
                    Point.fromLngLat(pin.lng, pin.lat)
                ).apply {
                    addStringProperty("name", pin.name)
                    addStringProperty("address", pin.address ?: pin.placeName ?: "")
                    addNumberProperty("number", pin.number)
                }
            }
            
            // Add pins to map using the provided style
            try {
                if (style.styleLayerExists("route-pins-layer")) {
                    style.removeStyleLayer("route-pins-layer")
                }
                if (style.styleLayerExists("route-pins-text-layer")) {
                    style.removeStyleLayer("route-pins-text-layer")
                }
                if (style.styleSourceExists("route-pins")) {
                    style.removeStyleSource("route-pins")
                }

                // Add custom pin image (same as MapActivity)
                vectorToBitmap(R.drawable.ic_custom_pin)?.let { bmp ->
                    style.addImage("route-pin-icon", bmp)
                }

                // Add pins source
                style.addSource(
                    geoJsonSource("route-pins") {
                        featureCollection(FeatureCollection.fromFeatures(pinFeatures))
                    }
                )
                
                // Ensure route line layer exists first. Then add pins above it.
                // Add pins layer using symbol layer with custom pin icon ABOVE the route line
                style.addLayer(
                    symbolLayer("route-pins-layer", "route-pins") {
                        iconImage("route-pin-icon")
                        iconSize(0.8)
                        iconAllowOverlap(true)
                        iconIgnorePlacement(true)
                    }
                )
                
                // Add text labels layer for pin names ABOVE the pins layer
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
                
                Log.d(TAG, "Successfully added ${pins.size} pins to map with custom pin icons")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add pins to map", e)
            }
        } else {
            Log.w(TAG, "No pins found for route: ${route.name}")
        }
        
        Log.d(TAG, "Loading route: ${route.name}")
        Log.d(TAG, "Pin count: ${route.pinCount}")
        Log.d(TAG, "Distance: ${route.kilometer} km")
        Log.d(TAG, "Fare: ₱${route.estimatedTotalFare}")
        Log.d(TAG, "=== END PIN RENDERING DEBUG ===")
        
        // Add click listener for pins
        setupPinClickListener()
    }
    
    private fun addRoutePolyline(route: Route, style: Style) {
        val routeLine = route.mapDetails?.routeLine
        if (routeLine?.geometry?.geometry?.coordinates != null) {
            Log.d(TAG, "Adding route polyline for route: ${route.name}")
            
            try {
                // Get coordinates from route line (nested structure)
                val coordinates = routeLine.geometry.geometry.coordinates
                if (coordinates.isNotEmpty()) {
                    Log.d(TAG, "Route polyline has ${coordinates.size} coordinate points")
                    
                    // Log first few coordinates for debugging
                    coordinates.take(5).forEachIndexed { index, coord ->
                        Log.d(TAG, "  Point $index: [${coord[0]}, ${coord[1]}]")
                    }
                    
                    if (coordinates.size > 5) {
                        Log.d(TAG, "  ... and ${coordinates.size - 5} more points")
                    }
                    
                    // Create LineString from coordinates
                    val lineString = LineString.fromLngLats(
                        coordinates.map { coord: List<Double> ->
                            Point.fromLngLat(coord[0], coord[1])
                        }
                    )
                    
                    // Create feature for the route line
                    val routeFeature = Feature.fromGeometry(lineString)
                    
                    // Add route line to map using the provided style
                    try {
                        // Remove existing route line if it exists
                        if (style.styleSourceExists("route-line")) {
                            style.removeStyleSource("route-line")
                        }
                        if (style.styleLayerExists("route-line-layer")) {
                            style.removeStyleLayer("route-line-layer")
                        }
                        
                        // Add route line source
                        style.addSource(
                            geoJsonSource("route-line") {
                                feature(routeFeature)
                            }
                        )
                        
                        // Add route line layer
                        style.addLayer(
                            lineLayer("route-line-layer", "route-line") {
                                // Beige route line from API response
                                lineColor("#D2B48C")
                                lineWidth(4.0)
                                lineOpacity(0.9)
                            }
                        )
                        
                        Log.d(TAG, "Successfully added route polyline with ${coordinates.size} points")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to add route polyline to map", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing route line data", e)
            }
        } else {
            Log.d(TAG, "No route line data available for route: ${route.name}")
            // Fallback: create polyline from pins if no route line data
            createFallbackPolylineFromPins(route, style)
        }
    }
    
    private fun createFallbackPolylineFromPins(route: Route, style: Style) {
        val pins = route.mapDetails?.pins
        if (pins.isNullOrEmpty()) {
            Log.d(TAG, "No pins available for fallback polyline")
            return
        }
        
        Log.d(TAG, "Creating fallback polyline from ${pins.size} pins")
        
        // Sort pins by their number to get the correct sequence
        val sortedPins = pins.sortedBy { it.number }
        
        // Create coordinates for the polyline
        val coordinates = sortedPins.map { pin ->
            Point.fromLngLat(pin.lng, pin.lat)
        }
        
        try {
            val lineString = LineString.fromLngLats(coordinates)
            val routeFeature = Feature.fromGeometry(lineString)
            
            try {
                if (style.styleLayerExists("route-line-layer")) {
                    style.removeStyleLayer("route-line-layer")
                }
                if (style.styleSourceExists("route-line")) {
                    style.removeStyleSource("route-line")
                }
                
                // Add fallback polyline source
                style.addSource(
                    geoJsonSource("route-line") {
                        feature(routeFeature)
                    }
                )
                
                // Add fallback polyline layer
                style.addLayer(
                    lineLayer("route-line-layer", "route-line") {
                        lineColor("#D2B48C") // Beige color for API response
                        lineWidth(4.0)
                        lineOpacity(0.9)
                    }
                )
                
                Log.d(TAG, "Successfully added fallback polyline with ${coordinates.size} points")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add fallback polyline to map", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating fallback polyline", e)
        }
    }

    // Compute closest pin to last known device location. If no location, fallback to first pin
    private fun populateClosestWaypointCard() {
        val pins = selectedRoute?.mapDetails?.pins ?: return
        if (pins.isEmpty()) return
        
        // Try to get last location (no continuous updates to keep it simple here)
        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissions missing; fallback to route center
            updateCardWithReference(Point.fromLngLat(pins.first().lng, pins.first().lat), pins)
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc ->
                val ref = if (loc != null) Point.fromLngLat(loc.longitude, loc.latitude)
                else Point.fromLngLat(pins.first().lng, pins.first().lat)
                updateCardWithReference(ref, pins)
            }
            .addOnFailureListener {
                updateCardWithReference(Point.fromLngLat(pins.first().lng, pins.first().lat), pins)
            }
    }
    
    private fun updateCardWithReference(reference: Point, pins: List<Pin>) {
        // Find closest pin
        var closest: Pin = pins.first()
        var best = Double.MAX_VALUE
        for (p in pins) {
            val d = computeHaversineMeters(
                reference.latitude(), reference.longitude(), p.lat, p.lng
            )
            if (d < best) { best = d; closest = p }
        }
        val distanceMeters = best
        val distanceKm = distanceMeters / 1000.0
        val etaMin = estimateEtaMinutes(distanceMeters, "Car").toInt()
        val fare = estimateFare(distanceKm)
        val traffic = "None" // Placeholder; could be computed if traffic data is available
        
        // Update of UI card removed; only log for debug
        Log.d(TAG, "Closest waypoint: ${closest.name}, distance ${formatDistance(distanceMeters)}")
        
        // Create/refresh geofence for chosen waypoint using AppSettings radius
        createGeofenceForPin(Point.fromLngLat(closest.lng, closest.lat))
    }
    
    private fun estimateEtaMinutes(distanceMeters: Double, mode: String): Double {
        val speed = when (mode) {
            "Car" -> 13.9 // ~50 km/h
            "Walk" -> 1.4 // ~5 km/h
            "Motor" -> 8.3 // ~30 km/h
            else -> 13.9
        }
        return distanceMeters / speed / 60.0
    }

    private fun setCardCollapsed(collapsed: Boolean) {
        // No-op: card UI removed from RoutesActivity
    }
    
    private fun formatDistance(meters: Double): String {
        return if (meters >= 1000) "${String.format("%.1f", meters / 1000)} km" else "${meters.toInt()} m"
    }
    
    private fun estimateFare(km: Double): Double {
        val base = 15.0
        val extra = if (km > 1.0) (km - 1.0) * 5.0 else 0.0
        return base + extra
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
    
    private fun showWaypointChooser() {
        val pins = selectedRoute?.mapDetails?.pins ?: return
        if (pins.isEmpty()) return
        val names = pins.map { it.name }.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Choose Destination")
            .setItems(names) { _, which ->
                val destPin = pins[which]
                val route = selectedRoute
                if (route != null) {
                    val intent = Intent(this, RoutesMapsActivity::class.java).apply {
                        // Pass route information
                        putExtra("route_id", route.id)
                        putExtra("route_name", route.name)
                        putExtra("route_description", route.description ?: "")
                        putExtra("route_pin_count", route.pinCount)
                        putExtra("route_kilometer", route.kilometer)
                        putExtra("route_estimated_fare", route.estimatedTotalFare)
                        putExtra("route_status", route.status)
                        
                        // Pass map details
                        putExtra("map_center_lng", route.mapDetails?.center?.lng ?: 0.0)
                        putExtra("map_center_lat", route.mapDetails?.center?.lat ?: 0.0)
                        putExtra("map_zoom", route.mapDetails?.zoom ?: 13.0)
                        
                        // Pass route line data if available
                        val routeLine = route.mapDetails?.routeLine
                        if (routeLine != null) {
                            putExtra("route_line_type", routeLine.type)
                            putExtra("route_line_geometry_type", routeLine.geometry?.type ?: "")
                            putExtra("route_line_geometry_data_type", routeLine.geometry?.geometry?.type ?: "")
                            // Pass coordinates as JSON string for complex data
                            val coordinates = routeLine.geometry?.geometry?.coordinates
                            if (coordinates != null) {
                                val coordinatesJson = com.google.gson.Gson().toJson(coordinates)
                                putExtra("route_line_coordinates", coordinatesJson)
                            }
                        }
                        
                        // Pass pins data as JSON string
                        val pinsJson = com.google.gson.Gson().toJson(pins)
                        putExtra("route_pins", pinsJson)
                        
                        // Pass fifo order if available
                        val fifoOrder = route.mapDetails?.fifoOrder
                        if (fifoOrder != null) {
                            val fifoOrderJson = com.google.gson.Gson().toJson(fifoOrder)
                            putExtra("route_fifo_order", fifoOrderJson)
                        }
                        
                        // No current location from pin - will use device location
                        putExtra("current_lat", 0.0)
                        putExtra("current_lng", 0.0)
                        putExtra("current_pin_name", "")
                        // Pass the selected pin as destination
                        putExtra("end_lat", destPin.lat)
                        putExtra("end_lng", destPin.lng)
                        putExtra("dest_name", destPin.name)
                    }
                    startActivity(intent)
                }
            }
            .show()
    }
    
    // Geofence using AppSettings radius
    private fun createGeofenceForPin(point: Point) {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val bgGranted = androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!bgGranted) return
        }
        val appSettings = com.example.gzingapp.utils.AppSettings(this)
        val radius = appSettings.getAlarmFenceRadiusMeters().toFloat()
        val geofence = com.google.android.gms.location.Geofence.Builder()
            .setRequestId("route_waypoint_geofence")
            .setCircularRegion(point.latitude(), point.longitude(), radius)
            .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
            .setTransitionTypes(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER or com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        val request = com.google.android.gms.location.GeofencingRequest.Builder()
            .setInitialTrigger(com.google.android.gms.location.GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            3001,
            android.content.Intent(this, com.example.gzingapp.GeofenceBroadcastReceiver::class.java),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        geofencingClient.removeGeofences(pendingIntent)
        geofencingClient.addGeofences(request, pendingIntent)
    }
    
    private fun setupPinClickListener() {
        // Add click listener to detect pin touches
        mapView.gestures.addOnMapClickListener { point ->
            // Select nearest pin like MapActivity tooltip style
            val pins = selectedRoute?.mapDetails?.pins
            if (!pins.isNullOrEmpty()) {
                var nearest: Pin = pins.first()
                var best = Double.MAX_VALUE
                for (p in pins) {
                    val d = computeHaversineMeters(point.latitude(), point.longitude(), p.lat, p.lng)
                    if (d < best) { best = d; nearest = p }
                }
                showPinTooltip(nearest, point)
                // Update selection only for local display; no card UI in RoutesActivity
            } else {
                hideTooltip()
            }
            true // Consume the click event
        }
    }
    
    private fun showPinTooltip(pin: Pin, point: com.mapbox.geojson.Point) {
        // Hide any existing tooltip
        hideTooltip()
        
        // Show custom white-background toast for better visibility
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
        Log.d(TAG, "Showing tooltip for pin: ${pin.name}")
    }
    
    
    private fun hideTooltip() {
        currentTooltip = null
    }
    
    private fun navigateToPin(pin: Pin) {
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra("navigation_mode", true)
            putExtra("end_lat", pin.lat)
            putExtra("end_lng", pin.lng)
            putExtra("destination_name", pin.name)
        }
        startActivity(intent)
        hideTooltip()
    }
    
    private fun navigateFromPin(pin: Pin) {
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra("navigation_mode", true)
            putExtra("start_lat", pin.lat)
            putExtra("start_lng", pin.lng)
            putExtra("start_name", pin.name)
        }
        startActivity(intent)
        hideTooltip()
    }
    
    private fun fitCameraToRoute(route: Route) {
        val pins = route.mapDetails?.pins
        val routeLine = route.mapDetails?.routeLine
        
        if (pins != null && pins.isNotEmpty()) {
            // Calculate bounds from pins
            val lats = pins.map { it.lat }
            val lngs = pins.map { it.lng }
            
            var minLat = lats.minOrNull() ?: 0.0
            var maxLat = lats.maxOrNull() ?: 0.0
            var minLng = lngs.minOrNull() ?: 0.0
            var maxLng = lngs.maxOrNull() ?: 0.0
            
            // If route line exists, use its coordinates for better bounds
            if (routeLine?.geometry?.geometry?.coordinates != null) {
                val routeCoords = routeLine.geometry.geometry.coordinates
                val routeLats = routeCoords.map { coord: List<Double> -> coord[1] }
                val routeLngs = routeCoords.map { coord: List<Double> -> coord[0] }
                
                minLat = minOf(minLat, routeLats.minOrNull() ?: minLat)
                maxLat = maxOf(maxLat, routeLats.maxOrNull() ?: maxLat)
                minLng = minOf(minLng, routeLngs.minOrNull() ?: minLng)
                maxLng = maxOf(maxLng, routeLngs.maxOrNull() ?: maxLng)
            }
            
            // Add padding
            val latPadding = (maxLat - minLat) * 0.1
            val lngPadding = (maxLng - minLng) * 0.1
            
            // Fit camera to bounds using a simpler approach
            val centerLat = (minLat + maxLat) / 2
            val centerLng = (minLng + maxLng) / 2
            val centerPoint = Point.fromLngLat(centerLng, centerLat)
            
            // Calculate appropriate zoom level based on bounds
            val latRange = maxLat - minLat
            val lngRange = maxLng - minLng
            val maxRange = maxOf(latRange, lngRange)
            
            // Adjust zoom based on range (smaller range = higher zoom)
            val zoom = when {
                maxRange > 0.1 -> 10.0
                maxRange > 0.05 -> 12.0
                maxRange > 0.02 -> 14.0
                else -> 16.0
            }
            
            mapView.getMapboxMap().setCamera(
                com.mapbox.maps.CameraOptions.Builder()
                    .center(centerPoint)
                    .zoom(zoom)
                    .build()
            )
            
            Log.d(TAG, "Fitted camera to route bounds - Center: $centerLat, $centerLng, Zoom: $zoom")
        }
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
    }
    
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        hideTooltip()
        mapView.onDestroy()
    }
    
    override fun onPause() {
        super.onPause()
        hideTooltip()
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    
    // Convert vector drawable to Bitmap for Mapbox symbol image (from MapActivity)
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
}
