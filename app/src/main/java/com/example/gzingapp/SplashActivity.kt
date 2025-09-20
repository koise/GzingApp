package com.example.gzingapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.gzingapp.repository.ApiRepository
import com.example.gzingapp.repository.AuthRepository
import com.example.gzingapp.network.RetrofitClient
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    
    private val splashTimeOut: Long = 3000 // 3 seconds
    private val apiRepository = ApiRepository()
    private lateinit var authRepository: AuthRepository
    private lateinit var appSettings: AppSettings
    
    companion object {
        private const val TAG = "SplashActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // RetrofitClient is now initialized in GzingApplication
        
        // Initialize app settings
        appSettings = AppSettings(this)
        authRepository = AuthRepository(appSettings)
        
        // Check location permissions first
        checkLocationPermissions()
    }
    
    private fun initializeApp() {
        lifecycleScope.launch {
            try {
                // Check API connection
                updateLoadingText("Checking API connection...")
                val apiResult = apiRepository.checkApiConnection()
                
                if (apiResult.isSuccess) {
                    val healthCheck = apiResult.getOrNull()
                    Log.d(TAG, "API Health Check successful")
                    Log.d(TAG, "Server Time: ${healthCheck?.serverTime}")
                    Log.d(TAG, "PHP Version: ${healthCheck?.phpVersion}")
                    Log.d(TAG, "Timestamp: ${healthCheck?.timestamp}")
                } else {
                    val error = apiResult.exceptionOrNull()
                    Log.e(TAG, "API connection failed: ${error?.message}")
                }
                
                delay(500)
                
                // Check database connection
                updateLoadingText("Checking database connection...")
                val dbResult = apiRepository.checkDatabaseConnection()
                
                if (dbResult.isSuccess) {
                    val healthStatus = dbResult.getOrNull()
                    Log.d(TAG, "Health Check Timestamp: ${healthStatus?.timestamp}")
                    Log.d(TAG, "Server Time: ${healthStatus?.serverTime}")
                    Log.d(TAG, "PHP Version: ${healthStatus?.phpVersion}")
                } else {
                    val error = dbResult.exceptionOrNull()
                    Log.e(TAG, "Database connection failed: ${error?.message}")
                }
                
                delay(500)
                
                // Get API info
                updateLoadingText("Getting API information...")
                val apiInfoResult = apiRepository.getApiInfo()
                
                if (apiInfoResult.isSuccess) {
                    val apiInfo = apiInfoResult.getOrNull()
                    Log.d(TAG, "API Version: ${apiInfo?.version ?: "Unknown"}")
                    Log.d(TAG, "Available endpoints: ${apiInfo?.endpoints?.size ?: 0}")
                } else {
                    val error = apiInfoResult.exceptionOrNull()
                    Log.e(TAG, "Failed to get API info: ${error?.message}")
                }
                
                delay(500)
                
                // Load user preferences
                updateLoadingText("Loading user preferences...")
                loadUserPreferences()
                delay(500)
                
                // Initialize app settings
                updateLoadingText("Initializing app settings...")
                appSettings.initializeAppSettings()
                delay(500)
                
                // Navigate to main activity
                updateLoadingText("Ready!")
                delay(500)
                navigateToMainActivity()
                
            } catch (e: Exception) {
                Log.e(TAG, "Initialization error: ${e.message}")
                // Still navigate to main activity even if initialization fails
                navigateToMainActivity()
            }
        }
    }
    
    private fun updateLoadingText(text: String) {
        runOnUiThread {
            findViewById<android.widget.TextView>(R.id.loadingText)?.text = text
        }
    }
    
    private fun loadUserPreferences() {
        // Load user preferences using AppSettings
        val themeMode = appSettings.getThemeMode()
        val isFirstLaunch = appSettings.isFirstLaunch()
        val userToken = appSettings.getUserToken()
        
        Log.d(TAG, "User preferences loaded:")
        Log.d(TAG, "- Theme mode: $themeMode")
        Log.d(TAG, "- First launch: $isFirstLaunch")
        Log.d(TAG, "- User logged in: ${userToken != null}")
    }
    
    private fun navigateToMainActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            // Check session validity
            lifecycleScope.launch {
                try {
                    val sessionResult = authRepository.checkSession()
                    if (sessionResult.isSuccess) {
                        val sessionData = sessionResult.getOrNull()
                        if (sessionData?.sessionActive == true) {
                            // Session is valid, save user data and go to main activity
                            appSettings.saveUserData(
                                sessionData.user.id,
                                sessionData.user.email,
                                sessionData.user.firstName,
                                sessionData.user.lastName,
                                sessionData.user.username,
                                sessionData.user.role
                            )
                            Log.d(TAG, "Valid session found, navigating to MapActivity")
                            startActivity(Intent(this@SplashActivity, MapActivity::class.java))
                        } else {
                            // Session is invalid, go to auth
                            Log.d(TAG, "No valid session, navigating to AuthActivity")
                            startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                        }
                    } else {
                        // Session check failed, go to auth
                        Log.d(TAG, "Session check failed, navigating to AuthActivity")
                        startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking session: ${e.message}")
                    // On error, go to auth
                    startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                }
                finish()
            }
        }, 1000) // Reduced delay since we're already doing initialization
    }
    
    private fun checkLocationPermissions() {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED && 
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permissions granted")
            // Check background location permission for Android 10+
            checkBackgroundLocationPermission()
        } else {
            Log.d(TAG, "Requesting location permissions")
            requestLocationPermissions()
        }
    }
    
    private fun checkBackgroundLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val backgroundLocationPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            
            if (backgroundLocationPermission == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Background location permission granted")
            } else {
                Log.d(TAG, "Background location permission not granted")
            }
        }
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notifPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (notifPerm != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
        
        // Continue with app initialization
        initializeApp()
    }
    
    private fun requestLocationPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions already granted, continue with initialization
            initializeApp()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(TAG, "Location permissions granted by user")
                    checkBackgroundLocationPermission()
                } else {
                    Log.d(TAG, "Location permissions denied by user")
                    // Continue with app initialization even if permissions are denied
                    // The map will still work, just without location features
                    initializeApp()
                }
            }
        }
    }
}
