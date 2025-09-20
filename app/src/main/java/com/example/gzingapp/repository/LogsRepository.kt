package com.example.gzingapp.repository

import com.example.gzingapp.data.*
import com.example.gzingapp.network.RetrofitClient
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class LogsRepository(private val context: android.content.Context) {
    private val apiService = RetrofitClient.apiService
    private val appSettings = AppSettings(context)
    
    companion object {
        private const val TAG = "LogsRepository"
    }
    
    /**
     * Create a navigation log
     */
    suspend fun createNavigationLog(
        userId: Int,
        activityType: String,
        startLatitude: Double? = null,
        startLongitude: Double? = null,
        endLatitude: Double? = null,
        endLongitude: Double? = null,
        destinationName: String? = null,
        destinationAddress: String? = null,
        routeDistance: Double? = null,
        estimatedDuration: Int? = null,
        transportMode: String? = null,
        navigationDuration: Int? = null,
        routeInstructions: String? = null,
        waypoints: List<String>? = null,
        destinationReached: Boolean = false,
        deviceInfo: String? = null,
        appVersion: String? = null,
        osVersion: String? = null,
        additionalData: Map<String, Any>? = null,
        errorMessage: String? = null
    ): Result<NavigationActivityLog> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateNavigationLogRequest(
                    activityType = activityType,
                    startLatitude = startLatitude,
                    startLongitude = startLongitude,
                    endLatitude = endLatitude,
                    endLongitude = endLongitude,
                    destinationName = destinationName,
                    destinationAddress = destinationAddress,
                    routeDistance = routeDistance,
                    estimatedDuration = estimatedDuration,
                    transportMode = transportMode,
                    navigationDuration = navigationDuration,
                    routeInstructions = routeInstructions,
                    waypoints = waypoints,
                    destinationReached = destinationReached,
                    deviceInfo = deviceInfo?.toString(),
                    appVersion = appVersion,
                    osVersion = osVersion,
                    additionalData = additionalData,
                    errorMessage = errorMessage
                )
                
                val response = apiService.createNavigationLog(request)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(TAG, "Navigation log created successfully")
                        Result.success(apiResponse.data)
                    } else {
                        Log.e(TAG, "API returned error: ${apiResponse?.message}")
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "Failed to create navigation log: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating navigation log", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Create a user activity log
     */
    suspend fun createUserActivityLog(
        userId: Int,
        logTypeActivity: String,
        logLevel: String = "info",
        action: String,
        message: String,
        userAgent: String? = null,
        additionalData: Map<String, Any>? = null
    ): Result<UserActivityLog> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateLogRequest(
                    logType = "user_activity",
                    userId = userId,
                    logTypeActivity = logTypeActivity,
                    logLevel = logLevel,
                    action = action,
                    message = message,
                    userAgent = userAgent,
                    additionalData = additionalData
                )
                
                val response = apiService.createLog(request)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(TAG, "User activity log created successfully")
                        val parsed = (apiResponse.data.data?.log as? UserActivityLog)
                        if (parsed != null) Result.success(parsed) else Result.failure(Exception("Unexpected log payload"))
                    } else {
                        Log.e(TAG, "API returned error: ${apiResponse?.message}")
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "Failed to create user activity log: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating user activity log", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Stop navigation by updating existing log
     */
    suspend fun stopNavigation(
        logId: Int? = null,
        userId: Int? = null,
        endLatitude: Double? = null,
        endLongitude: Double? = null,
        destinationReached: Boolean = false,
        navigationDuration: Int? = null,
        actualDistance: Double? = null,
        stopReason: String? = null,
        additionalData: Map<String, Any>? = null
    ): Result<NavigationActivityLog> {
        return withContext(Dispatchers.IO) {
            try {
                val request = NavigationStopRequest(
                    logId = logId,
                    userId = userId,
                    endLatitude = endLatitude,
                    endLongitude = endLongitude,
                    destinationReached = destinationReached,
                    navigationDuration = navigationDuration,
                    actualDistance = actualDistance,
                    stopReason = stopReason,
                    additionalData = additionalData
                )
                
                val response = apiService.logNavigationStop(request)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(TAG, "Navigation stopped successfully")
                        // Get the log from the response
                        val navigationLog = apiResponse.data.log
                        Result.success(navigationLog)
                    } else {
                        Log.e(TAG, "API returned error: ${apiResponse?.message}")
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "Failed to stop navigation: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping navigation", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get logs with filters
     */
    suspend fun getLogs(
        logType: String = "all",
        page: Int = 1,
        limit: Int = 20,
        activityType: String? = null,
        transportMode: String? = null,
        logTypeActivity: String? = null,
        logLevel: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): Result<LogsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLogs(
                    logType = logType,
                    page = page,
                    limit = limit,
                    activityType = activityType,
                    transportMode = transportMode,
                    logTypeActivity = logTypeActivity,
                    logLevel = logLevel,
                    dateFrom = dateFrom,
                    dateTo = dateTo
                )
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(TAG, "Logs retrieved successfully")
                        Result.success(apiResponse.data)
                    } else {
                        Log.e(TAG, "API returned error: ${apiResponse?.message}")
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "Failed to get logs: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting logs", e)
                Result.failure(e)
            }
        }
    }
}
