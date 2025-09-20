package com.example.gzingapp.repository

import android.content.Context
import android.util.Log
import com.example.gzingapp.data.ApiResponse
import com.example.gzingapp.data.SosContact
import com.example.gzingapp.data.SosContactsResponse
import com.example.gzingapp.network.ApiService
import com.example.gzingapp.network.RetrofitClient
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class SosRepository(private val context: Context) {
    
    private val apiService: ApiService = RetrofitClient.apiService
    private val appSettings = AppSettings(context)
    
    companion object {
        private const val TAG = "SosRepository"
    }
    
    /**
     * Fetch SOS contacts for the current user using new emergency contact API
     */
    suspend fun getSosContacts(userId: Int? = null): Result<List<SosContact>> = withContext(Dispatchers.IO) {
        val actualUserId = userId ?: appSettings.getUserId() ?: 33
        try {
            Log.d(TAG, "Fetching emergency contacts for user $actualUserId...")
            
            // First try the new emergency contacts API
            val emergencyResponse: Response<ApiResponse<com.example.gzingapp.data.EmergencyContactsResponse>> = apiService.getUserEmergencyContacts(actualUserId)
            
            if (emergencyResponse.isSuccessful) {
                val emergencyContactsResponse = emergencyResponse.body()
                if (emergencyContactsResponse?.success == true) {
                    val contacts = emergencyContactsResponse.data?.contacts ?: emptyList()
                    Log.d(TAG, "Successfully fetched ${contacts.size} emergency contacts from new API")
                    Result.success(contacts)
                } else {
                    Log.w(TAG, "New API returned error, falling back to old API: ${emergencyContactsResponse?.message}")
                    // Fallback to old API
                    getSosContactsFromOldApi(actualUserId)
                }
            } else {
                Log.w(TAG, "New API call failed, falling back to old API: ${emergencyResponse.code()} - ${emergencyResponse.message()}")
                // Fallback to old API
                getSosContactsFromOldApi(actualUserId)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching emergency contacts, falling back to old API", e)
            // Fallback to old API
            getSosContactsFromOldApi(actualUserId)
        }
    }
    
    /**
     * Fallback method to fetch SOS contacts from old API
     */
    private suspend fun getSosContactsFromOldApi(userId: Int): Result<List<SosContact>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching SOS contacts from old API for user $userId...")
            
            val response: Response<ApiResponse<SosContactsResponse>> = apiService.getSosContacts(userId)
            
            if (response.isSuccessful) {
                val contactsResponse = response.body()
                if (contactsResponse?.success == true) {
                    val contacts = (contactsResponse.data as? SosContactsResponse)?.data ?: emptyList()
                    Log.d(TAG, "Successfully fetched ${contacts.size} SOS contacts from old API")
                    Result.success(contacts)
                } else {
                    Log.e(TAG, "Old API returned error: ${contactsResponse?.message}")
                    Result.failure(Exception(contactsResponse?.message ?: "Unknown error"))
                }
            } else {
                Log.e(TAG, "Old API call failed: ${response.code()} - ${response.message()}")
                when (response.code()) {
                    401 -> Result.failure(Exception("Authentication failed. Please login again."))
                    404 -> Result.failure(Exception("No SOS contacts found"))
                    else -> Result.failure(Exception("Failed to fetch SOS contacts: ${response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching SOS contacts from old API", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get primary SOS contact
     */
    suspend fun getPrimarySosContact(userId: Int? = null): Result<SosContact?> = withContext(Dispatchers.IO) {
        try {
            val result = getSosContacts(userId)
            if (result.isSuccess) {
                val contacts = result.getOrNull() ?: emptyList()
                val primaryContact = contacts.find { it.isPrimary }
                Log.d(TAG, "Primary contact: ${primaryContact?.name ?: "None"}")
                Result.success(primaryContact)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to get contacts"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting primary SOS contact", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if user has any SOS contacts
     */
    suspend fun hasSosContacts(userId: Int? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val result = getSosContacts(userId)
            if (result.isSuccess) {
                val contacts = result.getOrNull() ?: emptyList()
                val hasContacts = contacts.isNotEmpty()
                Log.d(TAG, "User has SOS contacts: $hasContacts")
                Result.success(hasContacts)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to check contacts"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking SOS contacts", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get SOS contacts count
     */
    suspend fun getSosContactsCount(userId: Int? = null): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val result = getSosContacts(userId)
            if (result.isSuccess) {
                val contacts = result.getOrNull() ?: emptyList()
                val count = contacts.size
                Log.d(TAG, "SOS contacts count: $count")
                Result.success(count)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to get contacts count"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting SOS contacts count", e)
            Result.failure(e)
        }
    }
    
    /**
     * Validate SOS contacts data
     */
    fun validateSosContacts(contacts: List<SosContact>): List<SosContact> {
        return contacts.filter { contact ->
            contact.name.isNotBlank() && 
            contact.phoneNumber.isNotBlank() && 
            contact.relationship.isNotBlank()
        }
    }
    
    /**
     * Get emergency-ready contacts (with valid phone numbers)
     */
    fun getEmergencyReadyContacts(contacts: List<SosContact>): List<SosContact> {
        return contacts.filter { contact ->
            isValidPhoneNumber(contact.phoneNumber)
        }
    }
    
    /**
     * Check if phone number is valid for emergency use
     */
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        return cleanNumber.matches(Regex("^\\+?63[0-9]{10}$")) || 
               cleanNumber.matches(Regex("^09[0-9]{9}$"))
    }
}
