package com.example.gzingapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gzingapp.adapter.NavigationLogsAdapter
import com.example.gzingapp.data.NavigationActivityLog
import com.example.gzingapp.data.NavigationStatsData
import com.example.gzingapp.data.*
import com.example.gzingapp.repository.NavigationActivityRepository
import com.example.gzingapp.repository.NavigationApiRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class LogsActivity : AppCompatActivity() {
    
    // UI Components
    private lateinit var toolbar: Toolbar
    private lateinit var etSearch: TextInputEditText
    private lateinit var chipGroupFilters: ChipGroup
    private lateinit var chipGroupTransport: ChipGroup
    private lateinit var btnDateFrom: MaterialButton
    private lateinit var btnDateTo: MaterialButton
    private lateinit var btnApplyFilters: MaterialButton
    private lateinit var btnClearFilters: MaterialButton
    private lateinit var recyclerViewLogs: RecyclerView
    private lateinit var fabRefresh: FloatingActionButton
    private lateinit var layoutEmptyState: View
    private lateinit var layoutLoading: View
    private lateinit var tvLogsCount: TextView
    private lateinit var tvTotalNavigations: TextView
    private lateinit var tvSuccessfulNavigations: TextView
    private lateinit var tvSuccessRate: TextView
    
    // Adapter and Data
    private lateinit var logsAdapter: NavigationLogsAdapter
    private lateinit var navigationRepository: NavigationActivityRepository
    private lateinit var navigationApiRepository: NavigationApiRepository
    private var allLogs: List<NavigationActivityLog> = emptyList()
    private var filteredLogs: List<NavigationActivityLog> = emptyList()
    private var allApiLogs: List<NavigationLog> = emptyList()
    private var filteredApiLogs: List<NavigationLog> = emptyList()
    
    // Filter State
    private var selectedActivityType: String? = null
    private var selectedTransportMode: String? = null
    private var dateFrom: String? = null
    private var dateTo: String? = null
    private var searchQuery: String = ""
    
    // Date formatters
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)
        
        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupFilters()
        setupClickListeners()
        
        // Initialize repositories
        navigationRepository = NavigationActivityRepository(this)
        navigationApiRepository = NavigationApiRepository(this)
        
        // Load initial data
        loadNavigationLogs()
        loadApiNavigationLogs()
        loadStatistics()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        etSearch = findViewById(R.id.etSearch)
        chipGroupFilters = findViewById(R.id.chipGroupFilters)
        chipGroupTransport = findViewById(R.id.chipGroupTransport)
        btnDateFrom = findViewById(R.id.btnDateFrom)
        btnDateTo = findViewById(R.id.btnDateTo)
        btnApplyFilters = findViewById(R.id.btnApplyFilters)
        btnClearFilters = findViewById(R.id.btnClearFilters)
        recyclerViewLogs = findViewById(R.id.recyclerViewLogs)
        fabRefresh = findViewById(R.id.fabRefresh)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        layoutLoading = findViewById(R.id.layoutLoading)
        tvLogsCount = findViewById(R.id.tvLogsCount)
        tvTotalNavigations = findViewById(R.id.tvTotalNavigations)
        tvSuccessfulNavigations = findViewById(R.id.tvSuccessfulNavigations)
        tvSuccessRate = findViewById(R.id.tvSuccessRate)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        logsAdapter = NavigationLogsAdapter(
            logs = emptyList(),
            onLogClick = { log ->
                // Handle log click - could show details dialog
                showLogDetails(log)
            },
            onNavigateAgain = { log ->
                // Handle navigate again - go back to map with destination
                navigateToDestination(log)
            }
        )
        
        recyclerViewLogs.layoutManager = LinearLayoutManager(this)
        recyclerViewLogs.adapter = logsAdapter
    }
    
    private fun setupFilters() {
        // Setup activity type filter chips
        setupActivityTypeChips()
        
        // Setup transport mode filter chips
        setupTransportModeChips()
        
        // Setup search text watcher
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString() ?: ""
                applyFilters()
            }
        })
    }
    
    private fun setupActivityTypeChips() {
        val chips = listOf(
            findViewById<Chip>(R.id.chipAllTypes),
            findViewById<Chip>(R.id.chipNavigationStart),
            findViewById<Chip>(R.id.chipDestinationReached),
            findViewById<Chip>(R.id.chipNavigationStop)
        )
        
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Uncheck other chips in the group
                    chips.forEach { otherChip ->
                        if (otherChip != chip) {
                            otherChip.isChecked = false
                        }
                    }
                    
                    // Set selected activity type
                    selectedActivityType = when (chip.id) {
                        R.id.chipAllTypes -> null
                        R.id.chipNavigationStart -> "navigation_start"
                        R.id.chipDestinationReached -> "destination_reached"
                        R.id.chipNavigationStop -> "navigation_stop"
                        else -> null
                    }
                    
                    applyFilters()
                }
            }
        }
    }
    
    private fun setupTransportModeChips() {
        val chips = listOf(
            findViewById<Chip>(R.id.chipAllTransport),
            findViewById<Chip>(R.id.chipDriving),
            findViewById<Chip>(R.id.chipWalking),
            findViewById<Chip>(R.id.chipCycling)
        )
        
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Uncheck other chips in the group
                    chips.forEach { otherChip ->
                        if (otherChip != chip) {
                            otherChip.isChecked = false
                        }
                    }
                    
                    // Set selected transport mode
                    selectedTransportMode = when (chip.id) {
                        R.id.chipAllTransport -> null
                        R.id.chipDriving -> "driving"
                        R.id.chipWalking -> "walking"
                        R.id.chipCycling -> "cycling"
                        else -> null
                    }
                    
                    applyFilters()
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        btnDateFrom.setOnClickListener {
            showDatePicker { date ->
                dateFrom = apiDateFormat.format(date)
                btnDateFrom.text = displayDateFormat.format(date)
                applyFilters()
            }
        }
        
        btnDateTo.setOnClickListener {
            showDatePicker { date ->
                dateTo = apiDateFormat.format(date)
                btnDateTo.text = displayDateFormat.format(date)
                applyFilters()
            }
        }
        
        btnApplyFilters.setOnClickListener {
            applyFilters()
        }
        
        btnClearFilters.setOnClickListener {
            clearAllFilters()
        }
        
        fabRefresh.setOnClickListener {
            loadNavigationLogs()
            loadApiNavigationLogs()
            loadStatistics()
        }
    }
    
    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time
                onDateSelected(selectedDate)
            },
            year, month, day
        )
        
        datePickerDialog.show()
    }
    
    private fun loadNavigationLogs() {
        showLoading(true)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = navigationRepository.getUserNavigationLogs(limit = 100)
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    
                    result.onSuccess { response ->
                        // Handle old API response format
                        allLogs = when (response) {
                            is List<*> -> response as? List<NavigationActivityLog> ?: emptyList()
                            is Map<*, *> -> {
                                val dataMap = response as? Map<String, Any>
                                dataMap?.get("logs") as? List<NavigationActivityLog> ?: emptyList()
                            }
                            else -> emptyList()
                        }
                        applyFilters()
                    }.onFailure { error ->
                        showError("Failed to load navigation logs: ${error.message}")
                        showEmptyState()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Error loading navigation logs: ${e.message}")
                    showEmptyState()
                }
            }
        }
    }
    
    private fun loadApiNavigationLogs() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = navigationApiRepository.getNavigationLogs(
                    page = 1,
                    limit = 100,
                    includeDetails = true
                )
                
                withContext(Dispatchers.Main) {
                    result.onSuccess { response ->
                        android.util.Log.d("LogsActivity", "API Response received: ${response.logs?.size ?: 0} logs")
                        allApiLogs = response.logs ?: emptyList()
                        android.util.Log.d("LogsActivity", "allApiLogs size: ${allApiLogs.size}")
                        applyFilters()
                    }.onFailure { error ->
                        android.util.Log.e("LogsActivity", "Failed to load API navigation logs: ${error.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LogsActivity", "Error loading API navigation logs: ${e.message}")
            }
        }
    }
    
    private fun loadStatistics() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load statistics from both repositories
                val oldStatsResult = navigationRepository.getNavigationStats()
                val newStatsResult = navigationApiRepository.getNavigationStats()
                
                withContext(Dispatchers.Main) {
                    // Try new API first, fallback to old API
                    newStatsResult.onSuccess { response ->
                        response?.let { stats ->
                            updateApiStatistics(stats)
                        }
                    }.onFailure { error ->
                        android.util.Log.e("LogsActivity", "Failed to load new API statistics: ${error.message}")
                        
                        // Fallback to old API
                        oldStatsResult.onSuccess { response ->
                            response?.let { stats ->
                                updateApiStatistics(stats)
                            }
                        }.onFailure { error ->
                            android.util.Log.e("LogsActivity", "Failed to load old API statistics: ${error.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LogsActivity", "Error loading statistics: ${e.message}")
            }
        }
    }
    
    private fun updateStatistics(stats: NavigationStatsData) {
        tvTotalNavigations.text = stats.totalNavigations.toString()
        tvSuccessfulNavigations.text = stats.successfulNavigations.toString()
        
        val successRate = if (stats.totalNavigations > 0) {
            ((stats.successfulNavigations.toDouble() / stats.totalNavigations) * 100).toInt()
        } else {
            0
        }
        tvSuccessRate.text = "${successRate}%"
    }
    
    private fun updateApiStatistics(stats: NavigationStatsResponse) {
        val basicStats = stats.basicStats
        tvTotalNavigations.text = basicStats.totalNavigations.toString()
        tvSuccessfulNavigations.text = basicStats.successfulNavigations.toString()
        
        val successRate = if (basicStats.totalNavigations > 0) {
            ((basicStats.successfulNavigations.toDouble() / basicStats.totalNavigations) * 100).toInt()
        } else {
            0
        }
        tvSuccessRate.text = "${successRate}%"
    }
    
    private fun applyFilters() {
        // Filter old API logs
        filteredLogs = allLogs.filter { log ->
            // Search filter
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                log.destinationName?.contains(searchQuery, ignoreCase = true) == true ||
                log.destinationAddress?.contains(searchQuery, ignoreCase = true) == true
            }
            
            // Activity type filter
            val matchesActivityType = selectedActivityType?.let { type ->
                log.activityType == type
            } ?: true
            
            // Transport mode filter
            val matchesTransportMode = selectedTransportMode?.let { mode ->
                log.transportMode == mode
            } ?: true
            
            // Date range filter
            val matchesDateRange = log.createdAt?.let { dateString ->
                try {
                    // Try different date formats
                    val logDate = when {
                        dateString.contains("T") -> {
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                .parse(dateString)?.let { date ->
                                    apiDateFormat.format(date)
                                }
                        }
                        dateString.contains(" ") -> {
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .parse(dateString)?.let { date ->
                                    apiDateFormat.format(date)
                                }
                        }
                        else -> {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .parse(dateString)?.let { date ->
                                    apiDateFormat.format(date)
                                }
                        }
                    }
                    
                    val matchesFrom = dateFrom?.let { from ->
                        logDate?.let { it >= from } ?: true
                    } ?: true
                    
                    val matchesTo = dateTo?.let { to ->
                        logDate?.let { it <= to } ?: true
                    } ?: true
                    
                    matchesFrom && matchesTo
                } catch (e: Exception) {
                    true
                }
            } ?: true
            
            matchesSearch && matchesActivityType && matchesTransportMode && matchesDateRange
        }
        
        // Filter new API logs
        filteredApiLogs = allApiLogs.filter { log ->
            // Search filter
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                log.destinationName?.contains(searchQuery, ignoreCase = true) == true ||
                log.destinationAddress?.contains(searchQuery, ignoreCase = true) == true
            }
            
            // Activity type filter (map new API status to old activity types)
            val matchesActivityType = selectedActivityType?.let { type ->
                when (type) {
                    "navigation_start" -> log.activityType == "navigation_start"
                    "destination_reached" -> log.activityType == "destination_reached"
                    "navigation_stop" -> log.activityType == "navigation_stop"
                    else -> true
                }
            } ?: true
            
            // Transport mode filter
            val matchesTransportMode = selectedTransportMode?.let { mode ->
                log.transportMode == mode
            } ?: true
            
            // Date range filter
            val matchesDateRange = log.createdAt?.let { dateString ->
                try {
                    // Try different date formats
                    val logDate = when {
                        dateString.contains("T") -> {
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                .parse(dateString)?.let { date ->
                                    apiDateFormat.format(date)
                                }
                        }
                        dateString.contains(" ") -> {
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .parse(dateString)?.let { date ->
                                    apiDateFormat.format(date)
                                }
                        }
                        else -> {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .parse(dateString)?.let { date ->
                                    apiDateFormat.format(date)
                                }
                        }
                    }
                    
                    val matchesFrom = dateFrom?.let { from ->
                        logDate?.let { it >= from } ?: true
                    } ?: true
                    
                    val matchesTo = dateTo?.let { to ->
                        logDate?.let { it <= to } ?: true
                    } ?: true
                    
                    matchesFrom && matchesTo
                } catch (e: Exception) {
                    true
                }
            } ?: true
            
            matchesSearch && matchesActivityType && matchesTransportMode && matchesDateRange
        }
        
        updateLogsDisplay()
    }
    
    private fun clearAllFilters() {
        // Clear search
        etSearch.text?.clear()
        searchQuery = ""
        
        // Clear activity type filter
        findViewById<Chip>(R.id.chipAllTypes).isChecked = true
        selectedActivityType = null
        
        // Clear transport mode filter
        findViewById<Chip>(R.id.chipAllTransport).isChecked = true
        selectedTransportMode = null
        
        // Clear date filters
        dateFrom = null
        dateTo = null
        btnDateFrom.text = "From Date"
        btnDateTo.text = "To Date"
        
        // Apply filters (which will show all logs)
        applyFilters()
    }
    
    private fun updateLogsDisplay() {
        android.util.Log.d("LogsActivity", "updateLogsDisplay called - filteredLogs: ${filteredLogs.size}, filteredApiLogs: ${filteredApiLogs.size}")
        
        // Combine both old and new API logs
        val combinedLogs = filteredLogs + filteredApiLogs.map { apiLog ->
            // Convert NavigationLog to NavigationActivityLog for compatibility
            NavigationActivityLog(
                id = apiLog.id,
                userId = apiLog.userId,
                userName = "User", // Default value
                activityType = apiLog.activityType,
                startLatitude = apiLog.startLatitude?.toDoubleOrNull(),
                startLongitude = apiLog.startLongitude?.toDoubleOrNull(),
                endLatitude = apiLog.endLatitude?.toDoubleOrNull(),
                endLongitude = apiLog.endLongitude?.toDoubleOrNull(),
                destinationName = apiLog.destinationName,
                destinationAddress = apiLog.destinationAddress,
                routeDistance = apiLog.routeDistance?.toDoubleOrNull(), // Convert string to double
                navigationDuration = apiLog.actualDuration,
                transportMode = apiLog.transportMode,
                destinationReached = apiLog.destinationReached == 1, // Convert int to boolean
                createdAt = apiLog.createdAt,
                updatedAt = apiLog.updatedAt,
                deviceInfo = null // DeviceInfo not available in new API format
            )
        }
        
        android.util.Log.d("LogsActivity", "Combined logs size: ${combinedLogs.size}")
        
        logsAdapter.updateLogs(combinedLogs)
        tvLogsCount.text = "${combinedLogs.size} logs"
        
        if (combinedLogs.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
        }
    }
    
    private fun showLoading(show: Boolean) {
        layoutLoading.visibility = if (show) View.VISIBLE else View.GONE
        recyclerViewLogs.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    private fun showEmptyState() {
        layoutEmptyState.visibility = View.VISIBLE
        recyclerViewLogs.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        layoutEmptyState.visibility = View.GONE
        recyclerViewLogs.visibility = View.VISIBLE
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun showLogDetails(log: NavigationActivityLog) {
        // Create a simple dialog to show log details
        val details = buildString {
            appendLine("Activity: ${log.activityType}")
            appendLine("Destination: ${log.destinationName ?: "Unknown"}")
            appendLine("Address: ${log.destinationAddress ?: "Unknown"}")
            appendLine("Transport: ${log.transportMode ?: "Unknown"}")
            appendLine("Distance: ${log.routeDistance?.let { "%.1f km".format(it) } ?: "N/A"}")
            appendLine("Duration: ${log.navigationDuration?.let { "$it min" } ?: "N/A"}")
            appendLine("Reached: ${if (log.destinationReached) "Yes" else "No"}")
            appendLine("Date: ${log.createdAt ?: "Unknown"}")
            
            log.deviceInfo?.let { device ->
                appendLine("Battery: ${device.batteryLevel ?: "N/A"}%")
                appendLine("Network: ${device.networkType ?: "N/A"}")
            }
        }
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Navigation Log Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun navigateToDestination(log: NavigationActivityLog) {
        // Navigate back to MapActivity with the destination coordinates
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra("selected_place_lat", log.endLatitude ?: log.startLatitude)
            putExtra("selected_place_lng", log.endLongitude ?: log.startLongitude)
            putExtra("selected_place_name", log.destinationName)
            putExtra("selected_place_address", log.destinationAddress)
        }
        startActivity(intent)
        finish()
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
    }
}
