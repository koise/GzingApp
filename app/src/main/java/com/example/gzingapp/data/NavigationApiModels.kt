package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName

// Navigation API Request Models
data class CreateNewNavigationLogRequest(
    @SerializedName("activity_type") val activityType: String,
    @SerializedName("start_latitude") val startLatitude: Double? = null,
    @SerializedName("start_longitude") val startLongitude: Double? = null,
    @SerializedName("end_latitude") val endLatitude: Double? = null,
    @SerializedName("end_longitude") val endLongitude: Double? = null,
    @SerializedName("destination_name") val destinationName: String? = null,
    @SerializedName("destination_address") val destinationAddress: String? = null,
    @SerializedName("route_distance") val routeDistance: Double? = null,
    @SerializedName("estimated_duration") val estimatedDuration: Int? = null,
    @SerializedName("actual_duration") val actualDuration: Int? = null,
    @SerializedName("transport_mode") val transportMode: String? = null,
    @SerializedName("destination_reached") val destinationReached: Boolean = false,
    @SerializedName("stop_reason") val stopReason: String? = null,
    @SerializedName("device_model") val deviceModel: String? = null,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("battery_level") val batteryLevel: Int? = null,
    @SerializedName("network_type") val networkType: String? = null,
    @SerializedName("gps_accuracy") val gpsAccuracy: String? = null,
    @SerializedName("screen_resolution") val screenResolution: String? = null,
    @SerializedName("available_storage") val availableStorage: Long? = null,
    @SerializedName("app_version") val appVersion: String? = null,
    @SerializedName("os_version") val osVersion: String? = null,
    @SerializedName("additional_data") val additionalData: Map<String, Any>? = null,
    @SerializedName("error_message") val errorMessage: String? = null,
    @SerializedName("route_instructions") val routeInstructions: List<RouteInstruction>? = null,
    @SerializedName("waypoints") val waypoints: List<Waypoint>? = null,
    @SerializedName("route_polylines") val routePolylines: List<RoutePolyline>? = null,
    @SerializedName("traffic_data") val trafficData: TrafficData? = null,
    @SerializedName("navigation_events") val navigationEvents: List<NavigationEvent>? = null
)

data class UpdateNavigationLogRequest(
    @SerializedName("activity_type") val activityType: String? = null,
    @SerializedName("end_latitude") val endLatitude: Double? = null,
    @SerializedName("end_longitude") val endLongitude: Double? = null,
    @SerializedName("destination_reached") val destinationReached: Boolean? = null,
    @SerializedName("actual_duration") val actualDuration: Int? = null,
    @SerializedName("stop_reason") val stopReason: String? = null,
    @SerializedName("additional_data") val additionalData: Map<String, Any>? = null,
    @SerializedName("error_message") val errorMessage: String? = null,
    @SerializedName("traffic_data") val trafficData: TrafficData? = null,
    @SerializedName("navigation_events") val navigationEvents: List<NavigationEvent>? = null
)

data class StopNavigationRequest(
    @SerializedName("log_id") val logId: Int,
    @SerializedName("end_latitude") val endLatitude: Double? = null,
    @SerializedName("end_longitude") val endLongitude: Double? = null,
    @SerializedName("destination_reached") val destinationReached: Boolean = false,
    @SerializedName("navigation_duration") val navigationDuration: Int? = null,
    @SerializedName("actual_distance") val actualDistance: Double? = null,
    @SerializedName("stop_reason") val stopReason: String = "user_cancelled",
    @SerializedName("additional_data") val additionalData: Map<String, Any>? = null
)

// Navigation API Response Models
data class NavigationLogResponse(
    @SerializedName("log") val log: NavigationLog,
    @SerializedName("log_id") val logId: Int? = null
)

data class NavigationLogsResponse(
    @SerializedName("logs") val logs: List<NavigationLog>,
    @SerializedName("pagination") val pagination: PaginationInfo
)

data class NavigationLogDetailResponse(
    @SerializedName("log") val log: NavigationLog,
    @SerializedName("route_instructions") val routeInstructions: List<RouteInstruction>? = null,
    @SerializedName("waypoints") val waypoints: List<Waypoint>? = null,
    @SerializedName("route_polylines") val routePolylines: List<RoutePolyline>? = null,
    @SerializedName("traffic_data") val trafficData: List<TrafficData>? = null,
    @SerializedName("navigation_events") val navigationEvents: List<NavigationEvent>? = null,
    @SerializedName("navigation_metrics") val navigationMetrics: NavigationMetrics? = null,
    @SerializedName("summary") val summary: NavigationSummary? = null
)

data class NavigationStatsResponse(
    @SerializedName("period") val period: String,
    @SerializedName("basic_stats") val basicStats: BasicStats,
    @SerializedName("recent_activity") val recentActivity: List<RecentActivity>? = null,
    @SerializedName("traffic_stats") val trafficStats: List<TrafficStats>? = null,
    @SerializedName("time_stats") val timeStats: List<TimeStats>? = null,
    @SerializedName("popular_destinations") val popularDestinations: List<PopularDestination>? = null,
    @SerializedName("transport_mode_stats") val transportModeStats: List<TransportModeStats>? = null
)

data class StopNavigationResponse(
    @SerializedName("log") val log: NavigationLog,
    @SerializedName("navigation_summary") val navigationSummary: NavigationSummary,
    @SerializedName("message") val message: String
)

// Core Navigation Log Model
data class NavigationLog(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_name") val userName: String,
    @SerializedName("activity_type") val activityType: String,
    @SerializedName("start_latitude") val startLatitude: String? = null,
    @SerializedName("start_longitude") val startLongitude: String? = null,
    @SerializedName("end_latitude") val endLatitude: String? = null,
    @SerializedName("end_longitude") val endLongitude: String? = null,
    @SerializedName("destination_name") val destinationName: String? = null,
    @SerializedName("destination_address") val destinationAddress: String? = null,
    @SerializedName("route_distance") val routeDistance: String? = null,
    @SerializedName("estimated_duration") val estimatedDuration: Int? = null,
    @SerializedName("actual_duration") val actualDuration: Int? = null,
    @SerializedName("transport_mode") val transportMode: String? = null,
    @SerializedName("destination_reached") val destinationReached: Int = 0,
    @SerializedName("stop_reason") val stopReason: String? = null,
    @SerializedName("device_model") val deviceModel: String? = null,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("battery_level") val batteryLevel: Int? = null,
    @SerializedName("network_type") val networkType: String? = null,
    @SerializedName("gps_accuracy") val gpsAccuracy: String? = null,
    @SerializedName("screen_resolution") val screenResolution: String? = null,
    @SerializedName("available_storage") val availableStorage: Long? = null,
    @SerializedName("app_version") val appVersion: String? = null,
    @SerializedName("os_version") val osVersion: String? = null,
    @SerializedName("additional_data") val additionalData: Map<String, Any>? = null,
    @SerializedName("error_message") val errorMessage: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

// Supporting Models
data class RouteInstruction(
    @SerializedName("instruction") val instruction: String,
    @SerializedName("distance") val distance: Double? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("maneuver") val maneuver: String? = null
)

data class Waypoint(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("name") val name: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("address") val address: String? = null
)

data class RoutePolyline(
    @SerializedName("type") val type: String,
    @SerializedName("data") val data: String,
    @SerializedName("color") val color: String? = null,
    @SerializedName("width") val width: Double? = null,
    @SerializedName("opacity") val opacity: Double? = null
)

data class TrafficData(
    @SerializedName("condition") val condition: String,
    @SerializedName("average_speed") val averageSpeed: Double? = null,
    @SerializedName("delay") val delay: Int? = null,
    @SerializedName("duration_with_traffic") val durationWithTraffic: Int? = null,
    @SerializedName("duration_without_traffic") val durationWithoutTraffic: Int? = null,
    @SerializedName("enabled") val enabled: Boolean = true
)

data class NavigationEvent(
    @SerializedName("type") val type: String,
    @SerializedName("data") val data: Map<String, Any>? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null
)

data class NavigationMetrics(
    @SerializedName("duration_accuracy") val durationAccuracy: Double? = null,
    @SerializedName("distance_accuracy") val distanceAccuracy: Double? = null,
    @SerializedName("success_rate") val successRate: Double? = null,
    @SerializedName("efficiency_score") val efficiencyScore: Double? = null
)

data class NavigationSummary(
    @SerializedName("total_instructions") val totalInstructions: Int,
    @SerializedName("total_waypoints") val totalWaypoints: Int,
    @SerializedName("total_polylines") val totalPolylines: Int,
    @SerializedName("total_events") val totalEvents: Int,
    @SerializedName("has_traffic_data") val hasTrafficData: Boolean,
    @SerializedName("navigation_completed") val navigationCompleted: Boolean,
    @SerializedName("destination_reached") val destinationReached: Boolean
)

data class PaginationInfo(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    @SerializedName("has_prev_page") val hasPrevPage: Boolean
)

data class BasicStats(
    @SerializedName("total_navigations") val totalNavigations: Int,
    @SerializedName("navigation_starts") val navigationStarts: Int,
    @SerializedName("destinations_reached") val destinationsReached: Int,
    @SerializedName("navigation_stops") val navigationStops: Int,
    @SerializedName("successful_navigations") val successfulNavigations: Int,
    @SerializedName("cancelled_navigations") val cancelledNavigations: Int,
    @SerializedName("success_rate") val successRate: Double,
    @SerializedName("avg_navigation_duration") val avgNavigationDuration: Double,
    @SerializedName("avg_route_distance") val avgRouteDistance: Double,
    @SerializedName("total_distance_traveled") val totalDistanceTraveled: Double,
    @SerializedName("avg_speed_kmh") val avgSpeedKmh: Double,
    @SerializedName("transport_modes_used") val transportModesUsed: Int,
    @SerializedName("active_days") val activeDays: Int,
    @SerializedName("first_navigation") val firstNavigation: String? = null,
    @SerializedName("last_navigation") val lastNavigation: String? = null
)

data class RecentActivity(
    @SerializedName("date") val date: String,
    @SerializedName("navigation_count") val navigationCount: Int,
    @SerializedName("total_distance") val totalDistance: Double,
    @SerializedName("avg_duration") val avgDuration: Double
)

data class TrafficStats(
    @SerializedName("traffic_condition") val trafficCondition: String,
    @SerializedName("occurrence_count") val occurrenceCount: Int,
    @SerializedName("avg_speed") val avgSpeed: Double? = null,
    @SerializedName("avg_delay") val avgDelay: Double? = null
)

data class TimeStats(
    @SerializedName("hour") val hour: Int,
    @SerializedName("total_navigations") val totalNavigations: Int,
    @SerializedName("successful_navigations") val successfulNavigations: Int,
    @SerializedName("success_rate") val successRate: Double
)

data class PopularDestination(
    @SerializedName("destination_name") val destinationName: String,
    @SerializedName("destination_address") val destinationAddress: String? = null,
    @SerializedName("visit_count") val visitCount: Int,
    @SerializedName("avg_distance") val avgDistance: Double,
    @SerializedName("avg_duration") val avgDuration: Double,
    @SerializedName("last_visit") val lastVisit: String
)

data class TransportModeStats(
    @SerializedName("transport_mode") val transportMode: String,
    @SerializedName("usage_count") val usageCount: Int,
    @SerializedName("avg_distance") val avgDistance: Double,
    @SerializedName("avg_duration") val avgDuration: Double,
    @SerializedName("successful_trips") val successfulTrips: Int
)
