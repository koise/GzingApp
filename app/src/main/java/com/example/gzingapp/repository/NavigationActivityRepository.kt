package com.example.gzingapp.repository

import com.example.gzingapp.data.*
import com.example.gzingapp.network.RetrofitClient
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class NavigationActivityRepository(private val context: android.content.Context) {
    private val apiService = RetrofitClient.apiService
    private val appSettings = AppSettings(context)
    
    // For testing, use a default user ID
    private val defaultUserId = 10
    
    companion object {
        private const val TAG = "NavigationActivityRepo"
    }
    
    /**
     * Log navigation start
     */
    suspend fun logNavigationStart(
        startLatitude: Double? = null,
        startLongitude: Double? = null,
        destinationName: String? = null,
        destinationAddress: String? = null,
        routeDistance: Double? = null,
        estimatedDuration: Int? = null,
        transportMode: String? = null,
        routeInstructions: List<RouteInstruction>? = null,
        waypoints: List<Waypoint>? = null,
        deviceInfo: DeviceInfo? = null,
        appVersion: String? = null,
        osVersion: String? = null,
        additionalData: Map<String, Any>? = null
    ): Result<NavigationActivityLog> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateNavigationLogRequest(
                    activityType = "navigation_start",
                    startLatitude = startLatitude,
                    startLongitude = startLongitude,
                    destinationName = destinationName,
                    destinationAddress = destinationAddress,
                    routeDistance = routeDistance,
                    estimatedDuration = estimatedDuration,
                    transportMode = transportMode,
                    routeInstructions = routeInstructions?.joinToString(";") { it.instruction },
                    waypoints = waypoints?.map { "${it.lat},${it.lng}" },
                    deviceInfo = deviceInfo?.toString(),
                    appVersion = appVersion,
                    osVersion = osVersion,
                    additionalData = additionalData
                )
                
                // Use old API method - this should be updated to use the new API
                val response = apiService.createNavigationLog(request)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(TAG, "Navigation start logged successfully")
                        Result.success(apiResponse.data)
                    } else {
                        Log.e(TAG, "API returned error: ${apiResponse?.message}")
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "Failed to log navigation start: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error logging navigation start", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Log navigation stop
     */
    suspend fun logNavigationStop(
        endLatitude: Double? = null,
        endLongitude: Double? = null,
        navigationDuration: Int? = null,
        additionalData: Map<String, Any>? = null
    ): Result<NavigationStopResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = NavigationStopRequest(
                    userId = defaultUserId, // Add user ID for testing
                    endLatitude = endLatitude,
                    endLongitude = endLongitude,
                    navigationDuration = navigationDuration,
                    additionalData = additionalData
                )
                
                val response = apiService.logNavigationStop(request)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        Log.d(TAG, "Navigation stop logged successfully")
                        Result.success(apiResponse.data)
                    } else {
                        Log.e(TAG, "API returned error: ${apiResponse?.message}")
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "Failed to log navigation stop: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error logging navigation stop", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Log destination reached
     */
    suspend fun logDestinationReached(
        endLatitude: Double? = null,
        endLongitude: Double? = null,
        navigationDuration: Int? = null,
        additionalData: Map<String, Any>? = null
    ): Result<NavigationLogsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = DestinationReachedRequest(
                    userId = defaultUserId, // Add user ID for testing
                    endLatitude = endLatitude,
                    endLongitude = endLongitude,
                    navigationDuration = navigationDuration,
                    additionalData = additionalData
                )
                
                val response = apiService.logDestinationReached(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Destination reached logged successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Empty response body")
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Log.e(TAG, "Failed to log destination reached: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error logging destination reached", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Log navigation pause
     */
    suspend fun logNavigationPause(
        latitude: Double? = null,
        longitude: Double? = null,
        additionalData: Map<String, Any>? = null
    ): Result<NavigationLogsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = NavigationPauseRequest(
                    latitude = latitude,
                    longitude = longitude,
                    additionalData = additionalData
                )
                
                val response = apiService.logNavigationPause(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Navigation pause logged successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Empty response body")
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Log.e(TAG, "Failed to log navigation pause: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error logging navigation pause", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Log navigation resume
     */
    suspend fun logNavigationResume(
        latitude: Double? = null,
        longitude: Double? = null,
        additionalData: Map<String, Any>? = null
    ): Result<NavigationLogsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = NavigationResumeRequest(
                    latitude = latitude,
                    longitude = longitude,
                    additionalData = additionalData
                )
                
                val response = apiService.logNavigationResume(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Navigation resume logged successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Empty response body")
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Log.e(TAG, "Failed to log navigation resume: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error logging navigation resume", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Log route change
     */
    suspend fun logRouteChange(
        routeDistance: Double? = null,
        estimatedDuration: Int? = null,
        transportMode: String? = null,
        routeInstructions: List<RouteInstruction>? = null,
        waypoints: List<Waypoint>? = null,
        additionalData: Map<String, Any>? = null
    ): Result<NavigationLogsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RouteChangeRequest(
                    routeDistance = routeDistance,
                    estimatedDuration = estimatedDuration,
                    transportMode = transportMode,
                    routeInstructions = routeInstructions,
                    waypoints = waypoints,
                    additionalData = additionalData
                )
                
                val response = apiService.logRouteChange(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Route change logged successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Empty response body")
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Log.e(TAG, "Failed to log route change: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error logging route change", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get navigation logs with pagination and filters
     */
    suspend fun getNavigationLogs(
        page: Int = 1,
        perPage: Int = 20,
        userId: Int? = null,
        userName: String? = null,
        activityType: String? = null,
        transportMode: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): Result<NavigationLogsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getNavigationLogs(
                    page = page,
                    perPage = perPage,
                    userId = userId ?: defaultUserId, // Use default user ID if none provided
                    userName = userName,
                    activityType = activityType,
                    transportMode = transportMode,
                    dateFrom = dateFrom,
                    dateTo = dateTo
                )
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Navigation logs retrieved successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Empty response body")
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Log.e(TAG, "Failed to get navigation logs: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting navigation logs", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get user's navigation logs
     */
    suspend fun getUserNavigationLogs(userId: Int = defaultUserId, limit: Int = 100): Result<NavigationLogsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserNavigationLogs(userId = userId, limit = limit)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "User navigation logs retrieved successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Empty response body")
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Log.e(TAG, "Failed to get user navigation logs: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user navigation logs", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get navigation statistics
     */
    suspend fun getNavigationStats(
        userId: Int = defaultUserId,
        dateFrom: String? = null,
        dateTo: String? = null
    ): Result<NavigationStatsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getNavigationStats(
                    userId = userId,
                    dateFrom = dateFrom,
                    dateTo = dateTo
                )
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Navigation statistics retrieved successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Empty response body")
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Log.e(TAG, "Failed to get navigation statistics: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting navigation statistics", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get popular destinations
     */
    suspend fun getPopularDestinations(limit: Int = 10): Result<PopularDestinationsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPopularDestinations(limit = limit)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Popular destinations retrieved successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Empty response body")
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Log.e(TAG, "Failed to get popular destinations: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting popular destinations", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get transport mode statistics
     */
    suspend fun getTransportStats(): Result<TransportStatsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTransportStats()
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Transport statistics retrieved successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Empty response body")
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Log.e(TAG, "Failed to get transport statistics: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting transport statistics", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Delete navigation logs
     */
    suspend fun deleteNavigationLogs(logIds: List<Int>): Result<ApiResponse<Any>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = DeleteLogsRequest(logIds = logIds)
                val response = apiService.deleteNavigationLogs(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Navigation logs deleted successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "Empty response body")
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Log.e(TAG, "Failed to delete navigation logs: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting navigation logs", e)
                Result.failure(e)
            }
        }
    }
}
