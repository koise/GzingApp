package com.example.gzingapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gzingapp.data.AuthResponse
import com.example.gzingapp.data.User
import com.example.gzingapp.repository.AuthRepository
import com.example.gzingapp.utils.AppSettings
import com.example.gzingapp.utils.ValidationResult
import com.example.gzingapp.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val appSettings: AppSettings
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        val isLoggedIn = appSettings.isUserLoggedIn()
        _isLoggedIn.value = isLoggedIn
        
        Log.d("AuthViewModel", "Checking login status: isLoggedIn=$isLoggedIn")
    }
    
    fun login(email: String, password: String) {
        // Validate inputs
        val emailValidation = ValidationUtils.validateEmail(email)
        val passwordValidation = ValidationUtils.validatePassword(password)
        
        if (!emailValidation.isValid || !passwordValidation.isValid) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = emailValidation.errorMessage.ifBlank { passwordValidation.errorMessage }
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            
            result.fold(
                onSuccess = { authResponse ->
                    // Save user data (session-based auth)
                    appSettings.saveUserData(
                        authResponse.user.id,
                        authResponse.user.email,
                        authResponse.user.firstName,
                        authResponse.user.lastName,
                        authResponse.user.username,
                        authResponse.user.role
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        user = authResponse.user
                    )
                    _isLoggedIn.value = true
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Login failed"
                    )
                }
            )
        }
    }
    
    fun signup(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String? = null,
        username: String? = null
    ) {
        // Validate inputs
        val firstNameValidation = ValidationUtils.validateName(firstName, "First name")
        val lastNameValidation = ValidationUtils.validateName(lastName, "Last name")
        val emailValidation = ValidationUtils.validateEmail(email)
        val passwordValidation = ValidationUtils.validatePassword(password)
        val confirmPasswordValidation = ValidationUtils.validateConfirmPassword(password, confirmPassword)
        val phoneValidation = ValidationUtils.validatePhoneNumber(phoneNumber)
        val usernameValidation = ValidationUtils.validateUsername(username)
        
        val firstError = listOf(
            firstNameValidation,
            lastNameValidation,
            emailValidation,
            passwordValidation,
            confirmPasswordValidation,
            phoneValidation,
            usernameValidation
        ).firstOrNull { !it.isValid }
        
        if (firstError != null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = firstError.errorMessage
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            val result = authRepository.signup(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                phoneNumber = phoneNumber?.takeIf { it.isNotBlank() },
                username = username?.takeIf { it.isNotBlank() }
            )
            
            result.fold(
                onSuccess = { authResponse ->
                    // Save user data (session-based auth)
                    appSettings.saveUserData(
                        authResponse.user.id,
                        authResponse.user.email,
                        authResponse.user.firstName,
                        authResponse.user.lastName,
                        authResponse.user.username,
                        authResponse.user.role
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        user = authResponse.user
                    )
                    _isLoggedIn.value = true
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Signup failed"
                    )
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                // Call logout API to invalidate session
                authRepository.logout()
            } catch (e: Exception) {
                // Continue with local logout even if API call fails
            }
            
            // Clear local session data
            appSettings.clearUserSession()
            _isLoggedIn.value = false
            _uiState.value = AuthUiState()
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val user: User? = null
)

