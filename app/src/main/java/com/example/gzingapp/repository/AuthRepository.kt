package com.example.gzingapp.repository

import android.util.Log
import com.example.gzingapp.data.*
import com.example.gzingapp.network.RetrofitClient
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(private val appSettings: AppSettings) {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun login(email: String, password: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("AuthRepository", "Attempting login for email: $email")
            val loginRequest = LoginRequest(email, password)
            val response = apiService.login(loginRequest)
            
            Log.d("AuthRepository", "Login API response - Code: ${response.code()}, Success: ${response.isSuccessful}")
            Log.d("AuthRepository", "Login API response body: ${response.body()}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Log.d("AuthRepository", "Login successful for user: ${apiResponse.data.user.firstName} ${apiResponse.data.user.lastName}")
                    Result.success(apiResponse.data)
                } else {
                    Log.e("AuthRepository", "Login failed: ${apiResponse?.message}")
                    Result.failure(Exception("Login failed: ${apiResponse?.message}"))
                }
            } else {
                Log.e("AuthRepository", "Login HTTP error: ${response.code()} - ${response.message()}")
                handleErrorResponse(response)
            }
        } catch (e: IOException) {
            Log.e("AuthRepository", "Login network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Log.e("AuthRepository", "Login HTTP error: ${e.code()} - ${e.message()}")
            Result.failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login unexpected error: ${e.message}")
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
    
    suspend fun signup(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String? = null,
        username: String? = null
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val signupRequest = SignupRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                phoneNumber = phoneNumber,
                username = username
            )
            val response = apiService.signup(signupRequest)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception("Signup failed: ${apiResponse?.message}"))
                }
            } else {
                handleErrorResponse(response)
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
    
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Clear local session data
            appSettings.clearUserSession()
            // Clear cookies as well
            RetrofitClient.clearCookies()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Logout failed: ${e.message}"))
        }
    }
    
    suspend fun checkSession(): Result<SessionCheckResponse> = withContext(Dispatchers.IO) {
        try {
            // Check if user data exists in SharedPreferences
            val userId = appSettings.getUserId()
            val userEmail = appSettings.getUserEmail()
            
            if (userId != null && userEmail != null) {
                // User is logged in locally
                val userData = User(
                    id = userId,
                    firstName = appSettings.getFirstName() ?: "",
                    lastName = appSettings.getLastName() ?: "",
                    email = userEmail,
                    username = appSettings.getUsername() ?: "",
                    role = appSettings.getUserRole() ?: "user",
                    phoneNumber = null,
                    status = "active",
                    createdAt = "",
                    lastLogin = null
                )
                
                val sessionResponse = SessionCheckResponse(
                    user = userData,
                    sessionId = "local_session_${userId}",
                    sessionActive = true
                )
                
                Result.success(sessionResponse)
            } else {
                // No local session found
                Result.failure(Exception("No active session"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Session check failed: ${e.message}"))
        }
    }
    
    
    private fun handleErrorResponse(response: retrofit2.Response<*>): Result<Nothing> {
        return when (response.code()) {
            400 -> Result.failure(Exception("Bad request"))
            401 -> Result.failure(Exception("Invalid credentials"))
            403 -> Result.failure(Exception("Account not active"))
            404 -> Result.failure(Exception("User not found"))
            409 -> Result.failure(Exception("Email or username already exists"))
            422 -> Result.failure(Exception("Validation failed"))
            500 -> Result.failure(Exception("Server error"))
            else -> Result.failure(Exception("Unknown error: ${response.code()}"))
        }
    }
}

