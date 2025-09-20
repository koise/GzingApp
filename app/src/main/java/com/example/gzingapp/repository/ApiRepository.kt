package com.example.gzingapp.repository

import com.example.gzingapp.data.ApiResponse
import com.example.gzingapp.data.HealthCheckResponse
import com.example.gzingapp.data.ApiInfoResponse
import com.example.gzingapp.data.DatabaseStatusResponse
import com.example.gzingapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ApiRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun checkApiConnection(): Result<HealthCheckResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.healthCheck()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Result.success(apiResponse.data)
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
    
    suspend fun getApiInfo(): Result<ApiInfoResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getApiInfo()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Result.success(apiResponse.data)
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
    
    suspend fun checkDatabaseConnection(): Result<HealthCheckResponse> = withContext(Dispatchers.IO) {
        try {
            // Try to access the database setup endpoint to check database status
            val response = apiService.healthCheck()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception("Database check failed: ${apiResponse?.message}"))
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
}
