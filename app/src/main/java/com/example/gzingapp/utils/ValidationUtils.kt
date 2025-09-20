package com.example.gzingapp.utils

import android.util.Patterns

object ValidationUtils {
    
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult(false, "Please enter a valid email address")
            else -> ValidationResult(true, "")
        }
    }
    
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password is required")
            password.length < 6 -> ValidationResult(false, "Password must be at least 6 characters")
            else -> ValidationResult(true, "")
        }
    }
    
    fun validateName(name: String, fieldName: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "$fieldName is required")
            name.length < 2 -> ValidationResult(false, "$fieldName must be at least 2 characters")
            name.length > 50 -> ValidationResult(false, "$fieldName must be less than 50 characters")
            !name.matches(Regex("^[a-zA-Z\\s\\-']+$")) -> ValidationResult(false, "$fieldName can only contain letters, spaces, hyphens, and apostrophes")
            else -> ValidationResult(true, "")
        }
    }
    
    fun validatePhoneNumber(phoneNumber: String?): ValidationResult {
        if (phoneNumber.isNullOrBlank()) {
            return ValidationResult(true, "") // Phone number is optional
        }
        
        return when {
            !phoneNumber.matches(Regex("^\\+639[0-9]{9}$")) -> ValidationResult(false, "Phone number must be in format +639XXXXXXXXX")
            else -> ValidationResult(true, "")
        }
    }
    
    fun validateUsername(username: String?): ValidationResult {
        if (username.isNullOrBlank()) {
            return ValidationResult(true, "") // Username is optional
        }
        
        return when {
            username.length < 3 -> ValidationResult(false, "Username must be at least 3 characters")
            username.length > 20 -> ValidationResult(false, "Username must be less than 20 characters")
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> ValidationResult(false, "Username can only contain letters, numbers, and underscores")
            else -> ValidationResult(true, "")
        }
    }
    
    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult(false, "Please confirm your password")
            password != confirmPassword -> ValidationResult(false, "Passwords do not match")
            else -> ValidationResult(true, "")
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)

