package com.example.gzingapp.data

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

// Navigation History Data Models
data class NavigationHistory(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("navigation_log_id") val navigationLogId: Int? = null,
    @SerializedName("start_latitude") val startLatitude: Double,
    @SerializedName("start_longitude") val startLongitude: Double,
    @SerializedName("end_latitude") val endLatitude: Double,
    @SerializedName("end_longitude") val endLongitude: Double,
    @SerializedName("destination_name") val destinationName: String,
    @SerializedName("destination_address") val destinationAddress: String? = null,
    @SerializedName("route_distance") val routeDistance: Double,
    @SerializedName("estimated_duration") val estimatedDuration: Int? = null,
    @SerializedName("actual_duration") val actualDuration: Int? = null,
    @SerializedName("estimated_fare") val estimatedFare: Double? = null,
    @SerializedName("actual_fare") val actualFare: Double? = null,
    @SerializedName("transport_mode") val transportMode: String,
    @SerializedName("success_rate") val successRate: Double = 100.0,
    @SerializedName("completion_time") val completionTime: String? = null,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("waypoints_count") val waypointsCount: Int = 0,
    @SerializedName("traffic_condition") val trafficCondition: String? = null,
    @SerializedName("average_speed") val averageSpeed: Double? = null,
    @SerializedName("route_quality") val routeQuality: String = "good",
    @SerializedName("user_rating") val userRating: Int? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("is_favorite") 
    @JsonAdapter(BooleanTypeAdapter::class)
    val isFavorite: Boolean = false,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class NavigationHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: NavigationHistoryData? = null,
    @SerializedName("timestamp") val timestamp: String
)

data class NavigationHistoryData(
    @SerializedName("history") val history: List<NavigationHistory>? = null,
    @SerializedName("pagination") val pagination: PaginationInfo? = null
)

data class NavigationHistoryStats(
    @SerializedName("total_navigations") val totalNavigations: Int,
    @SerializedName("successful_navigations") val successfulNavigations: Int,
    @SerializedName("avg_duration_minutes") val avgDurationMinutes: Double,
    @SerializedName("avg_distance_km") val avgDistanceKm: Double,
    @SerializedName("avg_speed_kmh") val avgSpeedKmh: Double,
    @SerializedName("favorite_routes") val favoriteRoutes: Int,
    @SerializedName("last_navigation") val lastNavigation: String? = null,
    @SerializedName("first_navigation") val firstNavigation: String? = null
)

// PopularDestination is defined in NavigationApiModels.kt

data class CreateNavigationHistoryRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("start_latitude") val startLatitude: Double,
    @SerializedName("start_longitude") val startLongitude: Double,
    @SerializedName("end_latitude") val endLatitude: Double,
    @SerializedName("end_longitude") val endLongitude: Double,
    @SerializedName("destination_name") val destinationName: String,
    @SerializedName("destination_address") val destinationAddress: String? = null,
    @SerializedName("route_distance") val routeDistance: Double,
    @SerializedName("estimated_duration") val estimatedDuration: Int? = null,
    @SerializedName("actual_duration") val actualDuration: Int? = null,
    @SerializedName("estimated_fare") val estimatedFare: Double? = null,
    @SerializedName("actual_fare") val actualFare: Double? = null,
    @SerializedName("transport_mode") val transportMode: String,
    @SerializedName("waypoints_count") val waypointsCount: Int = 0,
    @SerializedName("traffic_condition") val trafficCondition: String? = null,
    @SerializedName("average_speed") val averageSpeed: Double? = null,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("completion_time") val completionTime: String? = null
)

data class UpdateNavigationHistoryRequest(
    @SerializedName("user_rating") val userRating: Int? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("is_favorite") val isFavorite: Boolean? = null,
    @SerializedName("route_quality") val routeQuality: String? = null
)
