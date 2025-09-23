package com.example.gzingapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.mapbox.geojson.Point
import kotlinx.coroutines.*
import java.net.URL
import org.json.JSONObject
import org.json.JSONArray

class PlacesActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvResults: RecyclerView
    private lateinit var tvResultsCount: TextView
    private lateinit var layoutEmptyState: View
    private lateinit var progressBar: ProgressBar

    private lateinit var resultsAdapter: PlaceResultsAdapter
    private var searchJob: Job? = null
    
    // Location services
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Point? = null
    private lateinit var tvCurrentLocation: TextView

    // Search restricted to Philippines only (no specific city restrictions)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)

        initializeViews()
        setupToolbar()
        setupSearchEngine()
        setupRecyclerView()
        setupSearchInput()
        setupLocationServices()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        etSearch = findViewById(R.id.etSearch)
        rvResults = findViewById(R.id.rvResults)
        tvResultsCount = findViewById(R.id.tvResultsCount)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        progressBar = findViewById(R.id.progressBar)
        tvCurrentLocation = findViewById(R.id.tvCurrentLocation)
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupSearchEngine() {
        // No initialization needed for HTTP API approach
    }

    private fun setupRecyclerView() {
        resultsAdapter = PlaceResultsAdapter { place ->
            navigateToMapWithPlace(place)
        }
        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = resultsAdapter
    }

    private fun setupSearchInput() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim()
                if (query.isNullOrEmpty()) {
                    clearResults()
                } else if (query.length >= 2) {
                    searchPlaces(query)
                }
            }
        })
        
        // Test the API with a simple query on startup
        testApiConnection()
    }
    
    private fun setupLocationServices() {
        // Check location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }
    
    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val point = Point.fromLngLat(it.longitude, it.latitude)
                    currentLocation = point
                    performReverseGeocodingForCurrentLocation(point)
                } ?: run {
                    tvCurrentLocation.text = "Location not available"
                }
            }
        }
    }
    
    private fun performReverseGeocodingForCurrentLocation(point: Point) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val address = getAddressFromCoordinates(point.latitude(), point.longitude())
                withContext(Dispatchers.Main) {
                    tvCurrentLocation.text = "📍 Current: $address"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvCurrentLocation.text = "📍 Current: ${point.latitude()}, ${point.longitude()}"
                }
            }
        }
    }
    
    private suspend fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&zoom=18&addressdetails=1"
                val connection = URL(url).openConnection()
                connection.setRequestProperty("User-Agent", "GzingApp/1.0")
                val response = connection.getInputStream().bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                val address = json.optJSONObject("address")
                if (address != null) {
                    val parts = mutableListOf<String>()
                    
                    // Try to get readable address components
                    address.optString("house_number")?.let { if (it.isNotEmpty()) parts.add(it) }
                    address.optString("road")?.let { if (it.isNotEmpty()) parts.add(it) }
                    address.optString("suburb")?.let { if (it.isNotEmpty()) parts.add(it) }
                    address.optString("city")?.let { if (it.isNotEmpty()) parts.add(it) }
                    address.optString("state")?.let { if (it.isNotEmpty()) parts.add(it) }
                    address.optString("country")?.let { if (it.isNotEmpty()) parts.add(it) }
                    
                    if (parts.isNotEmpty()) {
                        parts.joinToString(", ")
                    } else {
                        json.optString("display_name", "Unknown location")
                    }
                } else {
                    json.optString("display_name", "Unknown location")
                }
            } catch (e: Exception) {
                "Unknown location"
            }
        }
    }
    
    private fun testApiConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val accessToken = getString(R.string.mapbox_access_token)
                val testUrl = "https://api.mapbox.com/geocoding/v5/mapbox.places/Manila.json?access_token=$accessToken&limit=1"
                val response = URL(testUrl).readText()
                Log.d("PlacesActivity", "API Test successful: ${response.length} characters received")
            } catch (e: Exception) {
                Log.e("PlacesActivity", "API Test failed: ${e.message}", e)
            }
        }
    }
    
    private fun getPopularLandmarks(): List<String> {
        return listOf(
            // Manila and Metro Manila
            "Rizal Park", "Luneta", "Intramuros", "Manila Cathedral", "San Agustin Church",
            "Fort Santiago", "National Museum", "Cultural Center of the Philippines", "CCP",
            "SM Mall of Asia", "Ayala Museum", "Greenbelt", "Glorietta", "Rockwell",
            "Manila Ocean Park", "Star City", "Quezon Memorial Circle", "Ninoy Aquino Parks and Wildlife",
            "La Mesa Eco Park", "Manila Bay", "Roxas Boulevard", "Makati", "Taguig", "Pasig",
            "Mandaluyong", "San Juan", "Marikina", "Quezon City", "Caloocan", "Las Piñas",
            "Muntinlupa", "Parañaque", "Pasay", "Pateros", "Valenzuela",
            
            // Antipolo and Rizal
            "Antipolo Cathedral", "Our Lady of Peace and Good Voyage", "Hinulugang Taktak",
            "Marikina Sports Center", "Marikina River Park", "Marikina Shoe Museum",
            "Cainta", "Taytay", "Angono", "Binangonan", "Cardona", "Jala-jala", "Morong",
            "Pililla", "Rodriguez", "San Mateo", "Tanay", "Teresa",
            
            // Luzon Provinces
            "Tagaytay", "Taal Volcano", "Baguio", "Burnham Park", "Session Road", "Mines View Park",
            "Banaue Rice Terraces", "Sagada", "Vigan", "Ilocos Norte", "Ilocos Sur", "La Union",
            "Pangasinan", "Tarlac", "Nueva Ecija", "Bulacan", "Pampanga", "Zambales",
            "Bataan", "Aurora", "Nueva Vizcaya", "Quirino", "Isabela", "Cagayan",
            "Kalinga", "Apayao", "Mountain Province", "Ifugao", "Benguet", "Abra",
            
            // Visayas
            "Cebu", "Chocolate Hills", "Bohol", "Iloilo", "Bacolod", "Cagayan de Oro",
            "Boracay", "Aklan", "Capiz", "Antique", "Guimaras", "Negros Occidental",
            "Negros Oriental", "Siquijor", "Biliran", "Leyte", "Southern Leyte",
            "Samar", "Northern Samar", "Eastern Samar", "Masbate", "Romblon",
            "Marinduque", "Occidental Mindoro", "Oriental Mindoro", "Palawan",
            
            // Mindanao
            "Davao", "Samal Island", "Zamboanga", "Cagayan de Oro", "Iligan", "Butuan",
            "Surigao", "Agusan del Norte", "Agusan del Sur", "Surigao del Norte",
            "Surigao del Sur", "Dinagat Islands", "Camiguin", "Misamis Oriental",
            "Misamis Occidental", "Lanao del Norte", "Lanao del Sur", "Maguindanao",
            "Sultan Kudarat", "South Cotabato", "North Cotabato", "Sarangani",
            "Davao del Norte", "Davao del Sur", "Davao Oriental", "Davao Occidental",
            "Compostela Valley", "Zamboanga del Norte", "Zamboanga del Sur",
            "Zamboanga Sibugay", "Basilan", "Sulu", "Tawi-Tawi",
            
            // Popular Streets and Roads
            "EDSA", "Commonwealth Avenue", "Quezon Avenue", "España Boulevard",
            "Taft Avenue", "Roxas Boulevard", "Ayala Avenue", "Ortigas Avenue",
            "Shaw Boulevard", "Boni Avenue", "Gil Puyat Avenue", "Buendia Avenue",
            "Makati Avenue", "Ayala Avenue", "Paseo de Roxas", "Salcedo Street",
            "Legazpi Street", "Greenbelt", "Glorietta", "Rockwell", "Power Plant Mall",
            
            // Universities and Schools
            "University of the Philippines", "Ateneo de Manila University", "De La Salle University",
            "University of Santo Tomas", "Far Eastern University", "Polytechnic University",
            "Mapua University", "Adamson University", "San Beda University",
            "University of the East", "Lyceum of the Philippines", "Pamantasan ng Lungsod ng Maynila"
        )
    }

    private fun searchPlaces(query: String) {
        // Cancel previous search
        searchJob?.cancel()

        showLoading(true)
        hideEmptyState()

        searchJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("PlacesActivity", "Starting search for: '$query'")
                
                // Check if query matches any popular landmarks
                val matchingLandmarks = getPopularLandmarks().filter { 
                    it.contains(query, ignoreCase = true) || query.contains(it, ignoreCase = true)
                }
                
                if (matchingLandmarks.isNotEmpty()) {
                    Log.d("PlacesActivity", "Found matching landmarks: $matchingLandmarks")
                }
                
                // Try forward geocoding first
                val results = performGeocodingSearch(query).toMutableList()
                
                // If no results or very few results, try reverse geocoding for nearby places
                if (results.size < 3) {
                    Log.d("PlacesActivity", "Few results from forward search, trying reverse geocoding")
                    val reverseResults = performReverseGeocodingSearch(query)
                    results.addAll(reverseResults)
                    
                    // If still few results, try searching for specific place types
                    if (results.size < 3) {
                        Log.d("PlacesActivity", "Still few results, trying specific place type search")
                        val typeResults = performPlaceTypeSearch(query)
                        results.addAll(typeResults)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.d("PlacesActivity", "Search completed with ${results.size} results")
                    displayResults(results)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.e("PlacesActivity", "Search exception: ${e.message}", e)
                    showError("Search failed. Please try again.")
                }
            }
        }
    }

    private fun displayResults(results: List<SearchResult>) {
        if (results.isEmpty()) {
            showEmptyState("No places found")
            return
        }

        // Remove location filtering to allow all Philippines results
        resultsAdapter.updateResults(results)
        tvResultsCount.text = "${results.size} result${if (results.size != 1) "s" else ""}"
        
        if (results.isEmpty()) {
            showEmptyState("No places found")
        } else {
            hideEmptyState()
        }
    }

    // Geographic bounds checking removed - no restrictions

    private fun clearResults() {
        resultsAdapter.clearResults()
        tvResultsCount.text = "0 results"
        showEmptyState("Start typing to search for places")
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(message: String) {
        layoutEmptyState.visibility = View.VISIBLE
        rvResults.visibility = View.GONE
    }

    private fun hideEmptyState() {
        layoutEmptyState.visibility = View.GONE
        rvResults.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        // You can implement a snackbar or toast here
        Log.e("PlacesActivity", message)
    }

    private suspend fun performGeocodingSearch(query: String): List<SearchResult> {
        val accessToken = getString(R.string.mapbox_access_token)
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        // Search restricted to Philippines only - include all types for comprehensive search
        val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json?access_token=$accessToken&limit=20&country=PH&types=poi,address,place,locality,neighborhood,region,district,postcode"
        
        return try {
            Log.d("PlacesActivity", "Searching with URL: $url")
            val response = URL(url).readText()
            Log.d("PlacesActivity", "Raw API Response: $response")
            
            val jsonResponse = JSONObject(response)
            
            // Check if the response has an error
            if (jsonResponse.has("error")) {
                Log.e("PlacesActivity", "API Error: ${jsonResponse.getString("error")}")
                return emptyList()
            }
            
            val features = jsonResponse.getJSONArray("features")
            Log.d("PlacesActivity", "Found ${features.length()} features")
            
            val results = mutableListOf<SearchResult>()
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.getJSONObject("properties")
                val geometry = feature.getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")
                
                Log.d("PlacesActivity", "Feature $i properties: ${properties.toString()}")
                
                // Extract name from the correct property - try multiple sources
                val name = when {
                    feature.has("text") && feature.getString("text").isNotEmpty() -> 
                        feature.getString("text")
                    properties.has("name") && properties.getString("name").isNotEmpty() -> 
                        properties.getString("name")
                    properties.has("text") && properties.getString("text").isNotEmpty() -> 
                        properties.getString("text")
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> {
                        val placeName = feature.getString("place_name")
                        // Extract just the first part before comma for name
                        placeName.split(",")[0].trim()
                    }
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> {
                        val placeName = properties.getString("place_name")
                        // Extract just the first part before comma for name
                        placeName.split(",")[0].trim()
                    }
                    else -> "Unknown Place"
                }
                
                // Extract address from place_name or construct from context
                val address = when {
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> 
                        feature.getString("place_name")
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> 
                        properties.getString("place_name")
                    feature.has("context") -> {
                        // Try to construct address from context
                        val context = feature.getJSONArray("context")
                        if (context.length() > 0) {
                            val addressParts = mutableListOf<String>()
                            for (j in 0 until context.length()) {
                                val contextItem = context.getJSONObject(j)
                                val text = contextItem.optString("text", "")
                                if (text.isNotEmpty()) {
                                    addressParts.add(text)
                                }
                            }
                            if (addressParts.isNotEmpty()) {
                                addressParts.joinToString(", ")
                            } else {
                                "Address not available"
                            }
                        } else {
                            "Address not available"
                        }
                    }
                    properties.has("context") -> {
                        // Try to construct address from context
                        val context = properties.getJSONArray("context")
                        if (context.length() > 0) {
                            val addressParts = mutableListOf<String>()
                            for (j in 0 until context.length()) {
                                val contextItem = context.getJSONObject(j)
                                val text = contextItem.optString("text", "")
                                if (text.isNotEmpty()) {
                                    addressParts.add(text)
                                }
                            }
                            if (addressParts.isNotEmpty()) {
                                addressParts.joinToString(", ")
                            } else {
                                "Address not available"
                            }
                        } else {
                            "Address not available"
                        }
                    }
                    else -> "Address not available"
                }
                
                val lng = coordinates.getDouble(0)
                val lat = coordinates.getDouble(1)
                val point = Point.fromLngLat(lng, lat)
                
                // Determine category based on place type
                val placeType = properties.optJSONArray("category")?.getString(0) ?: 
                               properties.optString("place_type", "location")
                val featurePlaceType = feature.optJSONArray("place_type")?.getString(0) ?: "location"
                
                val category = when {
                    placeType.contains("restaurant") || placeType.contains("food") -> "Restaurant"
                    placeType.contains("hotel") || placeType.contains("accommodation") -> "Hotel"
                    placeType.contains("shop") || placeType.contains("store") || placeType.contains("shopping") -> "Shopping"
                    placeType.contains("hospital") || placeType.contains("health") -> "Hospital"
                    placeType.contains("school") || placeType.contains("education") -> "Education"
                    placeType.contains("place_of_worship") || placeType.contains("church") || placeType.contains("temple") -> "Religious"
                    placeType.contains("bank") || placeType.contains("finance") -> "Bank"
                    placeType.contains("fuel") || placeType.contains("gas") -> "Gas Station"
                    placeType.contains("pharmacy") || placeType.contains("drugstore") -> "Pharmacy"
                    placeType.contains("park") || placeType.contains("recreation") -> "Park"
                    placeType.contains("museum") || placeType.contains("gallery") -> "Museum"
                    placeType.contains("theater") || placeType.contains("cinema") -> "Entertainment"
                    placeType.contains("stadium") || placeType.contains("sports") -> "Sports"
                    placeType.contains("airport") || placeType.contains("station") -> "Transportation"
                    placeType.contains("government") || placeType.contains("office") -> "Government"
                    placeType.contains("landmark") || placeType.contains("monument") -> "Landmark"
                    featurePlaceType == "poi" -> "Point of Interest"
                    featurePlaceType == "address" -> "Address"
                    featurePlaceType == "neighborhood" -> "Neighborhood"
                    featurePlaceType == "locality" -> "City"
                    featurePlaceType == "place" -> "Place"
                    featurePlaceType == "region" -> "Province"
                    featurePlaceType == "district" -> "District"
                    featurePlaceType == "postcode" -> "Postal Code"
                    else -> "Location"
                }
                
                // Only add results that have a valid name
                if (name.isNotEmpty() && name != "Unknown Place") {
                    Log.d("PlacesActivity", "Adding result: name='$name', address='$address', category='$category'")
                    results.add(SearchResult(name, address, point, category))
                } else {
                    Log.d("PlacesActivity", "Skipping result with invalid name: '$name'")
                }
            }
            // If we don't have enough results, try a broader search
            if (results.isEmpty()) {
                Log.d("PlacesActivity", "No results found, trying broader search without bbox")
                val broaderResults = performBroaderSearch(query)
                results.addAll(broaderResults)
                
                // If still no results, try a very simple search
                if (results.isEmpty()) {
                    Log.d("PlacesActivity", "Still no results, trying simple search")
                    val simpleResults = performSimpleSearch(query)
                    results.addAll(simpleResults)
                    
                    // If still no results, try landmark-specific search
                    if (results.isEmpty()) {
                        Log.d("PlacesActivity", "Still no results, trying landmark search")
                        val landmarkResults = performLandmarkSearch(query)
                        results.addAll(landmarkResults)
                    }
                }
            }
            
            results
        } catch (e: Exception) {
            Log.e("PlacesActivity", "Geocoding error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun performReverseGeocodingSearch(query: String): List<SearchResult> {
        val accessToken = getString(R.string.mapbox_access_token)
        
        // Search within Philippines only
        val results = mutableListOf<SearchResult>()
        
        // Search with Philippines country restriction - include all types
        val phResults = searchNearbyPlaces(query, "121.0,14.6", accessToken, "Philippines")
        results.addAll(phResults)
        
        // Remove duplicates based on name and coordinates
        return results.distinctBy { "${it.name}_${it.coordinate.latitude()}_${it.coordinate.longitude()}" }
    }
    
    private suspend fun searchNearbyPlaces(query: String, center: String, accessToken: String, area: String): List<SearchResult> {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        // Search for nearby places within Philippines only - include all types for comprehensive search
        val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json?access_token=$accessToken&proximity=$center&radius=5000&limit=10&country=PH&types=poi,address,place,locality,neighborhood,region,district,postcode"
        
        return try {
            Log.d("PlacesActivity", "Reverse geocoding for $area: $url")
            val response = URL(url).readText()
            Log.d("PlacesActivity", "Reverse geocoding response for $area: $response")
            val jsonResponse = JSONObject(response)
            val features = jsonResponse.getJSONArray("features")
            Log.d("PlacesActivity", "Reverse geocoding found ${features.length()} features in $area")
            
            val results = mutableListOf<SearchResult>()
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.getJSONObject("properties")
                val geometry = feature.getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")
                
                val name = when {
                    feature.has("text") && feature.getString("text").isNotEmpty() -> 
                        feature.getString("text")
                    properties.has("name") && properties.getString("name").isNotEmpty() -> 
                        properties.getString("name")
                    properties.has("text") && properties.getString("text").isNotEmpty() -> 
                        properties.getString("text")
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> {
                        val placeName = feature.getString("place_name")
                        placeName.split(",")[0].trim()
                    }
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> {
                        val placeName = properties.getString("place_name")
                        placeName.split(",")[0].trim()
                    }
                    else -> "Unknown Place"
                }
                
                val address = when {
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> 
                        feature.getString("place_name")
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> 
                        properties.getString("place_name")
                    else -> "Address not available"
                }
                
                val lng = coordinates.getDouble(0)
                val lat = coordinates.getDouble(1)
                val point = Point.fromLngLat(lng, lat)
                
                // Remove location bounds checking to allow all Philippines results
                val placeType = properties.optString("place_type", "location")
                val featurePlaceType = feature.optJSONArray("place_type")?.getString(0) ?: "location"
                
                val category = when {
                    placeType.contains("restaurant") || placeType.contains("food") -> "Restaurant"
                    placeType.contains("hotel") || placeType.contains("accommodation") -> "Hotel"
                    placeType.contains("shop") || placeType.contains("store") || placeType.contains("shopping") -> "Shopping"
                    placeType.contains("hospital") || placeType.contains("health") -> "Hospital"
                    placeType.contains("school") || placeType.contains("education") -> "School"
                    placeType.contains("place_of_worship") || placeType.contains("church") || placeType.contains("temple") -> "Religious"
                    placeType.contains("bank") || placeType.contains("finance") -> "Bank"
                    placeType.contains("fuel") || placeType.contains("gas") -> "Gas Station"
                    placeType.contains("pharmacy") || placeType.contains("drugstore") -> "Pharmacy"
                    placeType.contains("park") || placeType.contains("recreation") -> "Park"
                    placeType.contains("museum") || placeType.contains("gallery") -> "Museum"
                    placeType.contains("theater") || placeType.contains("cinema") -> "Entertainment"
                    placeType.contains("stadium") || placeType.contains("sports") -> "Sports"
                    placeType.contains("airport") || placeType.contains("station") -> "Transportation"
                    placeType.contains("government") || placeType.contains("office") -> "Government"
                    placeType.contains("landmark") || placeType.contains("monument") -> "Landmark"
                    featurePlaceType == "poi" -> "Point of Interest"
                    featurePlaceType == "address" -> "Address"
                    featurePlaceType == "neighborhood" -> "Neighborhood"
                    featurePlaceType == "locality" -> "City"
                    featurePlaceType == "place" -> "Place"
                    featurePlaceType == "region" -> "Province"
                    featurePlaceType == "district" -> "District"
                    featurePlaceType == "postcode" -> "Postal Code"
                    else -> "Location"
                }
                
                if (name.isNotEmpty() && name != "Unknown Place") {
                    Log.d("PlacesActivity", "Adding reverse geocoding result: name='$name', address='$address', category='$category', area='$area'")
                    results.add(SearchResult(name, address, point, category))
                }
            }
            results
        } catch (e: Exception) {
            Log.e("PlacesActivity", "Reverse geocoding error for $area: ${e.message}")
            emptyList()
        }
    }

    private suspend fun performPlaceTypeSearch(query: String): List<SearchResult> {
        val accessToken = getString(R.string.mapbox_access_token)
        val results = mutableListOf<SearchResult>()
        
        // Map common search terms to specific place types
        val placeTypeMappings = mapOf(
            "school" to "school,education,university,college",
            "hospital" to "hospital,health,clinic,medical",
            "restaurant" to "restaurant,food,dining",
            "bank" to "bank,finance,atm",
            "pharmacy" to "pharmacy,drugstore,medicine",
            "gas" to "fuel,gas,station",
            "church" to "place_of_worship,church,temple",
            "park" to "park,recreation,playground",
            "mall" to "shopping,store,market",
            "hotel" to "hotel,accommodation,lodging"
        )
        
        // Find matching place types for the query
        val matchingTypes = placeTypeMappings.filter { (key, _) ->
            query.contains(key, ignoreCase = true) || key.contains(query, ignoreCase = true)
        }
        
        if (matchingTypes.isNotEmpty()) {
            Log.d("PlacesActivity", "Found matching place types: $matchingTypes")
            
            // Search for each matching place type
            for ((_, types) in matchingTypes) {
                val typeResults = searchByPlaceType(types, accessToken)
                results.addAll(typeResults)
            }
        }
        
        // Remove duplicates
        return results.distinctBy { "${it.name}_${it.coordinate.latitude()}_${it.coordinate.longitude()}" }
    }
    
    private suspend fun searchByPlaceType(types: String, accessToken: String): List<SearchResult> {
        val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$types.json?access_token=$accessToken&country=PH&limit=10&types=$types"
        
        return try {
            Log.d("PlacesActivity", "Searching by place type: $url")
            val response = URL(url).readText()
            val jsonResponse = JSONObject(response)
            val features = jsonResponse.getJSONArray("features")
            Log.d("PlacesActivity", "Found ${features.length()} places of type: $types")
            
            val results = mutableListOf<SearchResult>()
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.getJSONObject("properties")
                val geometry = feature.getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")
                
                val name = when {
                    feature.has("text") && feature.getString("text").isNotEmpty() -> 
                        feature.getString("text")
                    properties.has("name") && properties.getString("name").isNotEmpty() -> 
                        properties.getString("name")
                    properties.has("text") && properties.getString("text").isNotEmpty() -> 
                        properties.getString("text")
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> {
                        val placeName = feature.getString("place_name")
                        placeName.split(",")[0].trim()
                    }
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> {
                        val placeName = properties.getString("place_name")
                        placeName.split(",")[0].trim()
                    }
                    else -> "Unknown Place"
                }
                
                val address = when {
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> 
                        feature.getString("place_name")
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> 
                        properties.getString("place_name")
                    else -> "Address not available"
                }
                
                val lng = coordinates.getDouble(0)
                val lat = coordinates.getDouble(1)
                val point = Point.fromLngLat(lng, lat)
                
                val placeType = properties.optString("place_type", "location")
                val featurePlaceType = feature.optJSONArray("place_type")?.getString(0) ?: "location"
                
                val category = when {
                    placeType.contains("restaurant") || placeType.contains("food") -> "Restaurant"
                    placeType.contains("hotel") || placeType.contains("accommodation") -> "Hotel"
                    placeType.contains("shop") || placeType.contains("store") || placeType.contains("shopping") -> "Shopping"
                    placeType.contains("hospital") || placeType.contains("health") -> "Hospital"
                    placeType.contains("school") || placeType.contains("education") -> "School"
                    placeType.contains("place_of_worship") || placeType.contains("church") || placeType.contains("temple") -> "Religious"
                    placeType.contains("bank") || placeType.contains("finance") -> "Bank"
                    placeType.contains("fuel") || placeType.contains("gas") -> "Gas Station"
                    placeType.contains("pharmacy") || placeType.contains("drugstore") -> "Pharmacy"
                    placeType.contains("park") || placeType.contains("recreation") -> "Park"
                    placeType.contains("museum") || placeType.contains("gallery") -> "Museum"
                    placeType.contains("theater") || placeType.contains("cinema") -> "Entertainment"
                    placeType.contains("stadium") || placeType.contains("sports") -> "Sports"
                    placeType.contains("airport") || placeType.contains("station") -> "Transportation"
                    placeType.contains("government") || placeType.contains("office") -> "Government"
                    placeType.contains("landmark") || placeType.contains("monument") -> "Landmark"
                    featurePlaceType == "poi" -> "Point of Interest"
                    featurePlaceType == "address" -> "Address"
                    featurePlaceType == "neighborhood" -> "Neighborhood"
                    featurePlaceType == "locality" -> "City"
                    featurePlaceType == "place" -> "Place"
                    featurePlaceType == "region" -> "Province"
                    featurePlaceType == "district" -> "District"
                    featurePlaceType == "postcode" -> "Postal Code"
                    else -> "Location"
                }
                
                if (name.isNotEmpty() && name != "Unknown Place") {
                    Log.d("PlacesActivity", "Adding place type result: name='$name', address='$address', category='$category'")
                    results.add(SearchResult(name, address, point, category))
                }
            }
            results
        } catch (e: Exception) {
            Log.e("PlacesActivity", "Place type search error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun performBroaderSearch(query: String): List<SearchResult> {
        val accessToken = getString(R.string.mapbox_access_token)
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        // Broader search without bounding box restriction, including all Philippines types
        val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json?access_token=$accessToken&limit=15&country=PH&types=poi,address,place,locality,neighborhood,region,district,postcode"
        
        return try {
            Log.d("PlacesActivity", "Broader search URL: $url")
            val response = URL(url).readText()
            Log.d("PlacesActivity", "Broader search response: $response")
            val jsonResponse = JSONObject(response)
            val features = jsonResponse.getJSONArray("features")
            Log.d("PlacesActivity", "Broader search found ${features.length()} features")
            
            val results = mutableListOf<SearchResult>()
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.getJSONObject("properties")
                val geometry = feature.getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")
                
                val name = when {
                    feature.has("text") && feature.getString("text").isNotEmpty() -> 
                        feature.getString("text")
                    properties.has("name") && properties.getString("name").isNotEmpty() -> 
                        properties.getString("name")
                    properties.has("text") && properties.getString("text").isNotEmpty() -> 
                        properties.getString("text")
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> {
                        val placeName = feature.getString("place_name")
                        placeName.split(",")[0].trim()
                    }
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> {
                        val placeName = properties.getString("place_name")
                        placeName.split(",")[0].trim()
                    }
                    else -> "Unknown Place"
                }
                
                val address = when {
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> 
                        feature.getString("place_name")
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> 
                        properties.getString("place_name")
                    else -> "Address not available"
                }
                val lng = coordinates.getDouble(0)
                val lat = coordinates.getDouble(1)
                val point = Point.fromLngLat(lng, lat)
                
                val placeType = properties.optString("place_type", "location")
                val category = when (placeType) {
                    "poi" -> "Point of Interest"
                    "address" -> "Address"
                    "place" -> "Place"
                    else -> "Location"
                }
                
                if (name.isNotEmpty() && name != "Unknown Place") {
                    results.add(SearchResult(name, address, point, category))
                }
            }
            results
        } catch (e: Exception) {
            Log.e("PlacesActivity", "Broader search error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun performSimpleSearch(query: String): List<SearchResult> {
        val accessToken = getString(R.string.mapbox_access_token)
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        // Very simple search with minimal parameters, including all Philippines types
        val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json?access_token=$accessToken&limit=10&country=PH&types=poi,address,place,locality,neighborhood,region,district,postcode"
        
        return try {
            Log.d("PlacesActivity", "Simple search URL: $url")
            val response = URL(url).readText()
            Log.d("PlacesActivity", "Simple search response: $response")
            val jsonResponse = JSONObject(response)
            val features = jsonResponse.getJSONArray("features")
            Log.d("PlacesActivity", "Simple search found ${features.length()} features")
            
            val results = mutableListOf<SearchResult>()
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.getJSONObject("properties")
                val geometry = feature.getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")
                
                val name = when {
                    feature.has("text") && feature.getString("text").isNotEmpty() -> 
                        feature.getString("text")
                    properties.has("name") && properties.getString("name").isNotEmpty() -> 
                        properties.getString("name")
                    properties.has("text") && properties.getString("text").isNotEmpty() -> 
                        properties.getString("text")
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> {
                        val placeName = feature.getString("place_name")
                        placeName.split(",")[0].trim()
                    }
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> {
                        val placeName = properties.getString("place_name")
                        placeName.split(",")[0].trim()
                    }
                    else -> "Unknown Place"
                }
                
                val address = when {
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> 
                        feature.getString("place_name")
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> 
                        properties.getString("place_name")
                    else -> "Address not available"
                }
                val lng = coordinates.getDouble(0)
                val lat = coordinates.getDouble(1)
                val point = Point.fromLngLat(lng, lat)
                
                val placeType = properties.optString("place_type", "location")
                val featurePlaceType = feature.optJSONArray("place_type")?.getString(0) ?: "location"
                
                val category = when {
                    placeType.contains("restaurant") || placeType.contains("food") -> "Restaurant"
                    placeType.contains("hotel") || placeType.contains("accommodation") -> "Hotel"
                    placeType.contains("shop") || placeType.contains("store") || placeType.contains("shopping") -> "Shopping"
                    placeType.contains("hospital") || placeType.contains("health") -> "Hospital"
                    placeType.contains("school") || placeType.contains("education") -> "Education"
                    placeType.contains("place_of_worship") || placeType.contains("church") || placeType.contains("temple") -> "Religious"
                    placeType.contains("bank") || placeType.contains("finance") -> "Bank"
                    placeType.contains("fuel") || placeType.contains("gas") -> "Gas Station"
                    placeType.contains("pharmacy") || placeType.contains("drugstore") -> "Pharmacy"
                    placeType.contains("park") || placeType.contains("recreation") -> "Park"
                    placeType.contains("museum") || placeType.contains("gallery") -> "Museum"
                    placeType.contains("theater") || placeType.contains("cinema") -> "Entertainment"
                    placeType.contains("stadium") || placeType.contains("sports") -> "Sports"
                    placeType.contains("airport") || placeType.contains("station") -> "Transportation"
                    placeType.contains("government") || placeType.contains("office") -> "Government"
                    placeType.contains("landmark") || placeType.contains("monument") -> "Landmark"
                    featurePlaceType == "poi" -> "Point of Interest"
                    featurePlaceType == "address" -> "Address"
                    featurePlaceType == "neighborhood" -> "Neighborhood"
                    featurePlaceType == "locality" -> "City"
                    featurePlaceType == "place" -> "Place"
                    featurePlaceType == "region" -> "Province"
                    featurePlaceType == "district" -> "District"
                    featurePlaceType == "postcode" -> "Postal Code"
                    else -> "Location"
                }
                
                if (name.isNotEmpty() && name != "Unknown Place") {
                    results.add(SearchResult(name, address, point, category))
                }
            }
            results
        } catch (e: Exception) {
            Log.e("PlacesActivity", "Simple search error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun performLandmarkSearch(query: String): List<SearchResult> {
        val accessToken = getString(R.string.mapbox_access_token)
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        // Landmark-specific search with POI focus - Philippines only
        val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedQuery.json?access_token=$accessToken&limit=10&types=poi&country=PH"
        
        return try {
            Log.d("PlacesActivity", "Landmark search URL: $url")
            val response = URL(url).readText()
            Log.d("PlacesActivity", "Landmark search response: $response")
            val jsonResponse = JSONObject(response)
            val features = jsonResponse.getJSONArray("features")
            Log.d("PlacesActivity", "Landmark search found ${features.length()} features")
            
            val results = mutableListOf<SearchResult>()
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.getJSONObject("properties")
                val geometry = feature.getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")
                
                val name = when {
                    feature.has("text") && feature.getString("text").isNotEmpty() -> 
                        feature.getString("text")
                    properties.has("name") && properties.getString("name").isNotEmpty() -> 
                        properties.getString("name")
                    properties.has("text") && properties.getString("text").isNotEmpty() -> 
                        properties.getString("text")
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> {
                        val placeName = feature.getString("place_name")
                        placeName.split(",")[0].trim()
                    }
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> {
                        val placeName = properties.getString("place_name")
                        placeName.split(",")[0].trim()
                    }
                    else -> "Unknown Place"
                }
                
                val address = when {
                    feature.has("place_name") && feature.getString("place_name").isNotEmpty() -> 
                        feature.getString("place_name")
                    properties.has("place_name") && properties.getString("place_name").isNotEmpty() -> 
                        properties.getString("place_name")
                    else -> "Address not available"
                }
                
                val lng = coordinates.getDouble(0)
                val lat = coordinates.getDouble(1)
                val point = Point.fromLngLat(lng, lat)
                
                val placeType = properties.optString("place_type", "location")
                val featurePlaceType = feature.optJSONArray("place_type")?.getString(0) ?: "location"
                
                val category = when {
                    placeType.contains("restaurant") || placeType.contains("food") -> "Restaurant"
                    placeType.contains("hotel") || placeType.contains("accommodation") -> "Hotel"
                    placeType.contains("shop") || placeType.contains("store") || placeType.contains("shopping") -> "Shopping"
                    placeType.contains("hospital") || placeType.contains("health") -> "Hospital"
                    placeType.contains("school") || placeType.contains("education") -> "Education"
                    placeType.contains("place_of_worship") || placeType.contains("church") || placeType.contains("temple") -> "Religious"
                    placeType.contains("bank") || placeType.contains("finance") -> "Bank"
                    placeType.contains("fuel") || placeType.contains("gas") -> "Gas Station"
                    placeType.contains("pharmacy") || placeType.contains("drugstore") -> "Pharmacy"
                    placeType.contains("park") || placeType.contains("recreation") -> "Park"
                    placeType.contains("museum") || placeType.contains("gallery") -> "Museum"
                    placeType.contains("theater") || placeType.contains("cinema") -> "Entertainment"
                    placeType.contains("stadium") || placeType.contains("sports") -> "Sports"
                    placeType.contains("airport") || placeType.contains("station") -> "Transportation"
                    placeType.contains("government") || placeType.contains("office") -> "Government"
                    placeType.contains("landmark") || placeType.contains("monument") -> "Landmark"
                    featurePlaceType == "poi" -> "Point of Interest"
                    featurePlaceType == "address" -> "Address"
                    featurePlaceType == "neighborhood" -> "Neighborhood"
                    featurePlaceType == "locality" -> "City"
                    featurePlaceType == "place" -> "Place"
                    featurePlaceType == "region" -> "Province"
                    featurePlaceType == "district" -> "District"
                    featurePlaceType == "postcode" -> "Postal Code"
                    else -> "Location"
                }
                
                if (name.isNotEmpty() && name != "Unknown Place") {
                    results.add(SearchResult(name, address, point, category))
                }
            }
            results
        } catch (e: Exception) {
            Log.e("PlacesActivity", "Landmark search error: ${e.message}")
            emptyList()
        }
    }

    private fun navigateToMapWithPlace(place: SearchResult) {
        val point = place.coordinate
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra("selected_place_lat", point.latitude())
            putExtra("selected_place_lng", point.longitude())
            putExtra("selected_place_name", place.name)
            putExtra("selected_place_address", place.address)
            putExtra("transport_mode", "Car") // Default to driving
        }
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    tvCurrentLocation.text = "📍 Location permission denied"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchJob?.cancel()
    }
}
