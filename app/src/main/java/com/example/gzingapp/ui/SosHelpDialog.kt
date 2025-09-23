package com.example.gzingapp.ui

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gzingapp.R
import com.example.gzingapp.adapter.SosEmergencyAdapter
import com.example.gzingapp.data.SosContact
import com.example.gzingapp.repository.SosRepository
import com.example.gzingapp.services.SosSmsService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class SosHelpDialog(
    private val context: Context,
    private val onSosSent: (() -> Unit)? = null,
    private val onDismiss: (() -> Unit)? = null,
    private val initialLatitude: Double? = null,
    private val initialLongitude: Double? = null,
    private val initialLocation: String? = null
) : Dialog(context) {
    
    private lateinit var tvCurrentLocation: TextView
    private lateinit var rvSosContacts: RecyclerView
    private lateinit var layoutLoading: LinearLayout
    private lateinit var layoutNoContacts: LinearLayout
    private lateinit var btnCancelSos: Button
    private lateinit var btnSendSos: Button
    private lateinit var btnCloseSos: View
    
    private lateinit var sosAdapter: SosEmergencyAdapter
    private lateinit var sosRepository: SosRepository
    private lateinit var sosSmsService: SosSmsService
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var sosContacts: List<SosContact> = emptyList()
    private var currentLocation: String? = null
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null
    
    // Vibration control
    private var vibrator: Vibrator? = null
    private var vibrationJob: kotlinx.coroutines.Job? = null
    
    companion object {
        private const val TAG = "SosHelpDialog"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set dialog properties
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        setContentView(R.layout.dialog_sos_help)
        
        // Initialize components
        initializeViews()
        initializeServices()
        setupRecyclerView()
        setupClickListeners()
        
        // Start vibration
        startVibration()
        
        // Load SOS contacts
        loadSosContacts()
        
        // Use initial coordinates if provided, otherwise get current location
        if (initialLatitude != null && initialLongitude != null) {
            currentLatitude = initialLatitude
            currentLongitude = initialLongitude
            currentLocation = initialLocation ?: "${initialLatitude}, ${initialLongitude}"
            tvCurrentLocation.text = "Current Location: ${String.format("%.4f, %.4f", initialLatitude, initialLongitude)}"
            Log.d(TAG, "Using provided coordinates: $currentLatitude, $currentLongitude")
            Log.d(TAG, "Provided location: $currentLocation")
        } else {
            // Get current location
            getCurrentLocation()
        }
    }
    
    private fun initializeViews() {
        tvCurrentLocation = findViewById(R.id.tvCurrentLocation)
        rvSosContacts = findViewById(R.id.rvSosContacts)
        layoutLoading = findViewById(R.id.layoutLoading)
        layoutNoContacts = findViewById(R.id.layoutNoContacts)
        btnCancelSos = findViewById(R.id.btnCancelSos)
        btnSendSos = findViewById(R.id.btnSendSos)
        btnCloseSos = findViewById(R.id.btnCloseSos)
    }
    
    private fun initializeServices() {
        sosRepository = SosRepository(context)
        sosSmsService = SosSmsService(context)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        // Initialize vibrator
        try {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } catch (e: Exception) {
            // Vibrator not available
        }
    }
    
    private fun setupRecyclerView() {
        sosAdapter = SosEmergencyAdapter()
        rvSosContacts.layoutManager = LinearLayoutManager(context)
        rvSosContacts.adapter = sosAdapter
    }
    
    private fun setupClickListeners() {
        btnCancelSos.setOnClickListener {
            dismiss()
        }
        
        btnCloseSos.setOnClickListener {
            dismiss()
        }
        
        btnSendSos.setOnClickListener {
            sendEmergencySms()
        }
    }
    
    private fun startVibration() {
        vibrationJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                while (isActive) {
                    vibrator?.let { v ->
                        if (v.hasVibrator()) {
                            // Create emergency vibration pattern: short, pause, short, pause, long
                            val pattern = longArrayOf(0, 200, 100, 200, 100, 500)
                            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                            
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                val vibrationEffect = VibrationEffect.createWaveform(pattern, amplitudes, 0)
                                v.vibrate(vibrationEffect)
                            } else {
                                @Suppress("DEPRECATION")
                                v.vibrate(pattern, 0)
                            }
                        }
                    }
                    delay(1000) // Repeat every second
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error in vibration loop", e)
            }
        }
    }
    
    private fun loadSosContacts() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                layoutLoading.visibility = View.VISIBLE
                layoutNoContacts.visibility = View.GONE
                rvSosContacts.visibility = View.GONE
                
                val result = withContext(Dispatchers.IO) {
                    sosRepository.getSosContacts()
                }
                
                if (result.isSuccess) {
                    sosContacts = result.getOrNull() ?: emptyList()
                    val validContacts = sosRepository.getEmergencyReadyContacts(sosContacts)
                    
                    if (validContacts.isNotEmpty()) {
                        sosAdapter.updateContacts(validContacts)
                        rvSosContacts.visibility = View.VISIBLE
                        layoutNoContacts.visibility = View.GONE
                        btnSendSos.isEnabled = true
                        btnSendSos.text = "SEND SOS (${validContacts.size})"
                    } else {
                        layoutNoContacts.visibility = View.VISIBLE
                        rvSosContacts.visibility = View.GONE
                        btnSendSos.isEnabled = false
                        btnSendSos.text = "NO CONTACTS"
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Toast.makeText(context, "Failed to load contacts: $error", Toast.LENGTH_LONG).show()
                    layoutNoContacts.visibility = View.VISIBLE
                    rvSosContacts.visibility = View.GONE
                    btnSendSos.isEnabled = false
                    btnSendSos.text = "ERROR"
                }
                
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error loading SOS contacts", e)
                Toast.makeText(context, "Error loading contacts", Toast.LENGTH_SHORT).show()
                layoutNoContacts.visibility = View.VISIBLE
                rvSosContacts.visibility = View.GONE
                btnSendSos.isEnabled = false
                btnSendSos.text = "ERROR"
            } finally {
                layoutLoading.visibility = View.GONE
            }
        }
    }
    
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            tvCurrentLocation.text = "Location: Permission required"
            return
        }
        
        Log.d(TAG, "Requesting current location...")
        
        // First try to get last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.d(TAG, "Last known location: ${location.latitude}, ${location.longitude}")
                Log.d(TAG, "Location accuracy: ${location.accuracy}m")
                Log.d(TAG, "Location time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(location.time))}")
                
                // Accept any valid location (not just non-zero coordinates)
                if (location.latitude != 0.0 || location.longitude != 0.0) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    currentLocation = "${location.latitude}, ${location.longitude}"
                    tvCurrentLocation.text = "Current Location: ${String.format("%.4f, %.4f", location.latitude, location.longitude)}"
                    Log.d(TAG, "✅ GPS location captured: $currentLatitude, $currentLongitude")
                } else {
                    Log.w(TAG, "Last known location has zero coordinates, requesting fresh location...")
                    requestFreshLocation()
                }
            } else {
                Log.w(TAG, "No last known location, requesting fresh location...")
                requestFreshLocation()
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Failed to get last known location", exception)
            requestFreshLocation()
        }
    }
    
    private fun requestFreshLocation() {
        Log.d(TAG, "Requesting fresh GPS location...")
        
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds timeout
        ).apply {
            setMinUpdateIntervalMillis(1000L)
            setMaxUpdateDelayMillis(15000L)
        }.build()
        
        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "Fresh location received: ${location.latitude}, ${location.longitude}")
                    Log.d(TAG, "Location accuracy: ${location.accuracy}m")
                    
                    // Accept any valid location
                    if (location.latitude != 0.0 || location.longitude != 0.0) {
                        currentLatitude = location.latitude
                        currentLongitude = location.longitude
                        currentLocation = "${location.latitude}, ${location.longitude}"
                        tvCurrentLocation.text = "Current Location: ${String.format("%.4f, %.4f", location.latitude, location.longitude)}"
                        Log.d(TAG, "✅ Fresh GPS location captured: $currentLatitude, $currentLongitude")
                        
                        // Stop location updates after getting one good location
                        fusedLocationClient.removeLocationUpdates(this)
                    } else {
                        Log.w(TAG, "Fresh location has zero coordinates")
                        currentLatitude = null
                        currentLongitude = null
                        tvCurrentLocation.text = "Current Location: GPS coordinates invalid"
                    }
                } ?: run {
                    Log.w(TAG, "No location in fresh location result")
                    currentLatitude = null
                    currentLongitude = null
                    tvCurrentLocation.text = "Current Location: GPS not available"
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                android.os.Looper.getMainLooper()
            )
            
            // Stop location updates after 15 seconds to prevent battery drain
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    Log.d(TAG, "Location updates stopped after timeout")
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping location updates", e)
                }
            }, 15000)
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception requesting location updates", e)
            currentLatitude = null
            currentLongitude = null
            tvCurrentLocation.text = "Current Location: Permission denied"
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting location updates", e)
            currentLatitude = null
            currentLongitude = null
            tvCurrentLocation.text = "Current Location: Error getting location"
        }
    }
    
    private fun sendEmergencySms() {
        if (sosContacts.isEmpty()) {
            Toast.makeText(context, "No emergency contacts available", Toast.LENGTH_SHORT).show()
            return
        }
        
        btnSendSos.isEnabled = false
        btnSendSos.text = "SENDING..."
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Sending emergency SMS with:")
                Log.d(TAG, "Contacts: ${sosContacts.size}")
                Log.d(TAG, "Current Location: $currentLocation")
                Log.d(TAG, "Latitude: $currentLatitude")
                Log.d(TAG, "Longitude: $currentLongitude")
                
                val result = withContext(Dispatchers.IO) {
                    sosSmsService.sendEmergencySms(
                        contacts = sosContacts, 
                        currentLocation = currentLocation,
                        latitude = currentLatitude,
                        longitude = currentLongitude
                    )
                }
                
                if (result.isSuccess) {
                    Toast.makeText(context, "Emergency SMS sent successfully!", Toast.LENGTH_LONG).show()
                    onSosSent?.invoke()
                    dismiss()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Toast.makeText(context, "Failed to send SMS: $error", Toast.LENGTH_LONG).show()
                    btnSendSos.isEnabled = true
                    btnSendSos.text = "RETRY SEND"
                }
                
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error sending emergency SMS", e)
                Toast.makeText(context, "Error sending emergency SMS", Toast.LENGTH_SHORT).show()
                btnSendSos.isEnabled = true
                btnSendSos.text = "RETRY SEND"
            }
        }
    }
    
    private fun stopVibration() {
        vibrationJob?.cancel()
        vibrationJob = null
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error stopping vibration", e)
        }
    }
    
    override fun dismiss() {
        stopVibration()
        super.dismiss()
        onDismiss?.invoke()
    }
}
