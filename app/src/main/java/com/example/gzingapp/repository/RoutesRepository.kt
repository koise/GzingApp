package com.example.gzingapp.repository

import android.content.Context
import android.util.Log
import com.example.gzingapp.data.*
import com.example.gzingapp.network.ApiService
import com.example.gzingapp.network.RetrofitClient
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoutesRepository(private val context: Context) {
    
    private val apiService: ApiService = RetrofitClient.apiService
    private val appSettings = AppSettings(context)
    
    companion object {
        private const val TAG = "RoutesRepository"
    }
    
    /**
     * Create a new route
     */
    suspend fun createRoute(
        name: String,
        description: String? = null,
        pinCount: Int = 0,
        kilometer: Double = 0.0,
        estimatedTotalFare: Double = 0.0,
        mapDetails: MapDetails? = null,
        status: String = "active"
    ): Result<Route> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating route: $name")
                
                val request = CreateRouteRequest(
                    name = name,
                    description = description,
                    pincount = pinCount,
                    kilometer = kilometer,
                    estimated_total_fare = estimatedTotalFare,
                    map_details = mapDetails,
                    status = status
                )
                
                val response = apiService.createRoute(request)
                
                if (response.isSuccessful() && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Log.d(TAG, "Route created successfully: ${body.data?.route?.id}")
                        Result.success(body.data!!.route!!)
                    } else {
                        Log.e(TAG, "API returned error: ${body.message}")
                        Result.failure(Exception(body.message ?: "Unknown error"))
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating route", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get all routes
     */
    suspend fun getRoutes(
        limit: Int = 50,
        offset: Int = 0,
        orderBy: String = "created_at",
        orderDirection: String = "DESC"
    ): Result<RoutesApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching routes")
                
                val response = apiService.getRoutes(limit, offset, orderBy, orderDirection)
                
                if (response.isSuccessful() && response.body() != null) {
                    val body = response.body()!!
                    Log.d(TAG, "Routes fetched successfully: ${body.data?.routes?.size} routes")
                    Result.success(body)
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching routes", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get route details by ID
     */
    suspend fun getRouteDetails(routeId: Int): Result<RouteDetailsApiResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching route details for ID: $routeId")
                
                val response = apiService.getRouteDetails(routeId)
                
                if (response.isSuccessful() && response.body() != null) {
                    val body = response.body()!!
                    Log.d(TAG, "Route details fetched successfully")
                    Result.success(body)
                } else {
                    Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                    Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching route details", e)
                Result.failure(e)
            }
        }
    }
}