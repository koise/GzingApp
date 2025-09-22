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
                    
                    // Save session data locally
                    appSettings.saveUserData(
                        apiResponse.data.user.id,
                        apiResponse.data.user.email,
                        apiResponse.data.user.firstName,
                        apiResponse.data.user.lastName,
                        apiResponse.data.user.username,
                        apiResponse.data.user.role
                    )
                    
                    Result.success(apiResponse.data)
                } else {
                    val errorMessage = apiResponse?.message ?: "Login failed"
                    Log.e("AuthRepository", "Login failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                Log.e("AuthRepository", "Login HTTP error: ${response.code()} - ${response.message()}")
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Error response body: $errorBody")
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
            Log.d("AuthRepository", "Attempting signup for email: $email")
            val signupRequest = SignupRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                phoneNumber = phoneNumber,
                username = username
            )
            val response = apiService.signup(signupRequest)
            
            Log.d("AuthRepository", "Signup API response - Code: ${response.code()}, Success: ${response.isSuccessful}")
            Log.d("AuthRepository", "Signup API response body: ${response.body()}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Log.d("AuthRepository", "Signup successful for user: ${apiResponse.data.user.firstName} ${apiResponse.data.user.lastName}")
                    
                    // Save session data locally
                    appSettings.saveUserData(
                        apiResponse.data.user.id,
                        apiResponse.data.user.email,
                        apiResponse.data.user.firstName,
                        apiResponse.data.user.lastName,
                        apiResponse.data.user.username,
                        apiResponse.data.user.role
                    )
                    
                    Result.success(apiResponse.data)
                } else {
                    val errorMessage = apiResponse?.message ?: "Signup failed"
                    Log.e("AuthRepository", "Signup failed: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                Log.e("AuthRepository", "Signup HTTP error: ${response.code()} - ${response.message()}")
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Error response body: $errorBody")
                handleErrorResponse(response)
            }
        } catch (e: IOException) {
            Log.e("AuthRepository", "Signup network error: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Log.e("AuthRepository", "Signup HTTP error: ${e.code()} - ${e.message()}")
            Result.failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Signup unexpected error: ${e.message}")
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
    
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("AuthRepository", "Attempting logout")
            
            // Call logout API endpoint
            val response = apiService.logout()
            
            Log.d("AuthRepository", "Logout API response - Code: ${response.code()}, Success: ${response.isSuccessful}")
            
            // Clear local session data regardless of API response
            appSettings.clearUserSession()
            // Clear cookies as well
            RetrofitClient.clearCookies()
            
            if (response.isSuccessful) {
                Log.d("AuthRepository", "Logout successful")
                Result.success(Unit)
            } else {
                Log.w("AuthRepository", "Logout API failed but local session cleared")
                Result.success(Unit) // Still consider it successful since we cleared local data
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Logout error: ${e.message}")
            // Clear local session data even if API call fails
            appSettings.clearUserSession()
            RetrofitClient.clearCookies()
            Result.success(Unit) // Consider it successful since we cleared local data
        }
    }
    
    suspend fun checkSession(): Result<SessionCheckResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("AuthRepository", "Checking session with API")
            
            // Call the session check API endpoint
            val response = apiService.checkSession()
            
            Log.d("AuthRepository", "Session check API response - Code: ${response.code()}, Success: ${response.isSuccessful}")
            Log.d("AuthRepository", "Session check API response body: ${response.body()}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Log.d("AuthRepository", "Session is valid for user: ${apiResponse.data.user.firstName} ${apiResponse.data.user.lastName}")
                    
                    // Update local session data with fresh data from server
                    appSettings.saveUserData(
                        apiResponse.data.user.id,
                        apiResponse.data.user.email,
                        apiResponse.data.user.firstName,
                        apiResponse.data.user.lastName,
                        apiResponse.data.user.username,
                        apiResponse.data.user.role
                    )
                    
                    Result.success(apiResponse.data)
                } else {
                    Log.e("AuthRepository", "Session check failed: ${apiResponse?.message}")
                    // Clear local session if server says it's invalid
                    appSettings.clearUserSession()
                    Result.failure(Exception(apiResponse?.message ?: "Session expired"))
                }
            } else {
                Log.e("AuthRepository", "Session check HTTP error: ${response.code()} - ${response.message()}")
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Error response body: $errorBody")
                
                // Clear local session if server says it's invalid
                appSettings.clearUserSession()
                handleErrorResponse(response)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Session check error: ${e.message}")
            // Clear local session on any error
            appSettings.clearUserSession()
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

