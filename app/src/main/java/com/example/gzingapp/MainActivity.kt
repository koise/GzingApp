package com.example.gzingapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gzingapp.repository.AuthRepository
import com.example.gzingapp.utils.AppSettings
import com.example.gzingapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var authRepository: AuthRepository
    private lateinit var appSettings: AppSettings
    private lateinit var authViewModel: AuthViewModel
    
    // UI Components
    private lateinit var btnProfile: ImageButton
    private lateinit var btnMap: ImageButton
    private lateinit var btnLogout: ImageButton
    private lateinit var tvUserEmail: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize dependencies
        appSettings = AppSettings(this)
        authRepository = AuthRepository(appSettings)
        authViewModel = AuthViewModel(authRepository, appSettings)
        
        // Initialize UI components
        initializeViews()
        
        // Set up click listeners
        setupClickListeners()
        
        // Load user data
        loadUserData()
        
        // Check login status
        checkLoginStatus()
    }
    
    private fun initializeViews() {
        btnProfile = findViewById(R.id.btnProfile)
        btnMap = findViewById(R.id.btnMap)
        btnLogout = findViewById(R.id.btnLogout)
        tvUserEmail = findViewById(R.id.tvUserEmail)
    }
    
    private fun setupClickListeners() {
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        
        btnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
        
        btnLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun loadUserData() {
        val userEmail = appSettings.getUserEmail()
        if (userEmail != null) {
            tvUserEmail.text = "Logged in as: $userEmail"
        } else {
            tvUserEmail.text = "User not found"
        }
    }
    
    private fun checkLoginStatus() {
        lifecycleScope.launch {
            authViewModel.isLoggedIn.collect { isLoggedIn ->
                if (!isLoggedIn) {
                    logout()
                }
            }
        }
    }
    
    private fun logout() {
        authViewModel.logout()
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
