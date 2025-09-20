// MapActivity.kt - Save Route Dialog Implementation
// This file shows the modifications needed to add a save route dialog after stopping the alarm

// Add these imports at the top of MapActivity.kt
import android.app.AlertDialog
import android.widget.EditText
import android.widget.CheckBox
import android.widget.RatingBar
import android.widget.Toast
import com.example.gzingapp.data.CreateNavigationRouteRequest
import com.example.gzingapp.repository.NavigationRouteRepository

// Add these properties to the MapActivity class
private lateinit var navigationRouteRepository: NavigationRouteRepository

// Add this method to initialize the repository in onCreate()
private fun initializeRepositories() {
    navigationHistoryRepository = com.example.gzingapp.repository.NavigationHistoryRepository(this)
    routesRepository = com.example.gzingapp.repository.RoutesRepository(this)
    navigationRouteRepository = NavigationRouteRepository(this) // Add this line
}

// Modify the checkArrivalProximity method to show save dialog after alarm
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

// Add this new method to show the save route dialog
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

// Add this new method to save the navigation route
private fun saveNavigationRoute(
    routeName: String,
    routeDescription: String,
    isFavorite: Boolean,
    userRating: Int
) {
    val currentLoc = currentLocation
    val destLoc = pinnedLocation
    
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
            val distance = haversineMeters(
                currentLoc.latitude(), currentLoc.longitude(),
                destLoc.latitude(), destLoc.longitude()
            ) / 1000.0 // Convert to kilometers
            
            val estimatedDuration = estimateEtaMinutes(distance * 1000, selectedTransportMode).toInt()
            val averageSpeed = calculateAverageSpeed(distance, estimatedDuration)
            
            // Prepare route coordinates (simplified - just start and end points)
            val routeCoordinates = listOf(
                mapOf("lat" to currentLoc.latitude(), "lng" to currentLoc.longitude()),
                mapOf("lat" to destLoc.latitude(), "lng" to destLoc.longitude())
            )
            
            val request = CreateNavigationRouteRequest(
                userId = com.example.gzingapp.utils.AppSettings(this@MapActivity).getUserId() ?: 0,
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
            
            Log.d("SaveRoute", "Saving navigation route: $routeName")
            
            val result = navigationRouteRepository.createNavigationRoute(request)
            
            result.onSuccess { response ->
                Log.d("SaveRoute", "Route saved successfully: ${response.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MapActivity,
                        "Route '$routeName' saved successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }.onFailure { error ->
                Log.e("SaveRoute", "Failed to save route: ${error.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MapActivity,
                        "Failed to save route: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            
        } catch (e: Exception) {
            Log.e("SaveRoute", "Error saving route", e)
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

// Also modify the checkRealTimeProximity method to show the dialog
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

