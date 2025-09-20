package com.example.gzingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gzingapp.repository.AuthRepository
import com.example.gzingapp.utils.AppSettings
import com.example.gzingapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    
    private lateinit var authRepository: AuthRepository
    private lateinit var appSettings: AppSettings
    private lateinit var authViewModel: AuthViewModel
    
    // UI Components
    private lateinit var loginLayout: android.view.View
    private lateinit var signupLayout: android.view.View
    
    // Login components
    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoToSignup: Button
    private lateinit var tvLoginError: TextView
    
    // Signup components
    private lateinit var etSignupFirstName: EditText
    private lateinit var etSignupLastName: EditText
    private lateinit var etSignupEmail: EditText
    private lateinit var etSignupUsername: EditText
    private lateinit var etSignupPhone: EditText
    private lateinit var etSignupPassword: EditText
    private lateinit var etSignupConfirmPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var btnGoToLogin: Button
    private lateinit var tvSignupError: TextView
    
    private var isLoginScreen = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        
        // Initialize dependencies
        appSettings = AppSettings(this)
        authRepository = AuthRepository(appSettings)
        authViewModel = AuthViewModel(authRepository, appSettings)
        
        // Initialize UI components
        initializeViews()
        
        // Set up click listeners
        setupClickListeners()
        
        // Check login status
        checkLoginStatus()
        
        // Show initial screen
        showLoginScreen()
    }
    
    private fun initializeViews() {
        loginLayout = findViewById(R.id.loginLayout)
        signupLayout = findViewById(R.id.signupLayout)
        
        // Login components
        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoToSignup = findViewById(R.id.btnGoToSignup)
        tvLoginError = findViewById(R.id.tvLoginError)
        
        // Signup components
        etSignupFirstName = findViewById(R.id.etSignupFirstName)
        etSignupLastName = findViewById(R.id.etSignupLastName)
        etSignupEmail = findViewById(R.id.etSignupEmail)
        etSignupUsername = findViewById(R.id.etSignupUsername)
        etSignupPhone = findViewById(R.id.etSignupPhone)
        etSignupPassword = findViewById(R.id.etSignupPassword)
        etSignupConfirmPassword = findViewById(R.id.etSignupConfirmPassword)
        btnSignup = findViewById(R.id.btnSignup)
        btnGoToLogin = findViewById(R.id.btnGoToLogin)
        tvSignupError = findViewById(R.id.tvSignupError)
    }
    
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etLoginEmail.text.toString().trim()
            val password = etLoginPassword.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                showLoginError("Please fill in all fields")
                return@setOnClickListener
            }
            
            login(email, password)
        }
        
        btnGoToSignup.setOnClickListener {
            showSignupScreen()
        }
        
        btnSignup.setOnClickListener {
            val firstName = etSignupFirstName.text.toString().trim()
            val lastName = etSignupLastName.text.toString().trim()
            val email = etSignupEmail.text.toString().trim()
            val username = etSignupUsername.text.toString().trim()
            val phone = etSignupPhone.text.toString().trim()
            val password = etSignupPassword.text.toString().trim()
            val confirmPassword = etSignupConfirmPassword.text.toString().trim()
            
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || 
                username.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showSignupError("Please fill in all fields")
                return@setOnClickListener
            }
            
            if (password != confirmPassword) {
                showSignupError("Passwords do not match")
                return@setOnClickListener
            }
            
            signup(firstName, lastName, email, password, confirmPassword, phone, username)
        }
        
        btnGoToLogin.setOnClickListener {
            showLoginScreen()
        }
    }
    
    private fun showLoginScreen() {
        isLoginScreen = true
        loginLayout.visibility = android.view.View.VISIBLE
        signupLayout.visibility = android.view.View.GONE
        clearErrors()
    }
    
    private fun showSignupScreen() {
        isLoginScreen = false
        loginLayout.visibility = android.view.View.GONE
        signupLayout.visibility = android.view.View.VISIBLE
        clearErrors()
    }
    
    private fun showLoginError(message: String) {
        tvLoginError.text = message
        tvLoginError.visibility = android.view.View.VISIBLE
    }
    
    private fun showSignupError(message: String) {
        tvSignupError.text = message
        tvSignupError.visibility = android.view.View.VISIBLE
    }
    
    private fun clearErrors() {
        tvLoginError.visibility = android.view.View.GONE
        tvSignupError.visibility = android.view.View.GONE
    }
    
    private fun login(email: String, password: String) {
        lifecycleScope.launch {
            try {
                authViewModel.login(email, password)
                
                // Observe the result
                authViewModel.uiState.collect { uiState ->
                    if (uiState.isLoading) {
                        btnLogin.isEnabled = false
                        btnLogin.text = "Signing In..."
                    } else {
                        btnLogin.isEnabled = true
                        btnLogin.text = "Sign In"
                        
                        if (uiState.error != null) {
                            showLoginError(uiState.error)
                        }
                    }
                }
            } catch (e: Exception) {
                showLoginError("Login failed: ${e.message}")
                btnLogin.isEnabled = true
                btnLogin.text = "Sign In"
            }
        }
    }
    
    private fun signup(firstName: String, lastName: String, email: String, 
                      password: String, confirmPassword: String, phone: String, username: String) {
        lifecycleScope.launch {
            try {
                authViewModel.signup(firstName, lastName, email, password, confirmPassword, phone, username)
                
                // Observe the result
                authViewModel.uiState.collect { uiState ->
                    if (uiState.isLoading) {
                        btnSignup.isEnabled = false
                        btnSignup.text = "Creating Account..."
                    } else {
                        btnSignup.isEnabled = true
                        btnSignup.text = "Create Account"
                        
                        if (uiState.error != null) {
                            showSignupError(uiState.error)
                        }
                    }
                }
            } catch (e: Exception) {
                showSignupError("Signup failed: ${e.message}")
                btnSignup.isEnabled = true
                btnSignup.text = "Create Account"
            }
        }
    }
    
    private fun checkLoginStatus() {
        lifecycleScope.launch {
            authViewModel.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    navigateToMain()
                }
            }
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
        finish()
    }
}

