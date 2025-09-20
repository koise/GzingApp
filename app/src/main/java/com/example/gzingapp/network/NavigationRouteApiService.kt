package com.example.gzingapp.network

import com.example.gzingapp.data.*
import retrofit2.Response
import retrofit2.http.*

interface NavigationRouteApiService {
    
    /**
     * Create a new navigation route
     */
    @POST("endpoints/navigation-routes/create_navigation_route.php")
    suspend fun createNavigationRoute(
        @Body request: CreateNavigationRouteRequest
    ): Response<CreateNavigationRouteResponse>
    
    /**
     * Get user's navigation routes with pagination and filtering
     */
    @GET("endpoints/navigation-routes/get_navigation_routes.php")
    suspend fun getNavigationRoutes(
        @Query("user_id") userId: Int,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("order_by") orderBy: String = "created_at",
        @Query("order_direction") orderDirection: String = "DESC",
        @Query("favorites_only") favoritesOnly: Boolean = false,
        @Query("search") search: String = ""
    ): Response<GetNavigationRoutesResponse>
}

