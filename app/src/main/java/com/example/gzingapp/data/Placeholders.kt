package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName

// Common pagination info - moved to NavigationApiModels.kt to avoid duplication

// SOS / Emergency Contacts placeholder models
data class SosContactResponse(
    @SerializedName("contact") val contact: SosContact
)

data class EmergencyContactsResponse(
    @SerializedName("contacts") val contacts: List<SosContact> = emptyList(),
    @SerializedName("totalContacts") val totalContacts: Int = contacts.size
)

data class EmergencyContactDetailResponse(
    @SerializedName("contact") val contact: SosContact
)

data class UpdateEmergencyContactRequest(
    @SerializedName("contact_id") val contactId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("relationship") val relationship: String? = null,
    @SerializedName("is_primary") val isPrimary: Boolean? = null
)

data class EmergencyContactResponse(
    @SerializedName("contact") val contact: SosContact
)

data class DeleteEmergencyContactRequest(
    @SerializedName("contact_id") val contactId: Int,
    @SerializedName("permanent") val permanent: Boolean = false
)

data class DeleteEmergencyContactResponse(
    @SerializedName("deletedContact") val deletedContact: SosContact
)

// User/Profile placeholder models
data class UserProfileResponse(
    @SerializedName("user") val user: UserProfile
)

data class UpdateUserRequest(
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class UserResponse(
    @SerializedName("user") val user: UserProfile
)

data class ChangePasswordRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)

data class ChangePasswordResponse(
    @SerializedName("success") val success: Boolean = true
)

data class DeleteUserRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("password") val password: String
)

data class DeleteUserResponse(
    @SerializedName("success") val success: Boolean = true
)

data class UpdateUsersRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("username") val username: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("role") val role: String? = null
)

data class UpdateUsersResponse(
    @SerializedName("username") val username: String? = null,
    @SerializedName("role") val role: String = "user"
)

// Logs placeholder models (used by LogsRepository)
data class CreateLogRequest(
    @SerializedName("log_type") val logType: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    // navigation fields
    @SerializedName("activity_type") val activityType: String? = null,
    @SerializedName("start_latitude") val startLatitude: Double? = null,
    @SerializedName("start_longitude") val startLongitude: Double? = null,
    @SerializedName("end_latitude") val endLatitude: Double? = null,
    @SerializedName("end_longitude") val endLongitude: Double? = null,
    @SerializedName("destination_name") val destinationName: String? = null,
    @SerializedName("destination_address") val destinationAddress: String? = null,
    @SerializedName("route_distance") val routeDistance: Double? = null,
    @SerializedName("estimated_duration") val estimatedDuration: Int? = null,
    @SerializedName("transport_mode") val transportMode: String? = null,
    @SerializedName("navigation_duration") val navigationDuration: Int? = null,
    @SerializedName("route_instructions") val routeInstructions: String? = null,
    @SerializedName("waypoints") val waypoints: List<String>? = null,
    @SerializedName("destination_reached") val destinationReached: Boolean? = null,
    @SerializedName("device_info") val deviceInfo: String? = null,
    @SerializedName("app_version") val appVersion: String? = null,
    @SerializedName("os_version") val osVersion: String? = null,
    @SerializedName("additional_data") val additionalData: Map<String, Any>? = null,
    @SerializedName("error_message") val errorMessage: String? = null,
    // user activity fields
    @SerializedName("log_type_activity") val logTypeActivity: String? = null,
    @SerializedName("log_level") val logLevel: String? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("user_agent") val userAgent: String? = null
)

data class CreateLogResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("data") val data: LogData? = null,
    @SerializedName("message") val message: String? = null
)

data class LogData(
    @SerializedName("log") val log: Any? = null
)

data class LogsResponse(
    @SerializedName("logs") val logs: List<Any>? = null,
    @SerializedName("pagination") val pagination: PaginationInfo? = null
)

// Minimal log entities (only UserActivityLog is not defined elsewhere)
data class UserActivityLog(
    @SerializedName("id") val id: Int? = null
)


