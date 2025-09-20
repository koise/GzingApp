package com.example.gzingapp.ui

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
    private val onDismiss: (() -> Unit)? = null
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
        
        // Get current location
        getCurrentLocation()
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
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLocation = "${location.latitude}, ${location.longitude}"
                tvCurrentLocation.text = "Current Location: ${String.format("%.4f, %.4f", location.latitude, location.longitude)}"
            } else {
                tvCurrentLocation.text = "Current Location: Unable to determine"
            }
        }.addOnFailureListener {
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
                val result = withContext(Dispatchers.IO) {
                    sosSmsService.sendEmergencySms(sosContacts, currentLocation)
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
