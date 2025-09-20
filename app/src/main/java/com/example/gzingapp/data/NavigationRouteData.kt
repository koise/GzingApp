package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName

// Request data class for creating navigation routes
data class CreateNavigationRouteRequest(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("route_name")
    val routeName: String,
    
    @SerializedName("route_description")
    val routeDescription: String? = null,
    
    @SerializedName("start_latitude")
    val startLatitude: Double,
    
    @SerializedName("start_longitude")
    val startLongitude: Double,
    
    @SerializedName("end_latitude")
    val endLatitude: Double,
    
    @SerializedName("end_longitude")
    val endLongitude: Double,
    
    @SerializedName("destination_name")
    val destinationName: String,
    
    @SerializedName("destination_address")
    val destinationAddress: String? = null,
    
    @SerializedName("route_distance")
    val routeDistance: Double,
    
    @SerializedName("estimated_duration")
    val estimatedDuration: Int? = null,
    
    @SerializedName("estimated_fare")
    val estimatedFare: Double? = null,
    
    @SerializedName("transport_mode")
    val transportMode: String,
    
    @SerializedName("route_quality")
    val routeQuality: String = "good",
    
    @SerializedName("traffic_condition")
    val trafficCondition: String? = null,
    
    @SerializedName("average_speed")
    val averageSpeed: Double? = null,
    
    @SerializedName("waypoints_count")
    val waypointsCount: Int = 0,
    
    @SerializedName("route_coordinates")
    val routeCoordinates: List<Map<String, Double>>? = null,
    
    @SerializedName("is_favorite")
    val isFavorite: Int = 0,
    
    @SerializedName("is_public")
    val isPublic: Int = 0
)

// Response data class for navigation route
data class NavigationRoute(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("route_name")
    val routeName: String,
    
    @SerializedName("route_description")
    val routeDescription: String?,
    
    @SerializedName("start_latitude")
    val startLatitude: String,
    
    @SerializedName("start_longitude")
    val startLongitude: String,
    
    @SerializedName("end_latitude")
    val endLatitude: String,
    
    @SerializedName("end_longitude")
    val endLongitude: String,
    
    @SerializedName("destination_name")
    val destinationName: String,
    
    @SerializedName("destination_address")
    val destinationAddress: String?,
    
    @SerializedName("route_distance")
    val routeDistance: String,
    
    @SerializedName("estimated_duration")
    val estimatedDuration: Int?,
    
    @SerializedName("estimated_fare")
    val estimatedFare: String?,
    
    @SerializedName("transport_mode")
    val transportMode: String,
    
    @SerializedName("route_quality")
    val routeQuality: String,
    
    @SerializedName("traffic_condition")
    val trafficCondition: String?,
    
    @SerializedName("average_speed")
    val averageSpeed: String?,
    
    @SerializedName("waypoints_count")
    val waypointsCount: Int,
    
    @SerializedName("route_coordinates")
    val routeCoordinates: List<Map<String, Double>>?,
    
    @SerializedName("is_favorite")
    val isFavorite: Int,
    
    @SerializedName("is_public")
    val isPublic: Int,
    
    @SerializedName("usage_count")
    val usageCount: Int,
    
    @SerializedName("last_used")
    val lastUsed: String?,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)

// Response wrapper for navigation route creation
data class CreateNavigationRouteResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: NavigationRoute? = null
)

// Response wrapper for getting navigation routes
data class GetNavigationRoutesResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: NavigationRoutesData? = null
)

// Data wrapper for navigation routes list
data class NavigationRoutesData(
    @SerializedName("routes")
    val routes: List<NavigationRoute>,
    
    @SerializedName("pagination")
    val pagination: RoutePaginationInfo
)

// Pagination information for navigation routes
data class RoutePaginationInfo(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("limit")
    val limit: Int,
    
    @SerializedName("offset")
    val offset: Int,
    
    @SerializedName("has_more")
    val hasMore: Boolean
)
