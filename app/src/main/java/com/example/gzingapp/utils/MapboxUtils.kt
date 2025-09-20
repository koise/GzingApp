package com.example.gzingapp.utils

import android.content.Context
import com.mapbox.common.MapboxOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.geojson.Point

/**
 * Utility class for Mapbox configuration and common operations
 */
object MapboxUtils {
    
    /**
     * Initialize Mapbox with access token
     */
    fun initializeMapbox(context: Context, accessToken: String) {
        MapboxOptions.accessToken = accessToken
    }
    
    /**
     * Default camera position for Manila, Philippines
     */
    fun getDefaultCameraPosition() = com.mapbox.maps.CameraOptions.Builder()
        .center(Point.fromLngLat(120.9842, 14.5995))
        .zoom(12.0)
        .build()
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermissions(context: Context): Boolean {
        return android.content.pm.PackageManager.PERMISSION_GRANTED == 
            context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }
    
    /**
     * Get default map style
     */
    fun getDefaultMapStyle(): String {
        return Style.MAPBOX_STREETS
    }
    
    /**
     * Get satellite map style
     */
    fun getSatelliteMapStyle(): String {
        return Style.SATELLITE_STREETS
    }
    
    /**
     * Get dark map style
     */
    fun getDarkMapStyle(): String {
        return Style.DARK
    }
    
    /**
     * Get light map style
     */
    fun getLightMapStyle(): String {
        return Style.LIGHT
    }
}