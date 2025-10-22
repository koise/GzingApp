package com.example.gzingapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gzingapp.adapter.NavigationRouteAdapter
import com.example.gzingapp.data.NavigationRoute
import com.example.gzingapp.repository.NavigationRouteRepository
import com.example.gzingapp.utils.AppSettings
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NavigationRoutesActivity : AppCompatActivity() {
    
    private lateinit var navigationRouteRepository: NavigationRouteRepository
    private lateinit var appSettings: AppSettings
    private lateinit var routeAdapter: NavigationRouteAdapter
    
    private var allRouteItems = mutableListOf<NavigationRoute>()
    private var filteredRouteItems = mutableListOf<NavigationRoute>()
    
    private var selectedTransportMode: String? = null
    private var isFavoriteFilter = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_routes)
        
        // Initialize components
        navigationRouteRepository = NavigationRouteRepository(this)
        appSettings = AppSettings(this)
        
        setupToolbar()
        setupRecyclerView()
        setupSearchAndFilters()
        setupFloatingActionButton()
        setupEmptyState()
        
        // Load navigation routes
        loadNavigationRoutes()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Saved Routes"
    }
    
    private fun setupRecyclerView() {
        // Setup route adapter
        routeAdapter = NavigationRouteAdapter(
            onItemClick = { route ->
                // Show route details
                showRouteDetails(route)
            },
            onNavigateAgain = { route ->
                // Start navigation from saved route
                startNavigationFromRoute(route)
            },
            onToggleFavorite = { route ->
                // Toggle favorite status
                toggleRouteFavorite(route)
            }
        )
        
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRoutes)
        
        // Setup LinearLayoutManager with improved scrolling
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        
        // Enable smooth scrolling and improve performance
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
        recyclerView.setDrawingCacheEnabled(true)
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH)
        
        // Add item decoration for spacing
        recyclerView.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(
            this, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
        ))
        
        recyclerView.adapter = routeAdapter
    }
    
    private fun setupSearchAndFilters() {
        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
        val chipGroupTransport = findViewById<ChipGroup>(R.id.chipGroupTransport)
        val chipFavorite = findViewById<Chip>(R.id.chipFavorite)
        val btnClearFilters = findViewById<MaterialButton>(R.id.btnClearFilters)
        
        // Search functionality
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilters()
            }
        })
        
        // Transport mode filter chips
        chipGroupTransport.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedTransportMode = when {
                checkedIds.contains(R.id.chipDriving) -> "driving"
                checkedIds.contains(R.id.chipWalking) -> "walking"
                checkedIds.contains(R.id.chipCycling) -> "cycling"
                else -> null
            }
            applyFilters()
        }
        
        // Favorite filter
        chipFavorite.setOnCheckedChangeListener { _, isChecked ->
            isFavoriteFilter = isChecked
            applyFilters()
        }
        
        // Clear filters
        btnClearFilters.setOnClickListener {
            clearAllFilters()
        }
    }
    
    private fun setupFloatingActionButton() {
        // FAB removed as requested - users can create routes from the map directly
        val fabAddRoute = findViewById<FloatingActionButton>(R.id.fabAddRoute)
        fabAddRoute.visibility = View.GONE
    }
    
    private fun setupEmptyState() {
        val btnStartNavigation = findViewById<MaterialButton>(R.id.btnStartNavigation)
        btnStartNavigation.setOnClickListener {
            // Navigate to MapActivity to start navigation
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        // DEBUG: Add long press to change user ID for testing
        btnStartNavigation.setOnLongClickListener {
            showDebugUserDialog()
            true
        }
    }
    
    private fun showDebugUserDialog() {
        val currentUserId = appSettings.getUserId()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Debug: Change User ID")
        builder.setMessage("Current User ID: $currentUserId\n\nSelect a user ID to test with:")
        
        val userIds = arrayOf("32", "34", "35", "36")
        builder.setItems(userIds) { _, which ->
            val selectedUserId = userIds[which].toInt()
            appSettings.setUserId(selectedUserId)
            Log.d("NavigationRoutesActivity", "Changed user ID to: $selectedUserId")
            Toast.makeText(this, "User ID changed to: $selectedUserId", Toast.LENGTH_SHORT).show()
            // Reload routes with new user ID
            loadNavigationRoutes()
        }
        
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
    
    private fun loadNavigationRoutes() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val userId = appSettings.getUserId()
                Log.d("NavigationRoutesActivity", "Loading routes for user ID: $userId")
                if (userId == null) {
                    showLoading(false)
                    showError("User not logged in")
                    return@launch
                }
                
                val result = navigationRouteRepository.getNavigationRoutes(
                    userId = userId,
                    limit = 100,
                    favoritesOnly = false  // Always fetch all routes, filter in UI
                )
                
                result.fold(
                    onSuccess = { response ->
                        allRouteItems.clear()
                        val routes = response.data?.routes ?: emptyList()
                        allRouteItems.addAll(routes)
                        
                        Log.d("NavigationRoutes", "Loaded ${routes.size} navigation routes for user $userId")
                        if (routes.isNotEmpty()) {
                            Log.d("NavigationRoutes", "First route: ${routes.first().routeName}")
                        }
                        
                        showLoading(false)
                        applyFilters() // Apply filters after loading data
                    },
                    onFailure = { error ->
                        Log.e("NavigationRoutes", "Failed to load navigation routes", error)
                        showLoading(false)
                        showError("Failed to load navigation routes: ${error.message}")
                    }
                )
                
            } catch (e: Exception) {
                showLoading(false)
                showError("Failed to load navigation routes: ${e.message}")
            }
        }
    }
    
    private fun applyFilters() {
        filteredRouteItems.clear()
        
        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
        val searchQuery = etSearch.text.toString().lowercase()
        
        filteredRouteItems.addAll(allRouteItems.filter { route ->
            // Search filter
            val matchesSearch = searchQuery.isEmpty() || 
                route.routeName.lowercase().contains(searchQuery) ||
                route.destinationName.lowercase().contains(searchQuery) ||
                route.destinationAddress?.lowercase()?.contains(searchQuery) == true
            
            // Transport mode filter
            val matchesTransport = selectedTransportMode == null || 
                route.transportMode.equals(selectedTransportMode, ignoreCase = true) ||
                (selectedTransportMode == "driving" && (route.transportMode.isNullOrEmpty() || route.transportMode == "driving"))
            
            // Favorite filter
            val matchesFavorite = !isFavoriteFilter || route.isFavorite == 1
            
            matchesSearch && matchesTransport && matchesFavorite
        })
        
        updateRoutesDisplay()
    }
    
    private fun updateRoutesDisplay() {
        routeAdapter.updateRoutes(filteredRouteItems)
        
        if (filteredRouteItems.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
        }
        
        // Update statistics for routes
        updateRouteStatistics()
    }
    
    private fun updateRouteStatistics() {
        val totalRoutes = filteredRouteItems.size
        val favoriteRoutes = filteredRouteItems.count { it.isFavorite == 1 }
        val totalDistance = filteredRouteItems.sumOf { it.routeDistance.toDoubleOrNull() ?: 0.0 }
        val totalEstimatedFare = filteredRouteItems.sumOf { route ->
            route.estimatedFare?.toDoubleOrNull() ?: calculateDefaultFare(route.routeDistance.toDoubleOrNull() ?: 0.0)
        }
        
        findViewById<TextView>(R.id.tvTotalTrips).text = totalRoutes.toString()
        findViewById<TextView>(R.id.tvSuccessfulTrips).text = favoriteRoutes.toString()
        findViewById<TextView>(R.id.tvTotalDistance).text = String.format("%.1f km", totalDistance)
        findViewById<TextView>(R.id.tvTotalFare).text = String.format("₱%.2f", totalEstimatedFare)
    }
    
    private fun clearAllFilters() {
        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
        val chipGroupTransport = findViewById<ChipGroup>(R.id.chipGroupTransport)
        val chipFavorite = findViewById<Chip>(R.id.chipFavorite)
        
        etSearch.setText("")
        chipGroupTransport.clearCheck()
        chipFavorite.isChecked = false
        
        selectedTransportMode = null
        isFavoriteFilter = false
        
        applyFilters()
    }
    
    private fun showRouteDetails(route: NavigationRoute) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_navigation_route_details, null)
        
        // Populate dialog with route data
        val distance = route.routeDistance.toDoubleOrNull() ?: 0.0
        val estimatedFare = route.estimatedFare?.toDoubleOrNull() ?: calculateDefaultFare(distance)
        val transportMode = route.transportMode.ifBlank { "driving" }
        
        dialogView.findViewById<TextView>(R.id.tvRouteName).text = route.routeName
        dialogView.findViewById<TextView>(R.id.tvDestinationName).text = route.destinationName
        dialogView.findViewById<TextView>(R.id.tvDestinationAddress).text = route.destinationAddress
        dialogView.findViewById<TextView>(R.id.tvRouteDistance).text = String.format("%.2f km", distance)
        dialogView.findViewById<TextView>(R.id.tvTransportMode).text = transportMode.replaceFirstChar { it.uppercase() }
        dialogView.findViewById<TextView>(R.id.tvEstimatedFare).text = String.format("₱%.2f", estimatedFare)
        dialogView.findViewById<TextView>(R.id.tvCreatedDate).text = formatDateTime(route.createdAt)
        
        // Set favorite icon
        val favoriteIcon = dialogView.findViewById<ImageView>(R.id.ivFavorite)
        if (route.isFavorite == 1) {
            favoriteIcon.setImageResource(R.drawable.ic_favorite)
            favoriteIcon.setColorFilter(getColor(R.color.error))
        } else {
            favoriteIcon.setImageResource(R.drawable.ic_favorite_border)
            favoriteIcon.setColorFilter(getColor(R.color.text_secondary))
        }
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        // Set up button click listeners
        dialogView.findViewById<MaterialButton>(R.id.btnNavigateAgain).setOnClickListener {
            dialog.dismiss()
            startNavigationFromRoute(route)
        }
        
        dialogView.findViewById<MaterialButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun startNavigationFromRoute(route: NavigationRoute) {
        // Navigate to MapActivity with the route coordinates and auto-start navigation
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra("selected_place_lat", route.endLatitude.toDoubleOrNull() ?: 0.0)
            putExtra("selected_place_lng", route.endLongitude.toDoubleOrNull() ?: 0.0)
            putExtra("selected_place_name", route.destinationName)
            putExtra("selected_place_address", route.destinationAddress)
            putExtra("transport_mode", route.transportMode)
            putExtra("from_saved_route", true)
            putExtra("route_id", route.id)
            putExtra("auto_start_navigation", true) // Flag to auto-start navigation
        }
        startActivity(intent)
        finish()
    }
    
    private fun toggleRouteFavorite(route: NavigationRoute) {
        lifecycleScope.launch {
            try {
                // For now, just update the local data
                val index = allRouteItems.indexOfFirst { it.id == route.id }
                if (index != -1) {
                    val updatedRoute = route.copy(isFavorite = if (route.isFavorite == 1) 0 else 1)
                    allRouteItems[index] = updatedRoute
                    applyFilters()
                    
                    val message = if (route.isFavorite == 1) "Removed from favorites" else "Added to favorites"
                    Toast.makeText(this@NavigationRoutesActivity, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NavigationRoutesActivity, "Failed to update favorite", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun formatDateTime(dateTimeString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateTimeString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateTimeString
        }
    }
    
    private fun showLoading(show: Boolean) {
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBar)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRoutes)
        
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun showEmptyState() {
        val layoutEmptyState = findViewById<View>(R.id.layoutEmptyState)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRoutes)
        val tvEmptyTitle = findViewById<TextView>(R.id.tvEmptyTitle)
        val tvEmptyDescription = findViewById<TextView>(R.id.tvEmptyDescription)
        
        // Update empty state text based on whether we have any routes at all
        if (allRouteItems.isEmpty()) {
            tvEmptyTitle.text = "No Saved Routes Yet"
            tvEmptyDescription.text = "Complete navigation journeys and save your favorite routes for quick access. Routes are automatically saved when you reach your destination."
        } else {
            tvEmptyTitle.text = "No Routes Match Your Filters"
            tvEmptyDescription.text = "Try adjusting your search terms or filters to find your saved routes."
        }
        
        layoutEmptyState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        val layoutEmptyState = findViewById<View>(R.id.layoutEmptyState)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRoutes)
        
        layoutEmptyState.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun calculateDefaultDuration(distanceKm: Double): Int {
        // Assume average speed of 30 km/h for driving
        return (distanceKm / 30.0 * 60.0).toInt()
    }
    
    private fun calculateDefaultFare(distanceKm: Double): Double {
        // 15 pesos base for first 1 kilometer, 2 pesos for exceeding kilometers
        val base = 15.0
        val extra = if (distanceKm > 1.0) (distanceKm - 1.0) * 2.0 else 0.0
        return base + extra
    }
}
