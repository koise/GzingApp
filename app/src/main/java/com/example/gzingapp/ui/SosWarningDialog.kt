package com.example.gzingapp.ui

import android.app.Dialog
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.example.gzingapp.R
import com.mapbox.geojson.Point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class SosWarningDialog(
    context: Context,
    private val countdownSeconds: Int = 45,
    private val onCancelled: () -> Unit,
    private val onSendNow: () -> Unit,
    private val onAutoSend: () -> Unit,
    private val currentLocation: Point?,
    private val destinationLocation: Point?,
    private val deviationDistance: Double
) : Dialog(context) {

    private var remainingSeconds: Int = countdownSeconds
    private var countdownTimer: CountDownTimer? = null
    private var vibrator: Vibrator? = null
    private var toneGenerator: ToneGenerator? = null

    // UI Components
    private lateinit var tvCountdown: TextView
    private lateinit var tvCurrentLocation: TextView
    private lateinit var tvDestination: TextView
    private lateinit var tvDeviationDistance: TextView
    private lateinit var btnCancel: com.google.android.material.button.MaterialButton
    private lateinit var btnSendNow: com.google.android.material.button.MaterialButton

    init {
        setupDialog()
        initializeViews()
        setupClickListeners()
        updateLocationInfo()
    }

    private fun setupDialog() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_sos_warning)
        
        // Make dialog non-dismissible
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        
        // Full screen dialog
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        
        // Initialize vibrator and tone generator
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (e: Exception) {
            Log.e("SosWarningDialog", "Failed to initialize tone generator", e)
        }
    }

    private fun initializeViews() {
        tvCountdown = findViewById(R.id.tvCountdown)
        tvCurrentLocation = findViewById(R.id.tvCurrentLocation)
        tvDestination = findViewById(R.id.tvDestination)
        tvDeviationDistance = findViewById(R.id.tvDeviationDistance)
        btnCancel = findViewById(R.id.btnCancel)
        btnSendNow = findViewById(R.id.btnSendNow)
    }

    private fun setupClickListeners() {
        btnCancel.setOnClickListener {
            Log.d("SosWarningDialog", "User cancelled SOS warning")
            cancelCountdown()
            onCancelled()
            dismiss()
        }

        btnSendNow.setOnClickListener {
            Log.d("SosWarningDialog", "User manually triggered SOS send")
            cancelCountdown()
            onSendNow()
            dismiss()
        }
    }

    private fun updateLocationInfo() {
        // Update current location with reverse geocoding
        currentLocation?.let { point ->
            tvCurrentLocation.text = "Getting location..."
            performReverseGeocoding(point) { address ->
                tvCurrentLocation.text = address
            }
        } ?: run {
            tvCurrentLocation.text = "Current Location: Unknown"
        }

        // Update destination with reverse geocoding
        destinationLocation?.let { point ->
            tvDestination.text = "Getting destination..."
            performReverseGeocoding(point) { address ->
                tvDestination.text = address
            }
        } ?: run {
            tvDestination.text = "Destination: Unknown"
        }

        // Update deviation distance
        tvDeviationDistance.text = "Deviation: ${String.format("%.1f", deviationDistance)}m"
    }

    fun startCountdown() {
        Log.d("SosWarningDialog", "Starting countdown: $countdownSeconds seconds")
        
        // Play initial warning sound
        playWarningSound()
        
        // Start vibration
        vibrateDevice()
        
        countdownTimer = object : CountDownTimer((countdownSeconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = (millisUntilFinished / 1000).toInt()
                updateCountdownDisplay()
                
                // Play warning sound every 10 seconds
                if (remainingSeconds % 10 == 0 && remainingSeconds > 0) {
                    playWarningSound()
                    vibrateDevice()
                }
            }

            override fun onFinish() {
                Log.e("SosWarningDialog", "Countdown finished - triggering auto-send")
                remainingSeconds = 0
                updateCountdownDisplay()
                onAutoSend()
            }
        }
        
        countdownTimer?.start()
    }

    fun cancelCountdown() {
        Log.d("SosWarningDialog", "Cancelling countdown")
        countdownTimer?.cancel()
        countdownTimer = null
    }

    fun sendNow() {
        Log.d("SosWarningDialog", "Manual send now triggered")
        cancelCountdown()
        onSendNow()
    }

    private fun updateCountdownDisplay() {
        tvCountdown.text = remainingSeconds.toString()
        
        // Change color based on remaining time
        val color = when {
            remainingSeconds <= 10 -> "#E74C3C" // Red for last 10 seconds
            remainingSeconds <= 20 -> "#F39C12" // Orange for last 20 seconds
            else -> "#E74C3C" // Red for normal countdown
        }
        
        tvCountdown.setTextColor(android.graphics.Color.parseColor(color))
    }

    private fun playWarningSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
        } catch (e: Exception) {
            Log.e("SosWarningDialog", "Error playing warning sound", e)
        }
    }

    private fun vibrateDevice() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (e: Exception) {
            Log.e("SosWarningDialog", "Error vibrating device", e)
        }
    }

    override fun dismiss() {
        cancelCountdown()
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            Log.e("SosWarningDialog", "Error releasing tone generator", e)
        }
        super.dismiss()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelCountdown()
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            Log.e("SosWarningDialog", "Error releasing tone generator", e)
        }
    }

    // ==================== REVERSE GEOCODING METHODS ====================
    
    private fun performReverseGeocoding(point: Point, onResult: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val address = getAddressFromCoordinates(point.latitude(), point.longitude())
                withContext(Dispatchers.Main) {
                    onResult(address)
                }
            } catch (e: Exception) {
                Log.e("SosWarningDialog", "Error in reverse geocoding", e)
                withContext(Dispatchers.Main) {
                    val fallback = "Location: ${String.format("%.4f, %.4f", point.latitude(), point.longitude())}"
                    onResult(fallback)
                }
            }
        }
    }
    
    private suspend fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        Log.d("SosWarningDialog", "=== GET ADDRESS FROM COORDINATES ===")
        Log.d("SosWarningDialog", "Input coordinates: Lat=$lat, Lng=$lng")
        Log.d("SosWarningDialog", "Using OpenStreetMap Nominatim API for reverse geocoding")
        
        return withContext(Dispatchers.IO) {
            try {
                Log.d("SosWarningDialog", "Starting reverse geocoding API call")
                // Using OpenStreetMap Nominatim API for reverse geocoding (free)
                val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&zoom=18&addressdetails=1"
                Log.d("SosWarningDialog", "Reverse geocoding URL: $url")
                val connection = URL(url).openConnection()
                connection.setRequestProperty("User-Agent", "GzingApp/1.0")
                Log.d("SosWarningDialog", "Making HTTP request to Nominatim API")
                
                val response = connection.getInputStream().bufferedReader().use { it.readText() }
                Log.d("SosWarningDialog", "Received response from Nominatim API")
                Log.d("SosWarningDialog", "Response length: ${response.length} characters")
                val jsonObject = JSONObject(response)
                Log.d("SosWarningDialog", "Parsed JSON response successfully")
                
                val displayName = jsonObject.optString("display_name", "")
                Log.d("SosWarningDialog", "Display name from API: $displayName")
                val address = jsonObject.optJSONObject("address")
                Log.d("SosWarningDialog", "Address object from API: $address")
                
                if (displayName.isNotEmpty()) {
                    // Format the address nicely
                    val parts = displayName.split(", ")
                    if (parts.size >= 3) {
                        val formattedAddress = "${parts[0]}, ${parts[1]}, ${parts[2]}"
                        Log.d("SosWarningDialog", "Formatted address: $formattedAddress")
                        formattedAddress
                    } else {
                        Log.d("SosWarningDialog", "Using full display name: $displayName")
                        displayName
                    }
                } else {
                    val fallback = "Location: ${String.format("%.4f, %.4f", lat, lng)}"
                    Log.d("SosWarningDialog", "No display name found, using fallback: $fallback")
                    fallback
                }
            } catch (e: Exception) {
                Log.e("SosWarningDialog", "Error in reverse geocoding", e)
                val fallback = "Location: ${String.format("%.4f, %.4f", lat, lng)}"
                Log.d("SosWarningDialog", "Exception occurred, using fallback: $fallback")
                fallback
            }
        }
    }
}



