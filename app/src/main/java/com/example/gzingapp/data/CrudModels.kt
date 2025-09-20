package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName

// User Management Models
data class CreateUserRequest(
    @SerializedName("first_name")
    val firstName: String,
    
    @SerializedName("last_name")
    val lastName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    
    @SerializedName("role")
    val role: String = "user",
    
    @SerializedName("status")
    val status: String = "active",
    
    @SerializedName("notes")
    val notes: String? = null
)

data class UsersResponse(
    @SerializedName("users")
    val users: List<User>,
    
    @SerializedName("pagination")
    val pagination: PaginationInfo
)

// Route Models - Only unique classes not in RouteModels.kt

// SOS Contact Models - Only unique classes not in ProfileModels.kt
data class CreateSosContactRequest(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String,
    
    @SerializedName("relationship")
    val relationship: String,
    
    @SerializedName("is_primary")
    val isPrimary: Boolean = false
)

// Navigation Log Models - Only unique classes not in NavigationActivityModels.kt
data class CreateNavigationLogRequest(
    @SerializedName("activity_type")
    val activityType: String,
    
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
    val routeDistance: Double? = null,
    
    @SerializedName("estimated_duration")
    val estimatedDuration: Int? = null,
    
    @SerializedName("transport_mode")
    val transportMode: String? = null,
    
    @SerializedName("navigation_duration")
    val navigationDuration: Int? = null,
    
    @SerializedName("route_instructions")
    val routeInstructions: String? = null,
    
    @SerializedName("waypoints")
    val waypoints: List<String>? = null,
    
    @SerializedName("destination_reached")
    val destinationReached: Boolean = false,
    
    @SerializedName("device_info")
    val deviceInfo: String? = null,
    
    @SerializedName("app_version")
    val appVersion: String? = null,
    
    @SerializedName("os_version")
    val osVersion: String? = null,
    
    @SerializedName("additional_data")
    val additionalData: Map<String, Any>? = null,
    
    @SerializedName("error_message")
    val errorMessage: String? = null
)