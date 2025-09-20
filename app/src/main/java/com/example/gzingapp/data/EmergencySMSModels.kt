package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName

/**
 * Request model for sending emergency SMS
 */
data class EmergencySMSRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("emergency_type") val emergencyType: String = "emergency",
    @SerializedName("message") val message: String = "",
    @SerializedName("contacts") val contacts: List<String>
)

/**
 * Response model for emergency SMS API
 */
data class EmergencySMSResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: EmergencySMSData?
)

/**
 * Data model for emergency SMS response
 */
data class EmergencySMSData(
    @SerializedName("total_contacts") val totalContacts: Int,
    @SerializedName("successful_sends") val successfulSends: Int,
    @SerializedName("failed_sends") val failedSends: Int,
    @SerializedName("location_info") val locationInfo: LocationInfo?,
    @SerializedName("results") val results: List<SMSResult>
)

/**
 * Location information from reverse geocoding
 */
data class LocationInfo(
    @SerializedName("address") val address: String,
    @SerializedName("formatted_address") val formattedAddress: String,
    @SerializedName("place_type") val placeType: String,
    @SerializedName("confidence") val confidence: Double
)

/**
 * Individual SMS result
 */
data class SMSResult(
    @SerializedName("phone") val phone: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

/**
 * Emergency SMS log entry
 */
data class EmergencySMSLog(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("emergency_type") val emergencyType: String,
    @SerializedName("contacts_json") val contactsJson: String,
    @SerializedName("success_count") val successCount: Int,
    @SerializedName("failure_count") val failureCount: Int,
    @SerializedName("message_content") val messageContent: String?,
    @SerializedName("location_address") val locationAddress: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)


