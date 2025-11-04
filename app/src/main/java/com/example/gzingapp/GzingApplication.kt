package com.example.gzingapp

import android.app.Application
import com.example.gzingapp.network.RetrofitClient
import com.mapbox.common.MapboxOptions

class GzingApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Mapbox with access token
        try {
            val mapboxToken = getString(R.string.mapbox_access_token)
            if (mapboxToken.isNotEmpty() && !mapboxToken.contains("YOUR_MAPBOX_ACCESS_TOKEN")) {
                MapboxOptions.accessToken = mapboxToken
                android.util.Log.d("GzingApplication", "✅ Mapbox initialized successfully")
                android.util.Log.d("GzingApplication", "Token length: ${mapboxToken.length}")
                android.util.Log.d("GzingApplication", "Token preview: ${mapboxToken.take(10)}...")
                android.util.Log.d("GzingApplication", "✅ Mapbox Navigation SDK will be initialized in MapActivity")
            } else {
                android.util.Log.e("GzingApplication", "❌ Mapbox token is empty or placeholder!")
                android.util.Log.e("GzingApplication", "Please set a valid Mapbox access token in res/values/mapbox_config.xml")
            }
        } catch (e: Exception) {
            android.util.Log.e("GzingApplication", "❌ Error initializing Mapbox: ${e.message}", e)
        }
        
        // Initialize RetrofitClient with application context
        RetrofitClient.initialize(this)
    }
}

