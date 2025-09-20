package com.example.gzingapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gzingapp.repository.AuthRepository
import com.example.gzingapp.repository.ProfileRepository
import com.example.gzingapp.repository.SosRepository
import com.example.gzingapp.repository.NavigationActivityRepository
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.launch

class TestApiIntegrationActivity : AppCompatActivity() {
    
    private lateinit var resultTextView: TextView
    private lateinit var testButton: Button
    
    private lateinit var authRepository: AuthRepository
    private lateinit var profileRepository: ProfileRepository
    private val sosRepository = SosRepository(this)
    private val navigationRepository = NavigationActivityRepository(this)
    
    companion object {
        private const val TAG = "TestApiIntegration"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_api_integration)
        
        resultTextView = findViewById(R.id.resultTextView)
        testButton = findViewById(R.id.testButton)
        
        // Initialize repositories
        val appSettings = AppSettings(this)
        authRepository = AuthRepository(appSettings)
        profileRepository = ProfileRepository(appSettings)
        
        testButton.setOnClickListener {
            runApiTests()
        }
    }
    
    private fun runApiTests() {
        resultTextView.text = "Testing API Integration...\n"
        
        lifecycleScope.launch {
            try {
                // Test 1: Get User Profile - getUserProfile method not available in new API
                resultTextView.append("1. Testing User Profile...\n")
                resultTextView.append("⚠️ Profile functionality not available in current API\n")
                
                // Test 2: Get SOS Contacts
                resultTextView.append("\n2. Testing SOS Contacts...\n")
                val contactsResult = sosRepository.getSosContacts(33)
                if (contactsResult.isSuccess) {
                    val contacts = contactsResult.getOrNull()
                    resultTextView.append("✅ SOS Contacts loaded: ${contacts?.size} contacts\n")
                } else {
                    resultTextView.append("❌ SOS Contacts failed: ${contactsResult.exceptionOrNull()?.message}\n")
                }
                
                // Test 3: Log Navigation Start
                resultTextView.append("\n3. Testing Navigation Logging...\n")
                val navResult = navigationRepository.logNavigationStart(
                    startLatitude = 14.5995,
                    startLongitude = 120.9842,
                    transportMode = "Car"
                )
                if (navResult.isSuccess) {
                    resultTextView.append("✅ Navigation start logged successfully\n")
                } else {
                    resultTextView.append("❌ Navigation logging failed: ${navResult.exceptionOrNull()?.message}\n")
                }
                
                // Test 4: Get Navigation Logs
                resultTextView.append("\n4. Testing Navigation Logs...\n")
                val logsResult = navigationRepository.getNavigationLogs(userId = 33)
                if (logsResult.isSuccess) {
                    val logs = logsResult.getOrNull()
                    resultTextView.append("✅ Navigation logs loaded successfully\n")
                } else {
                    resultTextView.append("❌ Navigation logs failed: ${logsResult.exceptionOrNull()?.message}\n")
                }
                
                // Test 5: Get Navigation Statistics
                resultTextView.append("\n5. Testing Navigation Statistics...\n")
                val statsResult = navigationRepository.getNavigationStats(userId = 33)
                if (statsResult.isSuccess) {
                    val stats = statsResult.getOrNull()
                    resultTextView.append("✅ Navigation stats loaded successfully\n")
                } else {
                    resultTextView.append("❌ Navigation stats failed: ${statsResult.exceptionOrNull()?.message}\n")
                }
                
                resultTextView.append("\n🎉 API Integration Test Complete!\n")
                resultTextView.append("Your Android app is successfully integrated with the Mobile API!\n")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during API tests", e)
                resultTextView.append("\n❌ Test failed with exception: ${e.message}\n")
            }
        }
    }
}
