package com.example.gzingapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContextWrapper
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.w("GeofenceReceiver", "Received null geofencing event")
            return
        }
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Toast.makeText(context, "Geofence error: $errorMessage", Toast.LENGTH_SHORT).show()
            return
        }
        
        val geofenceTransition = geofencingEvent.geofenceTransition
        
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                try {
                    val ids = geofencingEvent.triggeringGeofences?.joinToString { it.requestId } ?: "(none)"
                    Log.d("GeofenceReceiver", "ENTER geofence(s): $ids at ${System.currentTimeMillis()}")
                } catch (_: Exception) { }
                // Start looping alarm
                try {
                    val svc = Intent(context, AlarmSoundService::class.java)
                    androidx.core.content.ContextCompat.startForegroundService(context, svc)
                } catch (_: Exception) { }
                // Arrived/near destination notification with stop action
                sendArrivedNotification(context)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                try {
                    val ids = geofencingEvent.triggeringGeofences?.joinToString { it.requestId } ?: "(none)"
                    Log.d("GeofenceReceiver", "EXIT geofence(s): $ids at ${System.currentTimeMillis()}")
                } catch (_: Exception) { }
                sendGeofenceNotification(context, "Exited geofence", "You have left the geofence area")
            }
            else -> {
                Toast.makeText(context, "Unknown geofence transition: $geofenceTransition", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun sendGeofenceNotification(context: Context, title: String, text: String) {
        val channelId = "gzing_geofence_channel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = nm.getNotificationChannel(channelId)
            if (existing == null) {
                val ch = NotificationChannel(channelId, "Geofence", NotificationManager.IMPORTANCE_DEFAULT)
                ch.description = "Geofence alerts"
                nm.createNotificationChannel(ch)
            }
        }
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.gzingapp.R.drawable.ic_location)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify((System.currentTimeMillis() % 100000).toInt(), notif)
    }

    private fun sendArrivedNotification(context: Context) {
        val channelId = "gzing_geofence_channel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = nm.getNotificationChannel(channelId)
            if (existing == null) {
                val ch = NotificationChannel(channelId, "Geofence", NotificationManager.IMPORTANCE_HIGH)
                ch.description = "Geofence alerts"
                nm.createNotificationChannel(ch)
            }
        }
        // Action to stop navigation
        val stopIntent = Intent(context, StopNavigationReceiver::class.java).apply {
            action = StopNavigationReceiver.ACTION_STOP_NAV
        }
        val stopPi = PendingIntent.getBroadcast(context, 2201, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.gzingapp.R.drawable.ic_navigation)
            .setContentTitle("You are near your destination")
            .setContentText("Tap Stop to end navigation")
            .addAction(com.example.gzingapp.R.drawable.ic_close, "Stop", stopPi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify((System.currentTimeMillis() % 100000).toInt(), notif)
    }
}
