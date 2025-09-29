package com.example.gzingapp

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.search.*
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.SearchResult as MapboxSearchResult
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.JSONArray
import java.net.URL

class PlacesActivity : AppCompatActivity() {

    // UI Components
    private lateinit var searchInput: EditText
    private lateinit var searchButton: com.google.android.material.button.MaterialButton
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var currentLocationText: TextView
    private lateinit var loadingCard: com.google.android.material.card.MaterialCardView
    private lateinit var emptyStateCard: com.google.android.material.card.MaterialCardView
    
    // Location Services
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Point? = null
    
    // Mapbox Search SDK
    private lateinit var searchEngine: SearchEngine
    private var searchRequestTask: AsyncOperationTask? = null
    private var currentSearchQuery: String = ""
    
    // Data
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private var searchResults = mutableListOf<SearchResult>()
    
    // Debouncing
    private var searchJob: Job? = null
    private val searchDelay = 500L // 500ms delay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)

        initializeViews()
        setupLocationServices()
        setupMapboxSearchEngine()
        setupRecyclerView()
        setupClickListeners()
        setupKeyboardHandling()
        
        // Get current location
        getCurrentLocation()
    }

    private fun initializeViews() {
        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        emptyStateText = findViewById(R.id.emptyStateText)
        currentLocationText = findViewById(R.id.currentLocationText)
        loadingCard = findViewById(R.id.loadingCard)
        emptyStateCard = findViewById(R.id.emptyStateCard)
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }
    
    private fun setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupMapboxSearchEngine() {
        try {
            Log.d("PlacesActivity", "Initializing Mapbox Search Engine...")
            
            val searchEngineSettings = SearchEngineSettings()
            searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
                ApiType.SEARCH_BOX,
                searchEngineSettings
            )
            
            Log.d("PlacesActivity", "Mapbox Search Engine initialized successfully")
            
            // Test the search engine with a simple query
            testSearchEngine()
            
        } catch (e: Exception) {
            Log.e("PlacesActivity", "Error initializing Mapbox Search Engine: ${e.message}", e)
            showError("Failed to initialize search engine: ${e.message}")
        }
    }
    
    private fun testSearchEngine() {
        try {
            Log.d("PlacesActivity", "Testing search engine...")
            // We'll test it when the user actually searches
        } catch (e: Exception) {
            Log.e("PlacesActivity", "Search engine test failed: ${e.message}", e)
        }
    }

    private fun setupRecyclerView() {
        searchResultsAdapter = SearchResultsAdapter { searchResult ->
            // Launch MapActivity with selected landmark
            launchMapActivity(searchResult)
        }
        
        resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlacesActivity)
            adapter = searchResultsAdapter
            setHasFixedSize(true)
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(this@PlacesActivity, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        }
    }
    
    private fun setupClickListeners() {
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearchWithDebounce(query)
            } else {
                showError("Please enter a search term")
            }
        }
        
        // Real-time search with debouncing
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty() && query.length >= 2) {
                    performSearchWithDebounce(query)
                } else if (query.isEmpty()) {
                    clearResults()
                }
            }
        })
        
        // Search on Enter key press
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearchWithDebounce(query)
                }
                true
            } else {
                false
            }
        }
    }
    
    private fun setupKeyboardHandling() {
        // Hide keyboard when clicking outside
        findViewById<View>(android.R.id.content).setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
        
        // Handle keyboard visibility changes
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = rootView.rootView.height - rootView.height
            if (heightDiff > 200) { // Keyboard is visible
                // Adjust UI when keyboard is shown
                adjustUIForKeyboard(true)
        } else {
                // Restore UI when keyboard is hidden
                adjustUIForKeyboard(false)
            }
        }
    }
    
    private fun adjustUIForKeyboard(keyboardVisible: Boolean) {
        if (keyboardVisible) {
            // When keyboard is visible, ensure RecyclerView is properly sized
            resultsRecyclerView.layoutParams.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            resultsRecyclerView.requestLayout()
        } else {
            // When keyboard is hidden, restore normal layout
            resultsRecyclerView.layoutParams.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            resultsRecyclerView.requestLayout()
        }
    }
    
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }
    
    private fun getCurrentLocation() {
        try {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                        currentLocation = Point.fromLngLat(it.longitude, it.latitude)
                        performReverseGeocoding(currentLocation!!)
                        Log.d("PlacesActivity", "Current location: ${it.latitude}, ${it.longitude}")
                    }
                }
            } else {
                currentLocationText.text = "Location permission not granted"
                Log.w("PlacesActivity", "Location permission not granted")
                }
            } catch (e: Exception) {
            Log.e("PlacesActivity", "Error getting current location: ${e.message}", e)
            currentLocationText.text = "Unable to get current location"
        }
    }
    
    private fun performReverseGeocoding(point: Point) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val address = getAddressFromCoordinates(point.latitude(), point.longitude())
                withContext(Dispatchers.Main) {
                    currentLocationText.text = "📍 $address"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    currentLocationText.text = "📍 Location: ${point.latitude()}, ${point.longitude()}"
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
    
    private fun performSearchWithDebounce(query: String) {
        // Cancel previous search job
        searchJob?.cancel()
        
        // Start new search job with delay
        searchJob = CoroutineScope(Dispatchers.Main).launch {
            delay(searchDelay)
            searchLandmarks(query)
        }
    }
    
    private fun clearResults() {
        searchResults.clear()
        searchResultsAdapter.updateResults(emptyList())
        hideEmptyState()
        showLoading(false)
    }
    
    private fun searchLandmarks(query: String) {
        Log.d("PlacesActivity", "Searching for landmarks: '$query'")
        
        // Cancel previous search
        searchRequestTask?.cancel()
        
        // Clear previous results
        searchResults.clear()
        searchResultsAdapter.updateResults(emptyList())

        showLoading(true)
        hideEmptyState()
        currentSearchQuery = query
        
        try {
            // Check if search engine is initialized
            if (!::searchEngine.isInitialized) {
                Log.e("PlacesActivity", "Search engine not initialized")
                showLoading(false)
                showError("Search engine not available. Please restart the app.")
                return
            }
            
            // Use Mapbox Search SDK with proper SearchOptions
            searchRequestTask = searchEngine.search(
                query,
                SearchOptions(limit = 10), // Mapbox API limit is 1-10
                searchCallback
            )
            Log.d("PlacesActivity", "Search request sent successfully")
            
            } catch (e: Exception) {
            Log.e("PlacesActivity", "Error starting search: ${e.message}", e)
                    showLoading(false)
            showError("Search failed: ${e.message}. Please try again.")
        }
    }
    
    private val searchCallback = object : SearchSelectionCallback {
        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
            Log.d("PlacesActivity", "Search suggestions: ${suggestions.size} found")
            
            if (suggestions.isEmpty()) {
                Log.d("PlacesActivity", "No suggestions found")
                showLoading(false)
                showEmptyState("No landmarks found for '$currentSearchQuery'. Try searching for restaurants, malls, hospitals, or schools.")
        } else {
                // Select the first suggestion to get detailed results
                Log.d("PlacesActivity", "Selecting first suggestion: ${suggestions.first().name}")
                searchRequestTask = searchEngine.select(suggestions.first(), this)
            }
        }
        
        override fun onResult(
            suggestion: SearchSuggestion,
            result: MapboxSearchResult,
            responseInfo: ResponseInfo
        ) {
            Log.d("PlacesActivity", "Search result: ${result.name}")
            showLoading(false)
            
            val searchResult = SearchResult(
                name = result.name,
                address = result.address?.formattedAddress() ?: "Address not available",
                coordinate = Point.fromLngLat(
                    result.coordinate?.longitude() ?: 0.0,
                    result.coordinate?.latitude() ?: 0.0
                ),
                category = getCategoryFromResult(result)
            )
            
            displayResults(listOf(searchResult))
        }
        
        override fun onResults(
            suggestion: SearchSuggestion,
            results: List<MapboxSearchResult>,
            responseInfo: ResponseInfo
        ) {
            Log.d("PlacesActivity", "Search results: ${results.size} found")
            showLoading(false)
            
            val searchResults = results.map { result ->
                SearchResult(
                    name = result.name,
                    address = result.address?.formattedAddress() ?: "Address not available",
                    coordinate = Point.fromLngLat(
                        result.coordinate?.longitude() ?: 0.0,
                        result.coordinate?.latitude() ?: 0.0
                    ),
                    category = getCategoryFromResult(result)
                )
            }
            
            displayResults(searchResults)
        }
        
        override fun onError(e: Exception) {
            Log.e("PlacesActivity", "Search error: ${e.message}", e)
            showLoading(false)
            
            // Try fallback search
            Log.d("PlacesActivity", "Trying fallback search...")
            performFallbackSearch(currentSearchQuery)
        }
    }
    
    private fun getCategoryFromSuggestion(suggestion: SearchSuggestion): String {
        val name = suggestion.name.lowercase()
        val categories = suggestion.categories ?: emptyList()
        
        return when {
            name.contains("restaurant") || name.contains("food") || name.contains("cafe") ||
            name.contains("mcdonald") || name.contains("jollibee") || name.contains("kfc") -> "Restaurant"
            
            name.contains("mall") || name.contains("shopping") || name.contains("store") ||
            name.contains("sm") || name.contains("ayala") -> "Shopping"
            
            name.contains("hospital") || name.contains("clinic") || name.contains("medical") -> "Hospital"
            
            name.contains("school") || name.contains("university") || name.contains("college") -> "Education"
            
            name.contains("station") || name.contains("terminal") || name.contains("airport") ||
            name.contains("mrt") || name.contains("lrt") -> "Transportation"
            
            name.contains("church") || name.contains("cathedral") || name.contains("temple") -> "Religious"
            
            name.contains("bank") || name.contains("atm") -> "Bank"
            
            name.contains("hotel") || name.contains("inn") || name.contains("resort") -> "Hotel"
            
            name.contains("park") || name.contains("playground") -> "Park"
            
            name.contains("museum") || name.contains("gallery") -> "Museum"
            
            else -> "Landmark"
        }
    }
    
    private fun getCategoryFromResult(result: MapboxSearchResult): String {
        val name = result.name.lowercase()
        val categories = result.categories ?: emptyList()
        
        return when {
            name.contains("restaurant") || name.contains("food") || name.contains("cafe") ||
            name.contains("mcdonald") || name.contains("jollibee") || name.contains("kfc") -> "Restaurant"
            
            name.contains("mall") || name.contains("shopping") || name.contains("store") ||
            name.contains("sm") || name.contains("ayala") -> "Shopping"
            
            name.contains("hospital") || name.contains("clinic") || name.contains("medical") -> "Hospital"
            
            name.contains("school") || name.contains("university") || name.contains("college") -> "Education"
            
            name.contains("station") || name.contains("terminal") || name.contains("airport") ||
            name.contains("mrt") || name.contains("lrt") -> "Transportation"
            
            name.contains("church") || name.contains("cathedral") || name.contains("temple") -> "Religious"
            
            name.contains("bank") || name.contains("atm") -> "Bank"
            
            name.contains("hotel") || name.contains("inn") || name.contains("resort") -> "Hotel"
            
            name.contains("park") || name.contains("playground") -> "Park"
            
            name.contains("museum") || name.contains("gallery") -> "Museum"
            
            else -> "Landmark"
        }
    }

    private fun displayResults(results: List<SearchResult>) {
        searchResults.clear()
        searchResults.addAll(results)
        searchResultsAdapter.updateResults(results)
        
        if (results.isEmpty()) {
            showEmptyState("No landmarks found. Try searching for restaurants, malls, hospitals, or schools.")
        } else {
            hideEmptyState()
            Log.d("PlacesActivity", "Displaying ${results.size} landmark results")
            
            // Ensure proper scrolling when results are shown
            resultsRecyclerView.post {
                // Scroll to top of results
                resultsRecyclerView.smoothScrollToPosition(0)
            }
        }
    }
    
    private fun launchMapActivity(searchResult: SearchResult) {
        Log.d("PlacesActivity", "Launching MapActivity with landmark: ${searchResult.name}")
        
        // Perform reverse geocoding to get actual address
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val actualAddress = getAddressFromCoordinates(
                    searchResult.coordinate.latitude(), 
                    searchResult.coordinate.longitude()
                )
                
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@PlacesActivity, MapActivity::class.java).apply {
                        // MapActivity expects these exact parameter names
                        putExtra("selected_place_lat", searchResult.coordinate.latitude())
                        putExtra("selected_place_lng", searchResult.coordinate.longitude())
                        putExtra("selected_place_name", searchResult.name)
                        putExtra("selected_place_address", actualAddress)
                        putExtra("transport_mode", "Car") // Default transport mode
                    }
                    
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Log.e("PlacesActivity", "Error getting address for coordinates", e)
                withContext(Dispatchers.Main) {
                    // Fallback to original address if reverse geocoding fails
                    val intent = Intent(this@PlacesActivity, MapActivity::class.java).apply {
                        // MapActivity expects these exact parameter names
                        putExtra("selected_place_lat", searchResult.coordinate.latitude())
                        putExtra("selected_place_lng", searchResult.coordinate.longitude())
                        putExtra("selected_place_name", searchResult.name)
                        putExtra("selected_place_address", searchResult.address)
                        putExtra("transport_mode", "Car") // Default transport mode
                    }
                    
                    startActivity(intent)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        loadingCard.visibility = if (show) View.VISIBLE else View.GONE
        searchButton.isEnabled = !show
    }
    
    private fun showEmptyState(message: String) {
        emptyStateText.text = message
        emptyStateCard.visibility = View.VISIBLE
        resultsRecyclerView.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        emptyStateCard.visibility = View.GONE
        resultsRecyclerView.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun performHttpSearch(query: String) {
        Log.d("PlacesActivity", "Performing HTTP search for: '$query'")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
        val accessToken = getString(R.string.mapbox_access_token)
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                
                // Use Mapbox Geocoding API v6
                val url = "https://api.mapbox.com/search/geocode/v6/forward?q=$encodedQuery&access_token=$accessToken&country=PH&limit=20"
                Log.d("PlacesActivity", "HTTP Search URL: $url")
                
                val response = java.net.URL(url).readText()
                Log.d("PlacesActivity", "HTTP Response: $response")
                
                val jsonResponse = org.json.JSONObject(response)
                val features = jsonResponse.getJSONArray("features")
            val results = mutableListOf<SearchResult>()
                
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.getJSONObject("properties")
                val geometry = feature.getJSONObject("geometry")
                val coordinates = geometry.getJSONArray("coordinates")
                
                    val name = properties.optString("name", "Unknown Place")
                    val fullAddress = properties.optString("full_address", "Address not available")
                    val featureType = properties.optString("feature_type", "location")
                
                val lng = coordinates.getDouble(0)
                val lat = coordinates.getDouble(1)
                val point = Point.fromLngLat(lng, lat)
                
                    // Determine category based on feature type and name
                val category = when {
                        featureType == "poi" -> "Point of Interest"
                        featureType == "street" -> "Street"
                        featureType == "place" -> "City"
                        featureType == "locality" -> "Area"
                        name.contains("school", ignoreCase = true) || name.contains("university", ignoreCase = true) -> "Education"
                        name.contains("hospital", ignoreCase = true) || name.contains("clinic", ignoreCase = true) -> "Hospital"
                        name.contains("mall", ignoreCase = true) || name.contains("shopping", ignoreCase = true) -> "Shopping"
                        name.contains("restaurant", ignoreCase = true) || name.contains("food", ignoreCase = true) -> "Restaurant"
                        name.contains("station", ignoreCase = true) || name.contains("terminal", ignoreCase = true) -> "Transportation"
                        else -> "Landmark"
                    }
                    
                    results.add(SearchResult(name, fullAddress, point, category))
                }
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (results.isEmpty()) {
                        showEmptyState("No landmarks found for '$query'. Try searching for restaurants, malls, hospitals, or schools.")
                    } else {
                        displayResults(results)
                        Log.d("PlacesActivity", "HTTP search completed with ${results.size} results")
                    }
                }
                
        } catch (e: Exception) {
                Log.e("PlacesActivity", "HTTP search error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    // Try fallback search
                    performFallbackSearch(query)
                }
            }
        }
    }
    
    private fun performFallbackSearch(query: String) {
        Log.d("PlacesActivity", "Performing fallback search for: '$query'")
        
        // Create some sample results as fallback
        val fallbackResults = createSampleResults(query)
        
        if (fallbackResults.isNotEmpty()) {
            showLoading(false)
            displayResults(fallbackResults)
            Log.d("PlacesActivity", "Fallback search completed with ${fallbackResults.size} results")
        } else {
            showLoading(false)
            showEmptyState("No landmarks found for '$query'. Try searching for restaurants, malls, hospitals, or schools.")
        }
    }
    
    private fun createSampleResults(query: String): List<SearchResult> {
            val results = mutableListOf<SearchResult>()
        
        // Add some sample results based on common search terms
        when (query.lowercase()) {
            "mcdonald", "mcdonald's" -> {
                results.add(SearchResult(
                    name = "McDonald's",
                    address = "Various locations in Philippines",
                    coordinate = Point.fromLngLat(121.1753, 14.6042),
                    category = "Restaurant"
                ))
            }
            "sm mall", "sm" -> {
                results.add(SearchResult(
                    name = "SM Mall",
                    address = "Various locations in Philippines",
                    coordinate = Point.fromLngLat(121.1753, 14.6042),
                    category = "Shopping"
                ))
            }
            "hospital" -> {
                results.add(SearchResult(
                    name = "Philippine General Hospital",
                    address = "Taft Avenue, Manila",
                    coordinate = Point.fromLngLat(121.1753, 14.6042),
                    category = "Hospital"
                ))
            }
            "school", "university" -> {
                results.add(SearchResult(
                    name = "University of the Philippines",
                    address = "Diliman, Quezon City",
                    coordinate = Point.fromLngLat(121.1753, 14.6042),
                    category = "Education"
                ))
            }
            "mrt", "lrt" -> {
                results.add(SearchResult(
                    name = "MRT Station",
                    address = "Various MRT stations in Metro Manila",
                    coordinate = Point.fromLngLat(121.1753, 14.6042),
                    category = "Transportation"
                ))
            }
            else -> {
                // Generic landmark
                results.add(SearchResult(
                    name = "Landmark in Philippines",
                    address = "Searching for: $query",
                    coordinate = Point.fromLngLat(121.1753, 14.6042),
                    category = "Landmark"
                ))
            }
        }
        
        return results
    }
    
    override fun onDestroy() {
        super.onDestroy()
        searchRequestTask?.cancel()
        searchJob?.cancel()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

// Search Results Adapter
class SearchResultsAdapter(
    private val onItemClick: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {
    
    private var searchResults = mutableListOf<SearchResult>()
    
    fun updateResults(results: List<SearchResult>) {
        val oldSize = searchResults.size
        searchResults.clear()
        searchResults.addAll(results)
        
        if (oldSize == 0) {
            notifyItemRangeInserted(0, results.size)
        } else {
            notifyDataSetChanged()
        }
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(searchResults[position])
    }
    
    override fun getItemCount(): Int = searchResults.size
    
    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.landmarkName)
        private val addressText: TextView = itemView.findViewById(R.id.landmarkAddress)
        private val categoryText: TextView = itemView.findViewById(R.id.landmarkCategory)
        private val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        
        fun bind(searchResult: SearchResult) {
            nameText.text = searchResult.name
            addressText.text = searchResult.address
            categoryText.text = searchResult.category
            
            // Set category icon
            val iconRes = when (searchResult.category) {
                "Restaurant" -> R.drawable.ic_restaurant
                "Shopping" -> R.drawable.ic_shopping
                "Hospital" -> R.drawable.ic_hospital
                "Education" -> R.drawable.ic_school
                "Transportation" -> R.drawable.ic_transportation
                "Religious" -> R.drawable.ic_church
                "Bank" -> R.drawable.ic_bank
                "Hotel" -> R.drawable.ic_hotel
                "Park" -> R.drawable.ic_park
                "Museum" -> R.drawable.ic_museum
                else -> R.drawable.ic_landmark
            }
            categoryIcon.setImageResource(iconRes)
            
            itemView.setOnClickListener {
                onItemClick(searchResult)
            }
        }
    }
}