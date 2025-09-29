package com.example.gzingapp.repository

import android.util.Log
import com.example.gzingapp.data.*
import com.example.gzingapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ProfileRepository(private val appSettings: com.example.gzingapp.utils.AppSettings) {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun getUserProfile(userId: Int = 33): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserProfile(userId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                val data = apiResponse?.data
                Log.d("ProfileRepository", "=== API RESPONSE DEBUG ===")
                Log.d("ProfileRepository", "Response successful: ${response.isSuccessful}")
                Log.d("ProfileRepository", "API Response: $apiResponse")
                Log.d("ProfileRepository", "Data: $data")
                if (data?.profile != null) {
                    Log.d("ProfileRepository", "Profile data: ${data.profile}")
                    Log.d("ProfileRepository", "Phone number from API: '${data.profile.phoneNumber}'")
                }
                Log.d("ProfileRepository", "=== END API RESPONSE DEBUG ===")
                
                if (apiResponse?.success == true && data != null) {
                    Result.success(data.profile)
                } else {
                    Result.failure(Exception("API returned unsuccessful response: ${apiResponse?.message}"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
    
    suspend fun updateProfile(userId: Int, profileRequest: UpdateProfileRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Add user_id and action parameter to the request
            val requestWithUserId = profileRequest.copy(userId = userId, action = "update")
            val response = apiService.updateUserProfile(UpdateUserRequest(
                userId = requestWithUserId.userId,
                action = requestWithUserId.action,
                firstName = requestWithUserId.firstName,
                lastName = requestWithUserId.lastName,
                phoneNumber = requestWithUserId.phoneNumber,
                username = requestWithUserId.username,
                notes = requestWithUserId.notes
            ))
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("API returned unsuccessful response: ${apiResponse?.message}"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
    
    suspend fun getSosContacts(userId: Int = 33): Result<List<SosContact>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSosContacts(userId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    val data = apiResponse.data
                    val contacts = when (data) {
                        is SosContactsResponse -> data.data
                        is EmergencyContactsResponse -> data.contacts
                        else -> emptyList()
                    }
                    Result.success(contacts)
                } else {
                    Result.failure(Exception("API returned unsuccessful response: ${apiResponse?.message}"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
    
    suspend fun addSosContact(userId: Int, sosContactRequest: AddSosContactRequest): Result<SosContact> = withContext(Dispatchers.IO) {
        try {
            // Add user_id and action parameter to the request
            val requestWithUserId = sosContactRequest.copy(userId = userId, action = "add")
            val response = apiService.createSosContact(CreateSosContactRequest(
                userId = requestWithUserId.userId ?: userId,
                name = requestWithUserId.name,
                phoneNumber = requestWithUserId.phoneNumber,
                relationship = requestWithUserId.relationship,
                isPrimary = requestWithUserId.isPrimary
            ))
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Result.success(apiResponse.data.contact)
                } else {
                    Result.failure(Exception("API returned unsuccessful response: ${apiResponse?.message}"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
    
    suspend fun deleteSosContact(userId: Int, contactId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val deleteRequest = mapOf("user_id" to userId.toString(), "action" to "delete")
            val response = apiService.deleteEmergencyContact(DeleteEmergencyContactRequest(contactId = contactId, permanent = false))
            
            // Log response details for debugging
            println("Delete response - Code: ${response.code()}, Success: ${response.isSuccessful}")
            println("Delete response - Headers: ${response.headers()}")
            
            if (response.isSuccessful) {
                // For delete operations, the API may return empty body with 200 status
                // Check if response body is empty
                val responseBody = response.body()
                if (responseBody == null) {
                    // Empty response body is considered success for delete operations
                    Result.success(Unit)
                } else {
                    // Try to parse JSON response
                    try {
                        if (responseBody.success == true) {
                            Result.success(Unit)
                        } else {
                            Result.failure(Exception("API returned unsuccessful response: ${responseBody.message}"))
                        }
                    } catch (e: Exception) {
                        // If JSON parsing fails but HTTP status is 200, still consider it success
                        Result.success(Unit)
                    }
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    suspend fun updateSosContact(userId: Int, contact: SosContact): Result<SosContact> = withContext(Dispatchers.IO) {
        try {
            val request = AddSosContactRequest(
                userId = userId,
                name = contact.name,
                phoneNumber = contact.phoneNumber,
                relationship = contact.relationship,
                isPrimary = contact.isPrimary,
                action = "update"
            )
            val response = apiService.updateEmergencyContact(UpdateEmergencyContactRequest(
                contactId = contact.id,
                name = contact.name,
                phoneNumber = contact.phoneNumber,
                relationship = contact.relationship,
                isPrimary = contact.isPrimary
            ))
            
            // Log response details for debugging
            println("Update response - Code: ${response.code()}, Success: ${response.isSuccessful}")
            println("Update response - Headers: ${response.headers()}")
            println("Update response - Body: ${response.body()}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Result.success(apiResponse.data.contact)
                } else {
                    Result.failure(Exception("API returned unsuccessful response: ${apiResponse?.message}"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    // Wrappers expected by ProfileActivity
    suspend fun fetchUserProfileFromAPI(userId: Int): Result<UserProfile> = getUserProfile(userId)

    suspend fun getCurrentUserProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val uid = appSettings.getUserId() ?: 33
            val first = appSettings.getFirstName() ?: "User"
            val last = appSettings.getLastName() ?: ""
            val email = appSettings.getUserEmail() ?: ""
            val username = appSettings.getUsername() ?: ""
            val role = appSettings.getUserRole() ?: "user"
            val phone = appSettings.getPhoneNumber()
            Result.success(
                UserProfile(
                    id = uid,
                    firstName = first,
                    lastName = last,
                    email = email,
                    username = username,
                    phoneNumber = phone,
                    role = role,
                    status = "active",
                    notes = null,
                    lastLogin = null,
                    createdAt = "",
                    updatedAt = "",
                    sosContactsCount = 0
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserEmergencyContacts(userId: Int): Result<EmergencyContactsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserEmergencyContacts(userId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserWithPrototypeAPI(
        userId: Int,
        firstName: String?,
        lastName: String?,
        email: String?,
        username: String?,
        phoneNumber: String?,
        role: String?
    ): Result<UpdateUsersResponse> = withContext(Dispatchers.IO) {
        try {
            val req = UpdateUsersRequest(
                userId = userId,
                firstName = firstName,
                lastName = lastName,
                email = email,
                username = username,
                phoneNumber = phoneNumber,
                role = role
            )
            val response = apiService.updateUsers(req)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    // In some APIs data may be envelope; fallback to making UpdateUsersResponse from fields too
                    Result.success(UpdateUsersResponse(username = body.data.username, role = body.data.role))
                } else if (body?.success == true) {
                    Result.success(UpdateUsersResponse(username = username, role = role ?: "user"))
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSosContact(
        userId: Int,
        name: String,
        phoneNumber: String,
        relationship: String,
        isPrimary: Boolean
    ): Result<SosContact> = withContext(Dispatchers.IO) {
        addSosContact(
            userId,
            AddSosContactRequest(userId, name, phoneNumber, relationship, isPrimary, action = "add")
        )
    }

    suspend fun updateEmergencyContact(
        contactId: Int,
        name: String,
        phoneNumber: String,
        relationship: String,
        isPrimary: Boolean?
    ): Result<EmergencyContactResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateEmergencyContact(
                UpdateEmergencyContactRequest(contactId = contactId, name = name, phoneNumber = phoneNumber, relationship = relationship, isPrimary = isPrimary)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEmergencyContact(contactId: Int, permanent: Boolean): Result<DeleteEmergencyContactResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteEmergencyContact(DeleteEmergencyContactRequest(contactId = contactId, permanent = permanent))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(userId: Int, currentPassword: String, newPassword: String): Result<ChangePasswordResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.changePassword(ChangePasswordRequest(userId, currentPassword, newPassword))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: Int, password: String): Result<DeleteUserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteUser(DeleteUserRequest(userId, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




