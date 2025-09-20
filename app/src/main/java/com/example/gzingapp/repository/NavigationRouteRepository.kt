package com.example.gzingapp.repository

import android.content.Context
import android.util.Log
import com.example.gzingapp.data.*
import com.example.gzingapp.network.ApiService
import com.example.gzingapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class NavigationRouteRepository(private val context: Context) {
    
    private val apiService: ApiService = RetrofitClient.apiService
    
    /**
     * Create a new navigation route
     */
    suspend fun createNavigationRoute(request: CreateNavigationRouteRequest): Result<CreateNavigationRouteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("NavigationRouteRepository", "=== API CALL START ===")
                Log.d("NavigationRouteRepository", "Creating navigation route: ${request.routeName}")
                Log.d("NavigationRouteRepository", "Request details:")
                Log.d("NavigationRouteRepository", "  - User ID: ${request.userId}")
                Log.d("NavigationRouteRepository", "  - Route Name: ${request.routeName}")
                Log.d("NavigationRouteRepository", "  - Start: ${request.startLatitude}, ${request.startLongitude}")
                Log.d("NavigationRouteRepository", "  - End: ${request.endLatitude}, ${request.endLongitude}")
                Log.d("NavigationRouteRepository", "  - Distance: ${request.routeDistance}")
                Log.d("NavigationRouteRepository", "  - Transport Mode: ${request.transportMode}")
                
                val response: Response<CreateNavigationRouteResponse> = apiService.createNavigationRoute(request)
                
                Log.d("NavigationRouteRepository", "API Response received:")
                Log.d("NavigationRouteRepository", "  - HTTP Code: ${response.code()}")
                Log.d("NavigationRouteRepository", "  - HTTP Message: ${response.message()}")
                Log.d("NavigationRouteRepository", "  - Is Successful: ${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("NavigationRouteRepository", "Response body: $responseBody")
                    
                    if (responseBody != null && responseBody.success) {
                        Log.d("NavigationRouteRepository", "✅ Route created successfully: ${responseBody.message}")
                        Result.success(responseBody)
                    } else {
                        val errorMessage = responseBody?.message ?: "Unknown error occurred"
                        Log.e("NavigationRouteRepository", "❌ API returned error: $errorMessage")
                        Log.e("NavigationRouteRepository", "Response body: $responseBody")
                        Result.failure(Exception(errorMessage))
                    }
                } else {
                    val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                    Log.e("NavigationRouteRepository", "❌ HTTP error: $errorMessage")
                    
                    // Try to get error body
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("NavigationRouteRepository", "Error body: $errorBody")
                    } catch (e: Exception) {
                        Log.e("NavigationRouteRepository", "Could not read error body", e)
                    }
                    
                    Result.failure(Exception(errorMessage))
                }
                
            } catch (e: Exception) {
                Log.e("NavigationRouteRepository", "❌ Exception creating navigation route", e)
                Log.e("NavigationRouteRepository", "Exception type: ${e.javaClass.simpleName}")
                Log.e("NavigationRouteRepository", "Exception message: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get user's navigation routes with pagination and filtering
     */
    suspend fun getNavigationRoutes(
        userId: Int,
        limit: Int = 50,
        offset: Int = 0,
        orderBy: String = "created_at",
        orderDirection: String = "DESC",
        favoritesOnly: Boolean = false,
        search: String = ""
    ): Result<GetNavigationRoutesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("NavigationRouteRepository", "Getting navigation routes for user: $userId")
                Log.d("NavigationRouteRepository", "API call parameters - userId: $userId, limit: $limit, offset: $offset, favoritesOnly: $favoritesOnly")
                
                val response: Response<GetNavigationRoutesResponse> = apiService.getNavigationRoutes(
                    userId = userId,
                    limit = limit,
                    offset = offset,
                    orderBy = orderBy,
                    orderDirection = orderDirection,
                    favoritesOnly = null,  // null = no filter, get all routes
                    search = search
                )
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.success) {
                        Log.d("NavigationRouteRepository", "Routes retrieved successfully: ${responseBody.data?.routes?.size ?: 0} routes")
                        Result.success(responseBody)
                    } else {
                        val errorMessage = responseBody?.message ?: "Unknown error occurred"
                        Log.e("NavigationRouteRepository", "API returned error: $errorMessage")
                        Result.failure(Exception(errorMessage))
                    }
                } else {
                    val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                    Log.e("NavigationRouteRepository", "HTTP error: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
                
            } catch (e: Exception) {
                Log.e("NavigationRouteRepository", "Exception getting navigation routes", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get user's favorite routes
     */
    suspend fun getFavoriteRoutes(userId: Int): Result<GetNavigationRoutesResponse> {
        return getNavigationRoutes(
            userId = userId,
            favoritesOnly = true,
            orderBy = "last_used",
            orderDirection = "DESC"
        )
    }
    
    /**
     * Search routes by destination name or address
     */
    suspend fun searchRoutes(userId: Int, searchQuery: String): Result<GetNavigationRoutesResponse> {
        return getNavigationRoutes(
            userId = userId,
            search = searchQuery,
            orderBy = "usage_count",
            orderDirection = "DESC"
        )
    }
    
    /**
     * Get most used routes
     */
    suspend fun getMostUsedRoutes(userId: Int, limit: Int = 10): Result<GetNavigationRoutesResponse> {
        return getNavigationRoutes(
            userId = userId,
            limit = limit,
            orderBy = "usage_count",
            orderDirection = "DESC"
        )
    }
}
