package com.example.gzingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gzingapp.ui.adapters.SosContactsAdapter
import com.example.gzingapp.data.AddSosContactRequest
import com.example.gzingapp.data.SosContact
import com.example.gzingapp.data.UpdateProfileRequest
import com.example.gzingapp.data.UserProfile
import com.example.gzingapp.repository.ProfileRepository
import com.example.gzingapp.utils.AppSettings
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var profileRepository: ProfileRepository
    private lateinit var appSettings: AppSettings
    
    // UI Components
    private lateinit var avatarProfile: com.example.gzingapp.ui.components.InitialsAvatar
    private lateinit var tvProfileName: TextView
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnEditMode: Button
    private lateinit var btnSaveProfile: Button
    private lateinit var btnAddContact: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnCancelEdit: Button
    private lateinit var layoutActionButtons: android.widget.LinearLayout
    private lateinit var recyclerViewContacts: RecyclerView
    private lateinit var contactsAdapter: SosContactsAdapter
    
    private var userProfile: UserProfile? = null
    private var sosContacts: List<SosContact> = emptyList()
    private var userId: Int = 0 // Will be set from authenticated user session
    private var isEditMode: Boolean = false
    
    companion object {
        private const val TAG = "ProfileActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        // Initialize components
        appSettings = AppSettings(this)
        profileRepository = ProfileRepository(appSettings)
        userId = appSettings.getUserId() ?: 0
        
        // Check if user is authenticated
        if (userId == 0) {
            Log.e(TAG, "No authenticated user found, redirecting to login")
            Toast.makeText(this, "Please log in to access your profile", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        Log.d(TAG, "ProfileActivity initialized for user ID: $userId")
        
        // Initialize UI components
        initializeViews()
        
        // Set up click listeners
        setupClickListeners()
        
        // Load user data
        loadUserData()
    }
    
    private fun initializeViews() {
        avatarProfile = findViewById(R.id.avatarProfile)
        tvProfileName = findViewById(R.id.tvProfileName)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        btnEditMode = findViewById(R.id.btnEditMode)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        btnAddContact = findViewById(R.id.btnAddContact)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        btnCancelEdit = findViewById(R.id.btnCancelEdit)
        layoutActionButtons = findViewById(R.id.layoutActionButtons)
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts)
        
        // Setup back button
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressed()
        }
        
        // Setup RecyclerView
        contactsAdapter = SosContactsAdapter()
        contactsAdapter.setOnEditClickListener { contact ->
            editContact(contact)
        }
        contactsAdapter.setOnDeleteClickListener { contact ->
            deleteContact(contact)
        }
        recyclerViewContacts.layoutManager = LinearLayoutManager(this)
        recyclerViewContacts.adapter = contactsAdapter
    }
    
    private fun setupClickListeners() {
        btnEditMode.setOnClickListener {
            enableEditMode()
        }
        
        btnCancelEdit.setOnClickListener {
            disableEditMode()
        }
        
        btnSaveProfile.setOnClickListener {
            saveProfile()
        }
        
        btnAddContact.setOnClickListener {
            showAddContactDialog()
        }
        
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
        
        btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
        
    }
    
    private fun loadUserData() {
        Log.d(TAG, "Loading user data for user ID: $userId")
        
        lifecycleScope.launch {
            try {
                // First try to fetch fresh data from API
                profileRepository.fetchUserProfileFromAPI(userId).fold(
                    onSuccess = { user ->
                        Log.d(TAG, "Successfully fetched user profile from API: ${user.firstName} ${user.lastName}")
                        userProfile = UserProfile(
                            id = user.id,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            email = user.email,
                            username = user.username,
                            phoneNumber = user.phoneNumber,
                            role = user.role,
                            status = user.status,
                            notes = user.notes,
                            lastLogin = user.lastLogin,
                            createdAt = user.createdAt ?: "",
                            updatedAt = user.updatedAt ?: "",
                            sosContactsCount = 0
                        )
                        Log.d(TAG, "User profile created from API - Phone: ${user.phoneNumber}")
                        updateProfileFields(userProfile!!)
                        
                        // Update local storage with fresh data from API
                        appSettings.saveUserData(
                            userId = user.id,
                            email = user.email,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            username = user.username,
                            role = user.role,
                            phoneNumber = user.phoneNumber
                        )
                    },
                    onFailure = { apiError ->
                        Log.w(TAG, "Failed to fetch from API, using local data: ${apiError.message}")
                        // Fallback to local data from SharedPreferences
                        profileRepository.getCurrentUserProfile().fold(
                            onSuccess = { user ->
                                Log.d(TAG, "Using local user profile: ${user.firstName} ${user.lastName}")
                                userProfile = UserProfile(
                                    id = user.id,
                                    firstName = user.firstName,
                                    lastName = user.lastName,
                                    email = user.email,
                                    username = user.username,
                                    phoneNumber = user.phoneNumber,
                                    role = user.role,
                                    status = user.status,
                                    notes = user.notes,
                                    lastLogin = user.lastLogin,
                                    createdAt = user.createdAt ?: "",
                                    updatedAt = user.updatedAt ?: "",
                                    sosContactsCount = 0
                                )
                                Log.d(TAG, "User profile created from local storage - Phone: ${user.phoneNumber}")
                                updateProfileFields(userProfile!!)
                            },
                            onFailure = { localError ->
                                Log.e(TAG, "Failed to load profile: ${localError.message}")
                                Toast.makeText(this@ProfileActivity, 
                                    "Failed to load profile: ${localError.message}", 
                                    Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
                
                // Load Emergency contacts using new API
                profileRepository.getUserEmergencyContacts(userId).fold(
                    onSuccess = { emergencyContactsResponse ->
                        sosContacts = emergencyContactsResponse.contacts
                        contactsAdapter.updateContacts(emergencyContactsResponse.contacts)
                        Log.d("ProfileActivity", "Loaded ${emergencyContactsResponse.contacts.size} emergency contacts (Total: ${emergencyContactsResponse.totalContacts})")
                    },
                    onFailure = { error ->
                        Log.w("ProfileActivity", "Failed to load emergency contacts, falling back to old API: ${error.message}")
                        // Fallback to old API
                        profileRepository.getSosContacts(userId).fold(
                            onSuccess = { contacts ->
                                sosContacts = contacts
                                contactsAdapter.updateContacts(contacts)
                                Log.d("ProfileActivity", "Loaded ${contacts.size} SOS contacts (fallback)")
                            },
                            onFailure = { fallbackError ->
                                Toast.makeText(this@ProfileActivity, 
                                    "Failed to load emergency contacts: ${fallbackError.message}", 
                                    Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, 
                    "Error loading data: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateProfileFields(profile: UserProfile) {
        Log.d("ProfileActivity", "=== UPDATING PROFILE FIELDS ===")
        Log.d("ProfileActivity", "Profile ID: ${profile.id}")
        Log.d("ProfileActivity", "First Name: ${profile.firstName}")
        Log.d("ProfileActivity", "Last Name: ${profile.lastName}")
        Log.d("ProfileActivity", "Email: ${profile.email}")
        Log.d("ProfileActivity", "Phone Number: '${profile.phoneNumber}' (type: ${profile.phoneNumber?.javaClass?.simpleName})")
        Log.d("ProfileActivity", "Username: ${profile.username}")
        Log.d("ProfileActivity", "Role: ${profile.role}")
        Log.d("ProfileActivity", "Status: ${profile.status}")
        
        etFirstName.setText(profile.firstName)
        etLastName.setText(profile.lastName)
        etEmail.setText(profile.email)
        
        // Handle phone number with better null checking
        val phoneNumber = profile.phoneNumber ?: ""
        etPhoneNumber.setText(phoneNumber)
        
        Log.d("ProfileActivity", "Phone number field set to: '${etPhoneNumber.text}' (original: '${profile.phoneNumber}')")
        Log.d("ProfileActivity", "=== PROFILE FIELDS UPDATED ===")
        
        // Update profile picture and display name
        val fullName = "${profile.firstName} ${profile.lastName}"
        tvProfileName.text = fullName
        
        // Set initials avatar
        avatarProfile.setInitialsFromNames(profile.firstName, profile.lastName)
        avatarProfile.setTextSize(32f)
        
        // Disable fields by default (read-only mode)
        etFirstName.isEnabled = false
        etLastName.isEnabled = false
        etEmail.isEnabled = false
        etPhoneNumber.isEnabled = false
    }
    
    
    private fun saveProfile() {
        Log.d(TAG, "Saving profile for user ID: $userId")
        
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Updating profile via API for user: $firstName $lastName")
                
                // Use the new prototype API for updating users
                profileRepository.updateUserWithPrototypeAPI(
                    userId = userId,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    username = userProfile?.username,
                    phoneNumber = phoneNumber.ifBlank { null },
                    role = userProfile?.role ?: "user"
                ).fold(
                    onSuccess = { updatedUser ->
                        Log.d(TAG, "Profile updated successfully via API")
                        
                        // Save updated profile to SharedPreferences
                        appSettings.saveUserData(
                            userId = userId,
                            email = email,
                            firstName = firstName,
                            lastName = lastName,
                            username = updatedUser.username ?: userProfile?.username ?: "",
                            role = updatedUser.role,
                            phoneNumber = phoneNumber.ifBlank { null }
                        )
                        
                        // Update local userProfile object
                        userProfile = userProfile?.copy(
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            phoneNumber = phoneNumber.ifBlank { null }
                        )
                        
                        // Update UI
                        updateProfileFields(userProfile!!)
                        
                        Toast.makeText(this@ProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        disableEditMode()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to update profile via API: ${error.message}")
                        Toast.makeText(this@ProfileActivity, 
                            "Failed to update profile: ${error.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile: ${e.message}")
                Toast.makeText(this@ProfileActivity, 
                    "Error updating profile: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showAddContactDialog() {
        // Check if user already has 3 contacts
        if (sosContacts.size >= 3) {
            Toast.makeText(this, "Maximum of 3 emergency contacts allowed. Please delete an existing contact before adding a new one.", Toast.LENGTH_LONG).show()
            return
        }
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        
        val etName = dialogView.findViewById<EditText>(R.id.etContactName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etContactPhone)
        val etRelationship = dialogView.findViewById<AutoCompleteTextView>(R.id.etContactRelationship)
        
        // Setup relationship dropdown
        val relationshipOptions = listOf("Family", "Friend", "Partner", "Others")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, relationshipOptions)
        etRelationship.setAdapter(adapter)
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Add Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val relationship = etRelationship.text.toString().trim()
                
                if (name.isNotEmpty() && phone.isNotEmpty() && relationship.isNotEmpty()) {
                    addContact(name, phone, relationship)
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun addContact(name: String, phone: String, relationship: String) {
        lifecycleScope.launch {
            try {
                profileRepository.addSosContact(userId, name, phone, relationship, false).fold(
                    onSuccess = { contact ->
                        Toast.makeText(this@ProfileActivity, 
                            "Contact added successfully: ${contact.name}", 
                            Toast.LENGTH_SHORT).show()
                        // Reload emergency contacts
                        loadUserData()
                    },
                    onFailure = { error ->
                        Toast.makeText(this@ProfileActivity, 
                            "Failed to add contact: ${error.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, 
                    "Error adding contact: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun editContact(contact: SosContact) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        
        val etName = dialogView.findViewById<EditText>(R.id.etContactName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etContactPhone)
        val etRelationship = dialogView.findViewById<AutoCompleteTextView>(R.id.etContactRelationship)
        
        // Pre-fill with existing data
        etName.setText(contact.name)
        etPhone.setText(contact.phoneNumber)
        etRelationship.setText(contact.relationship)
        
        // Setup relationship dropdown
        val relationshipOptions = listOf("Family", "Friend", "Partner", "Others")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, relationshipOptions)
        etRelationship.setAdapter(adapter)
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Edit Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val relationship = etRelationship.text.toString().trim()
                
                if (name.isNotEmpty() && phone.isNotEmpty() && relationship.isNotEmpty()) {
                    updateContact(contact.id, name, phone, relationship)
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun updateContact(contactId: Int, name: String, phone: String, relationship: String) {
        lifecycleScope.launch {
            try {
                profileRepository.updateEmergencyContact(
                    contactId = contactId,
                    name = name,
                    phoneNumber = phone,
                    relationship = relationship,
                    isPrimary = null
                ).fold(
                    onSuccess = { response ->
                        Toast.makeText(this@ProfileActivity, 
                            "Contact updated successfully: ${response.contact.name}", 
                            Toast.LENGTH_SHORT).show()
                        // Reload emergency contacts
                        loadUserData()
                    },
                    onFailure = { error ->
                        Toast.makeText(this@ProfileActivity, 
                            "Failed to update contact: ${error.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, 
                    "Error updating contact: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun deleteContact(contact: SosContact) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Emergency Contact")
            .setMessage("Are you sure you want to delete '${contact.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                performDeleteContact(contact)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performDeleteContact(contact: SosContact) {
        Log.d(TAG, "Attempting to delete contact: ${contact.name} (ID: ${contact.id})")
        
        lifecycleScope.launch {
            try {
                profileRepository.deleteEmergencyContact(contact.id, permanent = false).fold(
                    onSuccess = { response ->
                        Log.d(TAG, "Delete contact response received: $response")
                        
                        // Use the original contact name since the API might not return the deleted contact info
                        val contactName = contact.name
                        Log.d(TAG, "Contact deleted successfully: $contactName")
                        
                        Toast.makeText(this@ProfileActivity, 
                            "Contact deleted successfully: $contactName", 
                            Toast.LENGTH_SHORT).show()
                        
                        // Reload emergency contacts to refresh the list
                        loadUserData()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to delete contact: ${error.message}", error)
                        
                        // Even if the API call fails, try to reload the data to check if deletion actually worked
                        // This handles cases where the deletion succeeds but the response parsing fails
                        loadUserData()
                        
                        // Show a more user-friendly error message
                        Toast.makeText(this@ProfileActivity, 
                            "Contact deletion may have succeeded. Please refresh to verify.", 
                            Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during contact deletion: ${e.message}", e)
                
                // Even if there's an exception, try to reload the data to check if deletion worked
                loadUserData()
                
                Toast.makeText(this@ProfileActivity, 
                    "Contact deletion may have succeeded. Please refresh to verify.", 
                    Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        
        val etCurrentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = etCurrentPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()
                
                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (newPassword.length < 6) {
                    Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                changePassword(currentPassword, newPassword)
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun changePassword(currentPassword: String, newPassword: String) {
        lifecycleScope.launch {
            try {
                profileRepository.changePassword(userId, currentPassword, newPassword).fold(
                    onSuccess = { response ->
                        Toast.makeText(this@ProfileActivity, 
                            "Password changed successfully", 
                            Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(this@ProfileActivity, 
                            "Failed to change password: ${error.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, 
                    "Error changing password: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showDeleteAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_account, null)
        
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("This action cannot be undone. Please enter your password to confirm.")
            .setView(dialogView)
            .setPositiveButton("Delete Account") { _, _ ->
                val password = etPassword.text.toString().trim()
                
                if (password.isEmpty()) {
                    Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                // Show confirmation dialog
                android.app.AlertDialog.Builder(this)
                    .setTitle("Final Confirmation")
                    .setMessage("Are you absolutely sure you want to delete your account? This action cannot be undone and will permanently delete all your data.")
                    .setPositiveButton("Yes, Delete") { _, _ ->
                        deleteUserAccount(password)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun deleteUserAccount(password: String) {
        lifecycleScope.launch {
            try {
                profileRepository.deleteUser(userId, password).fold(
                    onSuccess = { response ->
                        Toast.makeText(this@ProfileActivity, 
                            "Account deleted successfully", 
                            Toast.LENGTH_SHORT).show()
                        
                        // Clear local session and logout
                        appSettings.clearUserSession()
                        val intent = Intent(this@ProfileActivity, AuthActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    },
                    onFailure = { error ->
                        Toast.makeText(this@ProfileActivity, 
                            "Failed to delete account: ${error.message}", 
                            Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, 
                    "Error deleting account: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun enableEditMode() {
        isEditMode = true
        etFirstName.isEnabled = true
        etLastName.isEnabled = true
        etEmail.isEnabled = true
        etPhoneNumber.isEnabled = true
        
        btnEditMode.visibility = android.view.View.GONE
        layoutActionButtons.visibility = android.view.View.VISIBLE
    }
    
    private fun disableEditMode() {
        isEditMode = false
        etFirstName.isEnabled = false
        etLastName.isEnabled = false
        etEmail.isEnabled = false
        etPhoneNumber.isEnabled = false
        
        btnEditMode.visibility = android.view.View.VISIBLE
        layoutActionButtons.visibility = android.view.View.GONE
        
        // Restore original values
        userProfile?.let { profile ->
            updateProfileFields(profile)
        }
    }
    
    private fun logout() {
        appSettings.clearUserSession()
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

