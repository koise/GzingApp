package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("timestamp")
    val timestamp: String
)

data class HealthCheckResponse(
    @SerializedName("timestamp")
    val timestamp: String,
    
    @SerializedName("server_time")
    val serverTime: Long,
    
    @SerializedName("php_version")
    val phpVersion: String
)

data class ApiInfoResponse(
    @SerializedName("version")
    val version: String,
    
    @SerializedName("endpoints")
    val endpoints: Map<String, String>,
    
    @SerializedName("documentation")
    val documentation: String
)

data class DatabaseStatusResponse(
    @SerializedName("connected")
    val connected: Boolean,
    
    @SerializedName("tables")
    val tables: List<String>?,
    
    @SerializedName("user_count")
    val userCount: Int?,
    
    @SerializedName("message")
    val message: String
)
