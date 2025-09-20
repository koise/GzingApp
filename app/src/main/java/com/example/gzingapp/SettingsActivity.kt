package com.example.gzingapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.media.RingtoneManager
import android.media.Ringtone
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import com.example.gzingapp.utils.AppSettings
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var appSettings: AppSettings
    private var testRingtone: Ringtone? = null
    private val handler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null

    companion object {
        private const val TAG = "SettingsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        // Initialize AppSettings
        appSettings = AppSettings(this)
        
        // Setup toolbar
        setupToolbar()
        
        // Setup geofence radius setting
        setupGeofenceRadiusSetting()
        
        // Setup voice announcements setting
        setupVoiceAnnouncementsSetting()
        
        // Setup alarm sound setting
        setupAlarmSoundSetting()
        
        // Setup test buttons
        setupTestButtons()
        
        Log.d(TAG, "SettingsActivity initialized with version 1.23")
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupGeofenceRadiusSetting() {
        val fence: AutoCompleteTextView = findViewById(R.id.etAlarmFenceRadius)
        val options = listOf("50 meters", "100 meters", "150 meters", "200 meters")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
        fence.setAdapter(adapter)
        fence.setText("${appSettings.getAlarmFenceRadiusMeters()} meters", false)
        fence.setOnItemClickListener { _, _, pos, _ ->
            val value = when (pos) { 
                0 -> 50
                1 -> 100
                2 -> 150
                3 -> 200
                else -> 100 
            }
            appSettings.setAlarmFenceRadiusMeters(value)
            Log.d(TAG, "Geofence radius updated to: $value meters")
            Toast.makeText(this, "Arrival alert radius set to $value meters", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupVoiceAnnouncementsSetting() {
        val switchVoice = findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.switchVoice)
        
        // Set initial state from settings
        val isEnabled = appSettings.isVoiceAnnouncementsEnabled()
        switchVoice.isChecked = isEnabled
        Log.d(TAG, "Initial voice announcements state: $isEnabled")
        
        // Set up toggle listener
        switchVoice.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Voice switch toggled to: $isChecked")
            appSettings.setVoiceAnnouncementsEnabled(isChecked)
            Log.d(TAG, "Voice announcements ${if (isChecked) "enabled" else "disabled"}")
            
            // Show toast feedback
            val message = if (isChecked) "Voice announcements enabled" else "Voice announcements disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            
            // Initialize TTS if enabled
            if (isChecked) {
                initializeTTS()
            } else {
                // Stop and shutdown TTS if disabled
                try {
                    tts?.stop()
                    tts?.shutdown()
                    tts = null
                    Log.d(TAG, "TTS stopped and shutdown")
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping TTS", e)
                }
            }
        }
        
        // Initialize TTS if voice announcements are enabled
        if (isEnabled) {
            initializeTTS()
        }
    }
    
    private fun initializeTTS() {
        if (tts == null) {
            tts = TextToSpeech(this) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    Log.d(TAG, "TextToSpeech initialized successfully")
                } else {
                    Log.e(TAG, "TextToSpeech initialization failed")
                }
            }
        }
    }
    
    private fun setupAlarmSoundSetting() {
        val etAlarmSound: AutoCompleteTextView = findViewById(R.id.etAlarmSound)
        val ringtoneManager = RingtoneManager(this)
        ringtoneManager.setType(RingtoneManager.TYPE_ALARM)
        val cursor = ringtoneManager.cursor
        val titles = mutableListOf<String>()
        val uris = mutableListOf<String>()
        
        while (cursor.moveToNext()) {
            val notificationUri: Uri = ringtoneManager.getRingtoneUri(cursor.position)
            val title = RingtoneManager.getRingtone(this, notificationUri)?.getTitle(this) ?: "Unknown"
            titles.add(title)
            uris.add(notificationUri.toString())
        }
        cursor.close()
        
        val soundAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, titles)
        etAlarmSound.setAdapter(soundAdapter)
        
        // Preselect if saved
        val savedUri = appSettings.getAlarmSoundUri()
        if (savedUri != null) {
            val idx = uris.indexOf(savedUri)
            if (idx >= 0) etAlarmSound.setText(titles[idx], false)
        }
        
        etAlarmSound.setOnItemClickListener { _, _, pos, _ ->
            val uri = uris.getOrNull(pos)
            if (uri != null) {
                appSettings.setAlarmSoundUri(uri)
                Log.d(TAG, "Alarm sound updated to: ${titles[pos]}")
                Toast.makeText(this, "Alarm sound set to: ${titles[pos]}", Toast.LENGTH_SHORT).show()
                
                // Preview sound
                try {
                    val ring: Ringtone? = RingtoneManager.getRingtone(this, Uri.parse(uri))
                    ring?.play()
                    handler.postDelayed({
                        try { ring?.stop() } catch (_: Exception) { }
                    }, 1000L)
                } catch (e: Exception) {
                    Log.e(TAG, "Error previewing alarm sound", e)
                }
            }
        }
    }
    
    private fun setupTestButtons() {
        // Test Voice Button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnTestVoice).setOnClickListener {
            val isEnabled = appSettings.isVoiceAnnouncementsEnabled()
            Log.d(TAG, "Test voice button clicked. Voice enabled: $isEnabled")
            
            if (isEnabled && tts != null) {
                val sample = "Arriving at Destination"
                val result = tts?.speak(sample, TextToSpeech.QUEUE_FLUSH, null, "test_voice")
                Log.d(TAG, "Testing voice announcement: $sample, Result: $result")
                Toast.makeText(this, "Playing voice announcement...", Toast.LENGTH_SHORT).show()
            } else if (!isEnabled) {
                Toast.makeText(this, "Please enable voice announcements first", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Text-to-Speech not initialized. Please try again.", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "TTS is null when trying to test voice")
            }
        }
        
        // Test Alarm Button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnTestAlarm).setOnClickListener {
            // Toggle: if currently playing, stop immediately
            testRingtone?.let {
                try { it.stop() } catch (_: Exception) { }
                testRingtone = null
                Toast.makeText(this, "Alarm test stopped", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val selectedUri = appSettings.getAlarmSoundUri()
            if (selectedUri != null) {
                try {
                    testRingtone = RingtoneManager.getRingtone(this, Uri.parse(selectedUri))
                    testRingtone?.play()
                    Log.d(TAG, "Testing alarm sound")
                    Toast.makeText(this, "Playing alarm sound...", Toast.LENGTH_SHORT).show()
                    handler.postDelayed({
                        try { testRingtone?.stop() } catch (_: Exception) { }
                        testRingtone = null
                    }, 7000L)
                } catch (e: Exception) { 
                    testRingtone = null
                    Log.e(TAG, "Error testing alarm sound", e)
                    Toast.makeText(this, "Error playing alarm sound", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please select an alarm sound first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { testRingtone?.stop() } catch (_: Exception) { }
        testRingtone = null
        try { tts?.stop(); tts?.shutdown() } catch (_: Exception) { }
        Log.d(TAG, "SettingsActivity destroyed")
    }
}