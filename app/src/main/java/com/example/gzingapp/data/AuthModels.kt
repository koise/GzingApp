package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName

// Login Request
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

// Signup Request
data class SignupRequest(
    @SerializedName("first_name")
    val firstName: String,
    
    @SerializedName("last_name")
    val lastName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    
    @SerializedName("username")
    val username: String? = null
)

// User Model
data class User(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("first_name")
    val firstName: String,
    
    @SerializedName("last_name")
    val lastName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String?,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("created_at")
    val createdAt: String?,
    
    @SerializedName("last_login")
    val lastLogin: String?
)

// Authentication Response (Session-based)
data class AuthResponse(
    @SerializedName("user")
    val user: User,
    
    @SerializedName("session_id")
    val sessionId: String
)

// Session Check Response
data class SessionCheckResponse(
    @SerializedName("user")
    val user: User,
    
    @SerializedName("session_id")
    val sessionId: String,
    
    @SerializedName("session_active")
    val sessionActive: Boolean
)

// Validation Error Response
data class ValidationError(
    @SerializedName("errors")
    val errors: Map<String, String>
)

