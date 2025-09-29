package com.example.gzingapp.service

import android.content.Context
import android.util.Log
import com.example.gzingapp.data.EmergencySMSRequest
import com.example.gzingapp.data.EmergencySMSResponse
import com.example.gzingapp.network.RetrofitClient
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class EmergencySMSService(private val context: Context) {
    
    private val appSettings = AppSettings(context)
    private val apiService = RetrofitClient.apiService
    
    companion object {
        private const val TAG = "EmergencySMSService"
    }
    
    /**
     * Send emergency SMS with current location
     */
    suspend fun sendEmergencySMS(
        latitude: Double,
        longitude: Double,
        emergencyType: String = "emergency",
        customMessage: String = "",
        contacts: List<String>
    ): Result<EmergencySMSResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = appSettings.getUserId() ?: 10
                
                val request = EmergencySMSRequest(
                    userId = userId,
                    latitude = latitude,
                    longitude = longitude,
                    emergencyType = emergencyType,
                    message = customMessage,
                    contacts = contacts
                )
                
                Log.d(TAG, "Sending emergency SMS for user: $userId")
                Log.d(TAG, "Location: $latitude, $longitude")
                Log.d(TAG, "Contacts: ${contacts.joinToString(", ")}")
                Log.d(TAG, "Request: $request")
                
                val response = apiService.sendEmergencySMS(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    Log.d(TAG, "Emergency SMS sent successfully")
                    Log.d(TAG, "Success count: ${result.data?.successfulSends}")
                    Log.d(TAG, "Failure count: ${result.data?.failedSends}")
                    
                    Result.success(result)
                } else {
                    val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                    Log.e(TAG, "Failed to send emergency SMS: $errorMessage")
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error body: $errorBody")
                    Log.e(TAG, "Response headers: ${response.headers()}")
                    Log.e(TAG, "Request URL: ${response.raw().request.url}")
                    Result.failure(Exception("$errorMessage - $errorBody"))
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error sending emergency SMS", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Send emergency SMS with default message
     */
    suspend fun sendEmergencySMSWithDefaultMessage(
        latitude: Double,
        longitude: Double,
        contacts: List<String>
    ): Result<EmergencySMSResponse> {
        val defaultMessage = "Something went wrong, I need immediate help! Please assist me at my current location."
        return sendEmergencySMS(latitude, longitude, "emergency", defaultMessage, contacts)
    }
    
    /**
     * Send medical emergency SMS
     */
    suspend fun sendMedicalEmergencySMS(
        latitude: Double,
        longitude: Double,
        contacts: List<String>
    ): Result<EmergencySMSResponse> {
        val medicalMessage = "Medical emergency! I need immediate medical assistance at my current location."
        return sendEmergencySMS(latitude, longitude, "medical", medicalMessage, contacts)
    }
    
    /**
     * Send security emergency SMS
     */
    suspend fun sendSecurityEmergencySMS(
        latitude: Double,
        longitude: Double,
        contacts: List<String>
    ): Result<EmergencySMSResponse> {
        val securityMessage = "Security emergency! I feel unsafe and need immediate help at my current location."
        return sendEmergencySMS(latitude, longitude, "security", securityMessage, contacts)
    }
    
    /**
     * Get emergency contacts from user settings
     */
    fun getEmergencyContacts(): List<String> {
        // This would typically come from user settings or a contacts database
        // For now, return some default contacts
        return listOf(
            "09123456789",  // Default emergency contact
            "63987654321"   // Another default contact
        )
    }
    
    /**
     * Validate phone number format
     */
    fun validatePhoneNumber(phone: String): Boolean {
        // Remove all non-numeric characters
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        
        // Check if it's a valid Philippine mobile number
        return when {
            cleanPhone.length == 10 && cleanPhone.startsWith("9") -> true
            cleanPhone.length == 11 && cleanPhone.startsWith("09") -> true
            cleanPhone.length == 12 && cleanPhone.startsWith("639") -> true
            cleanPhone.length == 13 && cleanPhone.startsWith("+639") -> true
            else -> false
        }
    }
    
    /**
     * Format phone number for SMS API
     */
    fun formatPhoneNumber(phone: String): String {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        
        return when {
            cleanPhone.length == 10 && cleanPhone.startsWith("9") -> "63$cleanPhone"
            cleanPhone.length == 11 && cleanPhone.startsWith("09") -> "63${cleanPhone.substring(1)}"
            cleanPhone.length == 12 && cleanPhone.startsWith("639") -> cleanPhone
            cleanPhone.length == 13 && cleanPhone.startsWith("+639") -> cleanPhone.substring(1)
            else -> cleanPhone
        }
    }
}
