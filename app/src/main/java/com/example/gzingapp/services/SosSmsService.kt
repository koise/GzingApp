package com.example.gzingapp.services

import android.content.Context
import android.util.Log
import com.example.gzingapp.data.SosContact
import com.example.gzingapp.data.SosSmsRequest
import com.example.gzingapp.data.SosSmsResponse
import com.example.gzingapp.network.ApiService
import com.example.gzingapp.network.RetrofitClient
import com.example.gzingapp.service.EmergencySMSService
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class SosSmsService(private val context: Context) {
    
    private val apiService: ApiService = RetrofitClient.apiService
    private val appSettings = AppSettings(context)
    private val emergencySMSService = EmergencySMSService(context)
    
    companion object {
        private const val TAG = "SosSmsService"
    }
    
    /**
     * Send emergency SMS to all SOS contacts
     */
    suspend fun sendEmergencySms(
        contacts: List<SosContact>,
        currentLocation: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<SosSmsResponse> = withContext(Dispatchers.IO) {
        try {
            val phoneNumbers = contacts.map { it.phoneNumber }
            
            Log.d(TAG, "Sending emergency SMS to ${phoneNumbers.size} contacts")
            Log.d(TAG, "Phone numbers: ${phoneNumbers.joinToString(", ")}")
            
            // Use EmergencySMSService to send SMS with location
            val result = if (latitude != null && longitude != null && (latitude != 0.0 || longitude != 0.0)) {
                Log.d(TAG, "Using GPS coordinates: $latitude, $longitude")
                emergencySMSService.sendEmergencySMS(
                    latitude = latitude,
                    longitude = longitude,
                    emergencyType = "emergency",
                    customMessage = currentLocation ?: "",
                    contacts = phoneNumbers
                )
            } else {
                Log.w(TAG, "GPS location not available, using fallback message")
                // Use fallback message instead of 0.0 coordinates
                emergencySMSService.sendEmergencySMS(
                    latitude = 0.0,
                    longitude = 0.0,
                    emergencyType = "emergency",
                    customMessage = "GPS location unavailable. Please check my last known location or contact me directly.",
                    contacts = phoneNumbers
                )
            }
            
            result.fold(
                onSuccess = { response ->
                    Log.d(TAG, "Emergency SMS sent successfully")
                    Log.d(TAG, "Success count: ${response.data?.successfulSends}")
                    Log.d(TAG, "Failure count: ${response.data?.failedSends}")
                    
                    Result.success(SosSmsResponse(
                        status = if (response.success) "success" else "error",
                        message = response.message,
                        data = null
                    ))
                },
                onFailure = { error ->
                    Log.e(TAG, "Error sending emergency SMS", error)
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending emergency SMS", e)
            Result.failure(e)
        }
    }
    
    /**
     * Build emergency message with location
     */
    private fun buildEmergencyMessage(currentLocation: String?): String {
        val userInfo = appSettings.getUserInfo()
        val userName = "${userInfo.firstName} ${userInfo.lastName}".trim()
        
        val baseMessage = "🚨 EMERGENCY ALERT 🚨\n\n" +
                "This is an emergency message from GzingApp.\n\n" +
                "User: ${if (userName.isNotEmpty()) userName else "Unknown"}\n" +
                "Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n\n" +
                "I need emergency help! Please contact me immediately.\n\n"
        
        val locationMessage = if (!currentLocation.isNullOrEmpty()) {
            "📍 Current Location: $currentLocation\n\n"
        } else {
            "📍 Location: Unable to determine current location\n\n"
        }
        
        val footerMessage = "This message was sent automatically by GzingApp emergency system.\n" +
                "Please respond immediately if you receive this message."
        
        return baseMessage + locationMessage + footerMessage
    }
    
    /**
     * Send test SMS to verify service
     */
    suspend fun sendTestSms(phoneNumber: String): Result<SosSmsResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Sending test SMS to $phoneNumber")
            
            // Use EmergencySMSService to send test SMS
            val result = emergencySMSService.sendEmergencySMS(
                latitude = 0.0,
                longitude = 0.0,
                emergencyType = "test",
                customMessage = "Test message from GzingApp SOS system. This is a test to verify SMS functionality.",
                contacts = listOf(phoneNumber)
            )
            
            result.fold(
                onSuccess = { response ->
                    Log.d(TAG, "Test SMS sent successfully")
                    Result.success(SosSmsResponse(
                        status = if (response.success) "success" else "error",
                        message = response.message,
                        data = null
                    ))
                },
                onFailure = { error ->
                    Log.e(TAG, "Error sending test SMS", error)
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending test SMS", e)
            Result.failure(e)
        }
    }
    
    /**
     * Validate phone numbers
     */
    fun validatePhoneNumbers(contacts: List<SosContact>): List<SosContact> {
        return contacts.filter { contact ->
            isValidPhoneNumber(contact.phoneNumber)
        }
    }
    
    /**
     * Check if phone number is valid
     */
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic validation for Philippine phone numbers
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        return cleanNumber.matches(Regex("^\\+?63[0-9]{10}$")) || 
               cleanNumber.matches(Regex("^09[0-9]{9}$"))
    }
    
    /**
     * Format phone number for SMS
     */
    fun formatPhoneNumber(phoneNumber: String): String {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        
        return when {
            cleanNumber.startsWith("+63") -> cleanNumber
            cleanNumber.startsWith("63") -> "+$cleanNumber"
            cleanNumber.startsWith("09") -> "+63${cleanNumber.substring(1)}"
            cleanNumber.startsWith("9") -> "+63$cleanNumber"
            else -> cleanNumber
        }
    }
}
