package com.example.gzingapp.repository

import android.content.Context
import android.util.Log
import com.example.gzingapp.data.*
import com.example.gzingapp.network.ApiService
import com.example.gzingapp.network.RetrofitClient
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NavigationHistoryRepository(private val context: Context) {
    
    private val apiService: ApiService = RetrofitClient.apiService
    private val appSettings = AppSettings(context)
    private val TAG = "NavigationHistoryRepo"
    
    /**
     * Get navigation history with pagination and filters
     */
    suspend fun getNavigationHistory(
        page: Int = 1,
        limit: Int = 20,
        transportMode: String? = null,
        isFavorite: Boolean? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): Result<NavigationHistoryData> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = appSettings.getUserId() ?: 10
                Log.d(TAG, "Getting navigation history for user: $userId, page: $page")
                
                val response = apiService.getNavigationHistory(
                    userId = userId,
                    limit = limit,
                    offset = (page - 1) * limit
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Navigation history retrieved successfully")
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
                Log.e(TAG, "Error getting navigation history", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get navigation history statistics
     */
    suspend fun getNavigationHistoryStats(): Result<NavigationHistoryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = appSettings.getUserId() ?: 10
                Log.d(TAG, "Getting navigation history stats for user: $userId")
                
                val response = apiService.getNavigationHistoryStats(userId)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Navigation history stats retrieved successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "API returned error: ${body.message}")
                        Result.failure(Exception(body.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting navigation history stats", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get popular destinations (commented out for now due to API response format issues)
     */
    /*
    suspend fun getPopularDestinations(): Result<List<PopularDestination>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = appSettings.getUserId() ?: 10
                Log.d(TAG, "Getting popular destinations for user: $userId")
                
                val response = apiService.getNavigationHistoryPopularDestinations(userId)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Popular destinations retrieved successfully")
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
                Log.e(TAG, "Error getting popular destinations", e)
                Result.failure(e)
            }
        }
    }
    */
    
    /**
     * Create navigation history entry
     */
    suspend fun createNavigationHistory(request: CreateNavigationHistoryRequest): Result<NavigationHistoryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating navigation history entry")
                
                val response = apiService.createNavigationHistory(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Navigation history created successfully")
                        Result.success(body)
                    } else {
                        Log.e(TAG, "API returned error: ${body.message}")
                        Result.failure(Exception(body.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating navigation history", e)
                Result.failure(e)
            }
        }
    }
    
    
    
    /**
     * Create navigation history entry (standalone version)
     */
    suspend fun createNavigationHistoryStandalone(request: CreateNavigationHistoryRequest): Result<NavigationHistoryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating navigation history entry (standalone) for user: ${request.userId}")
                
                val response = apiService.createNavigationHistory(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Navigation history created successfully (standalone)")
                        Result.success(body)
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
                Log.e(TAG, "Error creating navigation history (standalone)", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get navigation history by ID
     */
    suspend fun getNavigationHistoryById(id: Int): Result<NavigationHistoryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = appSettings.getUserId() ?: 10
                Log.d(TAG, "Getting navigation history by ID: $id for user: $userId")
                
                val response = apiService.getNavigationHistoryById(id, userId)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Navigation history retrieved successfully by ID")
                        Result.success(body)
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
                Log.e(TAG, "Error getting navigation history by ID", e)
                Result.failure(e)
            }
        }
    }
}
