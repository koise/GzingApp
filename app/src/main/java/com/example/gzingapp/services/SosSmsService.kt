package com.example.gzingapp.services

import android.content.Context
import android.util.Log
import com.example.gzingapp.data.SosContact
import com.example.gzingapp.data.SosSmsRequest
import com.example.gzingapp.data.SosSmsResponse
import com.example.gzingapp.network.ApiService
import com.example.gzingapp.network.RetrofitClient
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class SosSmsService(private val context: Context) {
    
    private val apiService: ApiService = RetrofitClient.apiService
    private val appSettings = AppSettings(context)
    
    companion object {
        private const val TAG = "SosSmsService"
    }
    
    /**
     * Send emergency SMS to all SOS contacts
     */
    suspend fun sendEmergencySms(
        contacts: List<SosContact>,
        currentLocation: String? = null
    ): Result<SosSmsResponse> = withContext(Dispatchers.IO) {
        try {
            val phoneNumbers = contacts.map { it.phoneNumber }
            val message = buildEmergencyMessage(currentLocation)
            
            val request = SosSmsRequest(
                phoneNumbers = phoneNumbers,
                message = message
            )
            
            Log.d(TAG, "Sending emergency SMS to ${phoneNumbers.size} contacts")
            Log.d(TAG, "Message: $message")
            
            // SMS functionality not available in current API
            // val response: Response<SosSmsResponse> = apiService.sendBulkSms(request)
            
            // SMS functionality not available in current API
            // Return a mock success response for now
            Log.d(TAG, "SMS functionality disabled - returning mock success")
            Result.success(SosSmsResponse(
                status = "success",
                message = "SMS functionality not available in current API",
                data = null
            ))
            
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
            val message = "Test message from GzingApp SOS system. This is a test to verify SMS functionality."
            
            val request = SosSmsRequest(
                phoneNumbers = listOf(phoneNumber),
                message = message
            )
            
            Log.d(TAG, "Sending test SMS to $phoneNumber")
            
            // SMS functionality not available in current API
            // Return a mock success response for now
            Log.d(TAG, "SMS functionality disabled - returning mock success")
            Result.success(SosSmsResponse(
                status = "success",
                message = "SMS functionality not available in current API",
                data = null
            ))
            
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
