package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName

/**
 * Navigation Activity Log Data Models
 * Models for tracking user navigation activities
 */

data class NavigationActivityLog(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("user_name")
    val userName: String,
    
    @SerializedName("activity_type")
    val activityType: String, // navigation_start, navigation_stop, navigation_pause, navigation_resume, route_change, destination_reached
    
    @SerializedName("start_latitude")
    val startLatitude: Double? = null,
    
    @SerializedName("start_longitude")
    val startLongitude: Double? = null,
    
    @SerializedName("end_latitude")
    val endLatitude: Double? = null,
    
    @SerializedName("end_longitude")
    val endLongitude: Double? = null,
    
    @SerializedName("destination_name")
    val destinationName: String? = null,
    
    @SerializedName("destination_address")
    val destinationAddress: String? = null,
    
    @SerializedName("route_distance")
    val routeDistance: Double? = null, // in kilometers
    
    @SerializedName("estimated_duration")
    val estimatedDuration: Int? = null, // in minutes
    
    @SerializedName("transport_mode")
    val transportMode: String? = null, // driving, walking, cycling, transit
    
    @SerializedName("navigation_duration")
    val navigationDuration: Int? = null, // actual duration in minutes
    
    @SerializedName("destination_reached")
    val destinationReached: Boolean = false, // whether user actually reached destination
    
    @SerializedName("route_instructions")
    val routeInstructions: List<RouteInstruction>? = null,
    
    @SerializedName("waypoints")
    val waypoints: List<Waypoint>? = null,
    
    @SerializedName("device_info")
    val deviceInfo: DeviceInfo? = null,
    
    @SerializedName("app_version")
    val appVersion: String? = null,
    
    @SerializedName("os_version")
    val osVersion: String? = null,
    
    @SerializedName("additional_data")
    val additionalData: Map<String, Any>? = null,
    
    @SerializedName("error_message")
    val errorMessage: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

// RouteInstruction moved to NavigationApiModels.kt

// Waypoint moved to NavigationApiModels.kt

data class DeviceInfo(
    @SerializedName("device_model")
    val deviceModel: String? = null,
    
    @SerializedName("device_id")
    val deviceId: String? = null,
    
    @SerializedName("battery_level")
    val batteryLevel: Int? = null,
    
    @SerializedName("network_type")
    val networkType: String? = null,
    
    @SerializedName("gps_accuracy")
    val gpsAccuracy: String? = null,
    
    @SerializedName("screen_resolution")
    val screenResolution: String? = null,
    
    @SerializedName("available_storage")
    val availableStorage: Long? = null
)

// Request models for creating navigation logs
data class NavigationStartRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @SerializedName("start_latitude")
    val startLatitude: Double? = null,
    
    @SerializedName("start_longitude")
    val startLongitude: Double? = null,
    
    @SerializedName("destination_name")
    val destinationName: String? = null,
    
    @SerializedName("destination_address")
    val destinationAddress: String? = null,
    
    @SerializedName("route_distance")
    val routeDistance: Double? = null,
    
    @SerializedName("estimated_duration")
    val estimatedDuration: Int? = null,
    
    @SerializedName("transport_mode")
    val transportMode: String? = null,
    
    @SerializedName("route_instructions")
    val routeInstructions: List<RouteInstruction>? = null,
    
    @SerializedName("waypoints")
    val waypoints: List<Waypoint>? = null,
    
    @SerializedName("device_info")
    val deviceInfo: DeviceInfo? = null,
    
    @SerializedName("app_version")
    val appVersion: String? = null,
    
    @SerializedName("os_version")
    val osVersion: String? = null,
    
    @SerializedName("additional_data")
    val additionalData: Map<String, Any>? = null
)

data class NavigationStopRequest(
    @SerializedName("log_id")
    val logId: Int? = null,
    
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @SerializedName("end_latitude")
    val endLatitude: Double? = null,
    
    @SerializedName("end_longitude")
    val endLongitude: Double? = null,
    
    @SerializedName("destination_reached")
    val destinationReached: Boolean = false,
    
    @SerializedName("navigation_duration")
    val navigationDuration: Int? = null,
    
    @SerializedName("actual_distance")
    val actualDistance: Double? = null,
    
    @SerializedName("stop_reason")
    val stopReason: String? = null,
    
    @SerializedName("additional_data")
    val additionalData: Map<String, Any>? = null
)

data class NavigationStopResponse(
    @SerializedName("log")
    val log: NavigationActivityLog,
    
    @SerializedName("stop_info")
    val stopInfo: StopInfo
)

data class StopInfo(
    @SerializedName("log_id")
    val logId: Int,
    
    @SerializedName("destination_reached")
    val destinationReached: Boolean,
    
    @SerializedName("navigation_duration")
    val navigationDuration: Int?,
    
    @SerializedName("actual_distance")
    val actualDistance: Double?,
    
    @SerializedName("stop_reason")
    val stopReason: String?,
    
    @SerializedName("stopped_at")
    val stoppedAt: String
)

data class DestinationReachedRequest(
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @SerializedName("end_latitude")
    val endLatitude: Double? = null,
    
    @SerializedName("end_longitude")
    val endLongitude: Double? = null,
    
    @SerializedName("navigation_duration")
    val navigationDuration: Int? = null,
    
    @SerializedName("additional_data")
    val additionalData: Map<String, Any>? = null
)

data class NavigationPauseRequest(
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    @SerializedName("additional_data")
    val additionalData: Map<String, Any>? = null
)

data class NavigationResumeRequest(
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    @SerializedName("additional_data")
    val additionalData: Map<String, Any>? = null
)

data class RouteChangeRequest(
    @SerializedName("route_distance")
    val routeDistance: Double? = null,
    
    @SerializedName("estimated_duration")
    val estimatedDuration: Int? = null,
    
    @SerializedName("transport_mode")
    val transportMode: String? = null,
    
    @SerializedName("route_instructions")
    val routeInstructions: List<RouteInstruction>? = null,
    
    @SerializedName("waypoints")
    val waypoints: List<Waypoint>? = null,
    
    @SerializedName("additional_data")
    val additionalData: Map<String, Any>? = null
)

// Response models
// NavigationLogsResponse moved to NavigationApiModels.kt

data class NavigationLogsData(
    @SerializedName("logs")
    val logs: List<NavigationActivityLog>? = null,
    
    @SerializedName("pagination")
    val pagination: PaginationInfo? = null
)

// PaginationInfo moved to ProfileModels.kt to avoid duplication

// NavigationStatsResponse moved to NavigationApiModels.kt

data class NavigationStatsData(
    @SerializedName("total_navigations")
    val totalNavigations: Int,
    
    @SerializedName("navigation_starts")
    val navigationStarts: Int,
    
    @SerializedName("destinations_reached")
    val destinationsReached: Int,
    
    @SerializedName("navigation_stops")
    val navigationStops: Int,
    
    @SerializedName("successful_navigations")
    val successfulNavigations: Int,
    
    @SerializedName("cancelled_navigations")
    val cancelledNavigations: Int,
    
    @SerializedName("avg_navigation_duration")
    val avgNavigationDuration: Double? = null,
    
    @SerializedName("avg_route_distance")
    val avgRouteDistance: Double? = null,
    
    @SerializedName("total_distance_traveled")
    val totalDistanceTraveled: Double? = null,
    
    @SerializedName("transport_modes_used")
    val transportModesUsed: Int,
    
    @SerializedName("active_days")
    val activeDays: Int
)

data class PopularDestinationsResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<PopularDestination>? = null
)

// PopularDestination moved to NavigationApiModels.kt

data class TransportStatsResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<TransportModeStat>? = null
)

data class TransportModeStat(
    @SerializedName("transport_mode")
    val transportMode: String,
    
    @SerializedName("usage_count")
    val usageCount: Int,
    
    @SerializedName("avg_distance")
    val avgDistance: Double? = null,
    
    @SerializedName("avg_duration")
    val avgDuration: Double? = null
)

// Activity types enum
enum class NavigationActivityType(val value: String) {
    NAVIGATION_START("navigation_start"),
    NAVIGATION_STOP("navigation_stop"),
    NAVIGATION_PAUSE("navigation_pause"),
    NAVIGATION_RESUME("navigation_resume"),
    ROUTE_CHANGE("route_change"),
    DESTINATION_REACHED("destination_reached")
}

// Transport modes enum
enum class TransportMode(val value: String) {
    DRIVING("driving"),
    WALKING("walking"),
    CYCLING("cycling"),
    TRANSIT("transit")
}

// Delete request model
data class DeleteLogsRequest(
    @SerializedName("log_ids")
    val logIds: List<Int>
)
