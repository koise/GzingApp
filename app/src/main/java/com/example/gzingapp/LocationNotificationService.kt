package com.example.gzingapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class LocationNotificationService : Service() {

    private val channelId = "gzing_location_channel"
    private val notificationId = 101
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            requestSingleLocationUpdate()
            handler.postDelayed(this, 3000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000L)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                updateNotificationWithLocation(loc)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val notif = buildNotification("Getting location...", "Please wait")
            startForeground(notificationId, notif)
            handler.post(updateRunnable)
            return START_STICKY
        } catch (e: Exception) {
            Log.e("LocationNotificationService", "Failed to start foreground service", e)
            // If foreground service fails, just run in background
            handler.post(updateRunnable)
            return START_STICKY
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun requestSingleLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun updateNotificationWithLocation(location: Location) {
        val lat = location.latitude
        val lon = location.longitude
        CoroutineScope(Dispatchers.IO).launch {
            val address = reverseGeocode(lat, lon)
            withContext(Dispatchers.Main) {
                val title = "You are currently at:"
                val body = "($lat, $lon)\n$address"
                val notif = buildNotification(title, body)
                NotificationManagerCompat.from(this@LocationNotificationService).notify(notificationId, notif)
            }
        }
    }

    private fun buildNotification(title: String, body: String): Notification {
        val intent = Intent(this, MapActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_location)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pi)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Location Updates", NotificationManager.IMPORTANCE_LOW)
            channel.description = "Background location updates"
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private suspend fun reverseGeocode(lat: Double, lon: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=18&addressdetails=1"
                val conn = URL(url).openConnection()
                conn.setRequestProperty("User-Agent", "GzingApp/1.0")
                val resp = conn.getInputStream().bufferedReader().use { it.readText() }
                val json = JSONObject(resp)
                val display = json.optString("display_name", "")
                if (display.isNotEmpty()) display else "Getting address..."
            } catch (_: Exception) {
                "Getting address..."
            }
        }
    }
}


