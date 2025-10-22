package com.example.gzingapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.gzingapp.adapter.NavigationHistoryAdapter
import com.example.gzingapp.adapter.NavigationRouteAdapter
import com.example.gzingapp.data.NavigationHistory
import com.example.gzingapp.data.NavigationRoute
import com.example.gzingapp.repository.NavigationHistoryRepository
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

class NavigationHistoryActivity : AppCompatActivity() {
    
    private lateinit var navigationHistoryRepository: NavigationHistoryRepository
    private lateinit var navigationRouteRepository: NavigationRouteRepository
    private lateinit var appSettings: AppSettings
    private lateinit var historyAdapter: NavigationHistoryAdapter
    private lateinit var routeAdapter: NavigationRouteAdapter
    
    private var allHistoryItems = mutableListOf<NavigationHistory>()
    private var filteredHistoryItems = mutableListOf<NavigationHistory>()
    private var allRouteItems = mutableListOf<NavigationRoute>()
    private var filteredRouteItems = mutableListOf<NavigationRoute>()
    
    private var currentTab = "history" // "history" or "routes"
    
    private var selectedTransportMode: String? = null
    private var selectedDateFrom: String? = null
    private var selectedDateTo: String? = null
    private var isFavoriteFilter = false
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_history)
        
        // Initialize components
        navigationHistoryRepository = NavigationHistoryRepository(this)
        navigationRouteRepository = NavigationRouteRepository(this)
        appSettings = AppSettings(this)
        
        setupToolbar()
        setupTabs()
        setupRecyclerView()
        setupSearchAndFilters()
        setupFloatingActionButton()
        setupEmptyState()
        
        // Load navigation history
        loadNavigationHistory()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Navigation History"
    }
    
    private fun setupTabs() {
        val btnHistoryTab = findViewById<MaterialButton>(R.id.btnHistoryTab)
        val btnRoutesTab = findViewById<MaterialButton>(R.id.btnRoutesTab)
        
        // Set initial state
        updateTabButtons()
        
        btnHistoryTab.setOnClickListener {
            currentTab = "history"
            updateTabButtons()
            updateDisplay()
        }
        
        btnRoutesTab.setOnClickListener {
            currentTab = "routes"
            updateTabButtons()
            updateDisplay()
            // Load routes if not already loaded
            if (allRouteItems.isEmpty()) {
                loadNavigationRoutes()
            }
        }
    }
    
    private fun updateTabButtons() {
        val btnHistoryTab = findViewById<MaterialButton>(R.id.btnHistoryTab)
        val btnRoutesTab = findViewById<MaterialButton>(R.id.btnRoutesTab)
        
        if (currentTab == "history") {
            btnHistoryTab.isSelected = true
            btnRoutesTab.isSelected = false
            supportActionBar?.title = "Navigation History"
        } else {
            btnHistoryTab.isSelected = false
            btnRoutesTab.isSelected = true
            supportActionBar?.title = "Saved Routes"
        }
    }
    
    private fun setupRecyclerView() {
        // Setup history adapter
        historyAdapter = NavigationHistoryAdapter(
            onItemClick = { history ->
                // Navigate to route details or show history details
                showHistoryDetails(history)
            },
            onNavigateAgain = { history ->
                // Start navigation to this destination
                startNavigationToDestination(history)
            },
            onToggleFavorite = { history ->
                // Toggle favorite status
                toggleFavorite(history)
            }
        )
        
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
        
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHistory)
        
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
        
        // Set initial adapter
        recyclerView.adapter = historyAdapter
    }
    
    private fun setupSearchAndFilters() {
        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
        val chipGroupTransport = findViewById<ChipGroup>(R.id.chipGroupTransport)
        val btnDateFilter = findViewById<MaterialButton>(R.id.btnDateFilter)
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
        
        // Date range filter
        btnDateFilter.setOnClickListener {
            showDateRangePicker()
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
        val fabAddHistory = findViewById<FloatingActionButton>(R.id.fabAddHistory)
        fabAddHistory.setOnClickListener {
            // For now, just show a toast - could navigate to create new history
            Toast.makeText(this, "Add new navigation history", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupEmptyState() {
        val btnStartNavigation = findViewById<MaterialButton>(R.id.btnStartNavigation)
        btnStartNavigation.setOnClickListener {
            // Navigate to RoutesMapsActivity to start navigation
            val intent = Intent(this, RoutesMapsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun loadNavigationHistory() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val userId = appSettings.getUserId()
                if (userId == null) {
                    showLoading(false)
                    showError("User not logged in")
                    return@launch
                }
                
                val result = navigationHistoryRepository.getNavigationHistory(
                    page = 1,
                    limit = 100,
                    transportMode = selectedTransportMode,
                    isFavorite = if (isFavoriteFilter) true else null,
                    dateFrom = selectedDateFrom,
                    dateTo = selectedDateTo
                )
                
                result.fold(
                    onSuccess = { data ->
                        allHistoryItems.clear()
                        allHistoryItems.addAll(data.history ?: emptyList())
                        showLoading(false)
                        applyFilters() // Apply filters after loading data
                    },
                    onFailure = { error ->
                        showLoading(false)
                        showError("Failed to load navigation history: ${error.message}")
                    }
                )
                
            } catch (e: Exception) {
                showLoading(false)
                showError("Failed to load navigation history: ${e.message}")
            }
        }
    }
    
    private fun loadNavigationRoutes() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val userId = appSettings.getUserId()
                if (userId == null) {
                    showLoading(false)
                    showError("User not logged in")
                    return@launch
                }
                
                val result = navigationRouteRepository.getNavigationRoutes(
                    userId = userId,
                    limit = 100,
                    favoritesOnly = isFavoriteFilter
                )
                
                result.fold(
                    onSuccess = { response ->
                        allRouteItems.clear()
                        allRouteItems.addAll(response.data?.routes ?: emptyList())
                        showLoading(false)
                        applyFilters() // Apply filters after loading data
                    },
                    onFailure = { error ->
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
    
    private fun updateDisplay() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHistory)
        
        if (currentTab == "history") {
            recyclerView.adapter = historyAdapter
            updateHistoryDisplay()
        } else {
            recyclerView.adapter = routeAdapter
            updateRoutesDisplay()
        }
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
        
        findViewById<android.widget.TextView>(R.id.tvTotalTrips).text = totalRoutes.toString()
        findViewById<android.widget.TextView>(R.id.tvSuccessfulTrips).text = favoriteRoutes.toString()
        findViewById<android.widget.TextView>(R.id.tvTotalDistance).text = String.format("%.1f km", totalDistance)
        findViewById<android.widget.TextView>(R.id.tvTotalFare).text = String.format("₱%.2f", totalEstimatedFare)
    }
    
    
    private fun applyFilters() {
        if (currentTab == "history") {
            applyHistoryFilters()
        } else {
            applyRouteFilters()
        }
    }
    
    private fun applyHistoryFilters() {
        filteredHistoryItems.clear()
        
        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
        val searchQuery = etSearch.text.toString().lowercase()
        
        filteredHistoryItems.addAll(allHistoryItems.filter { history ->
            // Search filter
            val matchesSearch = searchQuery.isEmpty() || 
                history.destinationName.lowercase().contains(searchQuery) ||
                history.destinationAddress?.lowercase()?.contains(searchQuery) == true
            
            // Transport mode filter
            val matchesTransport = selectedTransportMode == null || 
                history.transportMode == selectedTransportMode
            
            // Favorite filter
            val matchesFavorite = !isFavoriteFilter || history.isFavorite
            
            // Date range filter (simplified - just check if dates are set)
            val matchesDate = selectedDateFrom == null || selectedDateTo == null ||
                isDateInRange(history.startTime, selectedDateFrom, selectedDateTo)
            
            matchesSearch && matchesTransport && matchesFavorite && matchesDate
        })
        
        updateHistoryDisplay()
    }
    
    private fun applyRouteFilters() {
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
                route.transportMode == selectedTransportMode
            
            // Favorite filter
            val matchesFavorite = !isFavoriteFilter || route.isFavorite == 1
            
            matchesSearch && matchesTransport && matchesFavorite
        })
        
        updateRoutesDisplay()
    }
    
    private fun isDateInRange(dateString: String, fromDate: String?, toDate: String?): Boolean {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString)
            val from = fromDate?.let { dateFormat.parse(it) }
            val to = toDate?.let { dateFormat.parse(it) }
            
            when {
                from != null && to != null -> date != null && date >= from && date <= to
                from != null -> date != null && date >= from
                to != null -> date != null && date <= to
                else -> true
            }
        } catch (e: Exception) {
            true // If parsing fails, include the item
        }
    }
    
    private fun clearAllFilters() {
        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
        val chipGroupTransport = findViewById<ChipGroup>(R.id.chipGroupTransport)
        val chipFavorite = findViewById<Chip>(R.id.chipFavorite)
        val btnDateFilter = findViewById<MaterialButton>(R.id.btnDateFilter)
        
        etSearch.setText("")
        chipGroupTransport.clearCheck()
        chipFavorite.isChecked = false
        btnDateFilter.text = "Select Date Range"
        
        selectedTransportMode = null
        selectedDateFrom = null
        selectedDateTo = null
        isFavoriteFilter = false
        
        applyFilters()
    }
    
    private fun showRouteDetails(route: NavigationRoute) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_navigation_route_details, null)
        
        // Populate dialog with route data
        val distance = route.routeDistance.toDoubleOrNull() ?: 0.0
        val duration = route.estimatedDuration ?: calculateDefaultDuration(distance)
        val estimatedFare = route.estimatedFare?.toDoubleOrNull() ?: calculateDefaultFare(distance)
        val transportMode = route.transportMode.ifBlank { "driving" }
        
        dialogView.findViewById<TextView>(R.id.tvRouteName).text = route.routeName
        dialogView.findViewById<TextView>(R.id.tvDestinationName).text = route.destinationName
        dialogView.findViewById<TextView>(R.id.tvDestinationAddress).text = route.destinationAddress
        dialogView.findViewById<TextView>(R.id.tvRouteDistance).text = String.format("%.2f km", distance)
        dialogView.findViewById<TextView>(R.id.tvDuration).text = "${duration} min"
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
        // Navigate to MapActivity with the route coordinates
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra("selected_place_lat", route.endLatitude.toDoubleOrNull() ?: 0.0)
            putExtra("selected_place_lng", route.endLongitude.toDoubleOrNull() ?: 0.0)
            putExtra("selected_place_name", route.destinationName)
            putExtra("selected_place_address", route.destinationAddress)
            putExtra("transport_mode", route.transportMode)
            putExtra("from_saved_route", true)
            putExtra("route_id", route.id)
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
                    Toast.makeText(this@NavigationHistoryActivity, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NavigationHistoryActivity, "Failed to update favorite", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                
                selectedDateFrom = dateFormat.format(selectedDate.time)
                val btnDateFilter = findViewById<MaterialButton>(R.id.btnDateFilter)
                btnDateFilter.text = "From: ${displayDateFormat.format(selectedDate.time)}"
                applyFilters()
            },
            year, month, day
        ).show()
    }
    
    private fun updateHistoryDisplay() {
        historyAdapter.updateHistory(filteredHistoryItems)
        
        if (filteredHistoryItems.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
        }
        
        // Update statistics
        updateStatistics()
    }
    
    private fun updateStatistics() {
        val totalTrips = filteredHistoryItems.size
        val successfulTrips = filteredHistoryItems.count { it.successRate >= 100.0 }
        val totalDistance = filteredHistoryItems.sumOf { it.routeDistance }
        val totalFare = filteredHistoryItems.sumOf { it.actualFare ?: 0.0 }
        
        findViewById<android.widget.TextView>(R.id.tvTotalTrips).text = totalTrips.toString()
        findViewById<android.widget.TextView>(R.id.tvSuccessfulTrips).text = successfulTrips.toString()
        findViewById<android.widget.TextView>(R.id.tvTotalDistance).text = String.format("%.1f km", totalDistance)
        findViewById<android.widget.TextView>(R.id.tvTotalFare).text = String.format("₱%.2f", totalFare)
    }
    
    private fun showHistoryDetails(history: NavigationHistory) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_navigation_history_details, null)
        
        // Populate dialog with history data
        dialogView.findViewById<TextView>(R.id.tvDestinationName).text = history.destinationName
        dialogView.findViewById<TextView>(R.id.tvDestinationAddress).text = history.destinationAddress
        dialogView.findViewById<TextView>(R.id.tvRouteDistance).text = String.format("%.2f km", history.routeDistance)
        dialogView.findViewById<TextView>(R.id.tvDuration).text = "${history.actualDuration} min"
        dialogView.findViewById<TextView>(R.id.tvTransportMode).text = history.transportMode.replaceFirstChar { it.uppercase() }
        // dialogView.findViewById<TextView>(R.id.tvWaypointsCount).text = history.waypointsCount.toString()
        dialogView.findViewById<TextView>(R.id.tvEstimatedFare).text = String.format("₱%.2f", history.estimatedFare)
        // dialogView.findViewById<TextView>(R.id.tvActualFare).text = String.format("₱%.2f", history.actualFare)
        // dialogView.findViewById<TextView>(R.id.tvSuccessRate).text = String.format("%.1f%%", history.successRate)
        // dialogView.findViewById<TextView>(R.id.tvAverageSpeed).text = String.format("%.1f km/h", history.averageSpeed)
        // dialogView.findViewById<TextView>(R.id.tvTrafficCondition).text = history.trafficCondition
        // dialogView.findViewById<TextView>(R.id.tvRouteQuality).text = history.routeQuality.replaceFirstChar { it.uppercase() }
        dialogView.findViewById<TextView>(R.id.tvStartTime).text = formatDateTime(history.startTime)
        dialogView.findViewById<TextView>(R.id.tvEndTime).text = formatDateTime(history.endTime)
        dialogView.findViewById<TextView>(R.id.tvCompletionTime).text = formatDateTime(history.completionTime ?: history.endTime)
        
        // Set notes
        // val notes = history.notes ?: "No notes available"
        // dialogView.findViewById<TextView>(R.id.tvNotes).text = notes
        
        // Set favorite icon
        val favoriteIcon = dialogView.findViewById<ImageView>(R.id.ivFavorite)
        if (history.isFavorite) {
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
            startNavigationToDestination(history)
        }
        
        dialogView.findViewById<MaterialButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun formatDateTime(dateTimeString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateTimeString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateTimeString
        }
    }
    
    private fun startNavigationToDestination(history: NavigationHistory) {
        // Navigate to MapActivity with the destination coordinates from navigation history
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra("selected_place_lat", history.endLatitude)
            putExtra("selected_place_lng", history.endLongitude)
            putExtra("selected_place_name", history.destinationName)
            putExtra("selected_place_address", history.destinationAddress)
            putExtra("transport_mode", history.transportMode)
            putExtra("from_navigation_history", true)
            putExtra("navigation_history_id", history.id)
        }
        startActivity(intent)
        finish()
    }
    
    private fun toggleFavorite(history: NavigationHistory) {
        lifecycleScope.launch {
            try {
                // For now, just update the local data
                val index = allHistoryItems.indexOfFirst { it.id == history.id }
                if (index != -1) {
                    allHistoryItems[index] = history.copy(isFavorite = !history.isFavorite)
                    applyFilters()
                    
                    val message = if (history.isFavorite) "Removed from favorites" else "Added to favorites"
                    Toast.makeText(this@NavigationHistoryActivity, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NavigationHistoryActivity, "Failed to update favorite", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBar)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHistory)
        
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun showEmptyState() {
        val layoutEmptyState = findViewById<View>(R.id.layoutEmptyState)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHistory)
        
        // Update empty state text based on current tab
        val emptyTitle = findViewById<TextView>(R.id.tvEmptyTitle)
        val emptyDescription = findViewById<TextView>(R.id.tvEmptyDescription)
        
        if (currentTab == "routes") {
            emptyTitle?.text = "No Saved Routes Yet"
            emptyDescription?.text = "Complete navigation journeys and save your favorite routes for quick access"
        } else {
            emptyTitle?.text = "No Completed Navigation Yet"
            emptyDescription?.text = "Start your first navigation journey and your completed trips will appear here"
        }
        
        layoutEmptyState.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        val layoutEmptyState = findViewById<View>(R.id.layoutEmptyState)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHistory)
        
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