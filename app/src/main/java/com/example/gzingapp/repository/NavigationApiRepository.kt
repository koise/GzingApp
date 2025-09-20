package com.example.gzingapp.repository

import android.content.Context
import android.util.Log
import com.example.gzingapp.data.*
import com.example.gzingapp.network.ApiService
import com.example.gzingapp.network.RetrofitClient
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NavigationApiRepository(private val context: Context) {
    
    private val apiService: ApiService = RetrofitClient.apiService
    private val appSettings = AppSettings(context)
    
    companion object {
        private const val TAG = "NavigationApiRepository"
    }
    
    /**
     * Create a new navigation log
     */
    suspend fun createNavigationLog(request: CreateNewNavigationLogRequest): Result<NavigationLogResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating navigation log: ${request.activityType}")
                
                val response = apiService.createNewNavigationLog(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Navigation log created successfully: ${body.data?.logId}")
                        Result.success(body.data!!)
                    } else {
                        Log.e(TAG, "API returned error: ${body.message}")
                        Result.failure(Exception(body.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating navigation log", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get navigation logs with filtering and pagination
     */
    suspend fun getNavigationLogs(
        page: Int = 1,
        limit: Int = 20,
        activityType: String? = null,
        transportMode: String? = null,
        destinationReached: Boolean? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        includeDetails: Boolean = false
    ): Result<NavigationLogsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting navigation logs: page=$page, limit=$limit")
                
                val response = apiService.getNavigationLogs(
                    page = page,
                    limit = limit,
                    activityType = activityType,
                    transportMode = transportMode,
                    destinationReached = destinationReached,
                    dateFrom = dateFrom,
                    dateTo = dateTo,
                    includeDetails = includeDetails
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Navigation logs retrieved: ${body.data?.logs?.size} logs")
                        Result.success(body.data!!)
                    } else {
                        Log.e(TAG, "API returned error: ${body.message}")
                        Result.failure(Exception(body.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting navigation logs", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get navigation statistics
     */
    suspend fun getNavigationStats(
        period: String = "all",
        includeDestinations: Boolean = true,
        includeTransportModes: Boolean = true
    ): Result<NavigationStatsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting navigation stats: period=$period")
                
                val response = apiService.getNavigationStats(
                    period = period,
                    includeDestinations = includeDestinations,
                    includeTransportModes = includeTransportModes
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Navigation stats retrieved successfully")
                        Result.success(body.data!!)
                    } else {
                        Log.e(TAG, "API returned error: ${body.message}")
                        Result.failure(Exception(body.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting navigation stats", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get detailed navigation log
     */
    suspend fun getNavigationLogDetail(logId: Int): Result<NavigationLogDetailResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting navigation log detail: $logId")
                
                val response = apiService.getNavigationLogDetail(logId)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Navigation log detail retrieved successfully")
                        Result.success(body.data!!)
                    } else {
                        Log.e(TAG, "API returned error: ${body.message}")
                        Result.failure(Exception(body.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting navigation log detail", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Update navigation log
     */
    suspend fun updateNavigationLog(logId: Int, request: UpdateNavigationLogRequest): Result<NavigationLogResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Updating navigation log: $logId")
                Log.d(TAG, "Request data: $request")
                
                val response = apiService.updateNavigationLog(logId, request)
                
                Log.d(TAG, "Update response - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d(TAG, "Response body: $body")
                    if (body.success) {
                        Log.d(TAG, "Navigation log updated successfully")
                        Result.success(body.data!!)
                    } else {
                        Log.e(TAG, "API returned error: ${body.message}")
                        Result.failure(Exception(body.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error body: $errorBody")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating navigation log", e)
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
    
    /**
     * Stop navigation
     */
    suspend fun stopNavigation(request: StopNavigationRequest): Result<StopNavigationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Stopping navigation: ${request.logId}")
                
                val response = apiService.stopNavigation(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Navigation stopped successfully")
                        Result.success(body.data!!)
                    } else {
                        Log.e(TAG, "API returned error: ${body.message}")
                        Result.failure(Exception(body.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping navigation", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Helper method to create a navigation start log
     */
    suspend fun logNavigationStart(
        startLatitude: Double,
        startLongitude: Double,
        destinationName: String,
        destinationAddress: String? = null,
        routeDistance: Double? = null,
        estimatedDuration: Int? = null,
        transportMode: String = "driving",
        waypoints: List<Waypoint>? = null,
        routePolylines: List<RoutePolyline>? = null,
        trafficData: TrafficData? = null
    ): Result<NavigationLogResponse> {
        val request = CreateNewNavigationLogRequest(
            activityType = "navigation_start",
            startLatitude = startLatitude,
            startLongitude = startLongitude,
            destinationName = destinationName,
            destinationAddress = destinationAddress,
            routeDistance = routeDistance,
            estimatedDuration = estimatedDuration,
            transportMode = transportMode,
            deviceModel = android.os.Build.MODEL,
            deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ),
            batteryLevel = getBatteryLevel(),
            networkType = getNetworkType(),
            gpsAccuracy = "high",
            screenResolution = getScreenResolution(),
            availableStorage = getAvailableStorage(),
            appVersion = getAppVersion(),
            osVersion = android.os.Build.VERSION.RELEASE,
            waypoints = waypoints,
            routePolylines = routePolylines,
            trafficData = trafficData,
            navigationEvents = listOf(
                NavigationEvent(
                    type = "navigation_start",
                    data = mapOf("started_at" to System.currentTimeMillis()),
                    latitude = startLatitude,
                    longitude = startLongitude
                )
            )
        )
        
        return createNavigationLog(request)
    }
    
    /**
     * Helper method to log navigation stop
     */
    suspend fun logNavigationStop(
        logId: Int,
        endLatitude: Double,
        endLongitude: Double,
        destinationReached: Boolean = false,
        navigationDuration: Int? = null,
        actualDistance: Double? = null,
        stopReason: String = "user_cancelled"
    ): Result<StopNavigationResponse> {
        val request = StopNavigationRequest(
            logId = logId,
            endLatitude = endLatitude,
            endLongitude = endLongitude,
            destinationReached = destinationReached,
            navigationDuration = navigationDuration,
            actualDistance = actualDistance,
            stopReason = stopReason,
            additionalData = mapOf(
                "completion_time" to System.currentTimeMillis(),
                "user_rating" to 5
            )
        )
        
        return stopNavigation(request)
    }
    
    /**
     * Helper method to log destination reached
     */
    suspend fun logDestinationReached(
        logId: Int,
        endLatitude: Double,
        endLongitude: Double,
        navigationDuration: Int? = null,
        actualDistance: Double? = null
    ): Result<NavigationLogResponse> {
        val request = UpdateNavigationLogRequest(
            activityType = "destination_reached",
            endLatitude = endLatitude,
            endLongitude = endLongitude,
            destinationReached = true,
            actualDuration = navigationDuration,
            stopReason = "destination_reached",
            additionalData = mapOf(
                "completion_time" to System.currentTimeMillis(),
                "actual_distance" to actualDistance
            ) as Map<String, Any>,
            navigationEvents = listOf(
                NavigationEvent(
                    type = "destination_reached",
                    data = mapOf("arrived_at" to System.currentTimeMillis()),
                    latitude = endLatitude,
                    longitude = endLongitude
                )
            )
        )
        
        return updateNavigationLog(logId, request)
    }
    
    // Helper methods for device information
    private fun getBatteryLevel(): Int? {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getNetworkType(): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            when (activeNetwork?.type) {
                android.net.ConnectivityManager.TYPE_WIFI -> "WiFi"
                android.net.ConnectivityManager.TYPE_MOBILE -> "Mobile"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getScreenResolution(): String {
        return try {
            val displayMetrics = context.resources.displayMetrics
            "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getAvailableStorage(): Long? {
        return try {
            val stat = android.os.StatFs(context.filesDir.absolutePath)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}
