package com.example.gzingapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class AppSettings(private val context: Context) {
    
    private val sharedPref: SharedPreferences = 
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_APP_VERSION = "app_version"
        private const val KEY_LAST_LAUNCH = "last_launch"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_REMEMBER_LOGIN = "remember_login"
        private const val KEY_ALARM_FENCE_RADIUS = "alarm_fence_radius"
        private const val KEY_ALARM_SOUND_URI = "alarm_sound_uri"
        private const val KEY_VOICE_ANNOUNCEMENTS = "voice_announcements"
        private const val KEY_ROUTE_GEOFENCE_ENABLED = "route_geofence_enabled"
        private const val KEY_ROUTE_GEOFENCE_RADIUS = "route_geofence_radius"
        private const val KEY_GEOFENCE_NOTIFICATIONS = "geofence_notifications"
        
        // Authentication keys
        private const val KEY_AUTH_STATE = "auth_state"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
        private const val KEY_SESSION_EXPIRY = "session_expiry"
        private const val KEY_AUTO_LOGIN = "auto_login"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        
        // Theme modes
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
        
        // Authentication states
        const val AUTH_STATE_LOGGED_OUT = "logged_out"
        const val AUTH_STATE_LOGGED_IN = "logged_in"
        const val AUTH_STATE_SESSION_EXPIRED = "session_expired"
        const val AUTH_STATE_LOGGING_IN = "logging_in"
    }
    
    // Theme settings
    fun getThemeMode(): String {
        return sharedPref.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }
    
    fun setThemeMode(theme: String) {
        sharedPref.edit().putString(KEY_THEME_MODE, theme).apply()
        applyThemeMode(theme)
    }
    
    private fun applyThemeMode(theme: String) {
        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    // App launch settings
    fun isFirstLaunch(): Boolean {
        return sharedPref.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    fun setFirstLaunch(isFirst: Boolean) {
        sharedPref.edit().putBoolean(KEY_FIRST_LAUNCH, isFirst).apply()
    }
    
    fun getAppVersion(): String {
        return sharedPref.getString(KEY_APP_VERSION, "1.0") ?: "1.0"
    }
    
    fun setAppVersion(version: String) {
        sharedPref.edit().putString(KEY_APP_VERSION, version).apply()
    }
    
    fun getLastLaunchTime(): Long {
        return sharedPref.getLong(KEY_LAST_LAUNCH, 0L)
    }
    
    fun setLastLaunchTime(time: Long) {
        sharedPref.edit().putLong(KEY_LAST_LAUNCH, time).apply()
    }
    
    // User session settings (simplified without tokens)
    
    fun getUserId(): Int? {
        val userId = sharedPref.getInt(KEY_USER_ID, -1)
        return if (userId == -1) null else userId
    }
    
    fun setUserId(userId: Int?) {
        if (userId != null) {
            sharedPref.edit().putInt(KEY_USER_ID, userId).apply()
        } else {
            sharedPref.edit().remove(KEY_USER_ID).apply()
        }
    }
    
    fun getUserEmail(): String? {
        return sharedPref.getString(KEY_USER_EMAIL, null)
    }
    
    fun setUserEmail(email: String?) {
        if (email != null) {
            sharedPref.edit().putString(KEY_USER_EMAIL, email).apply()
        } else {
            sharedPref.edit().remove(KEY_USER_EMAIL).apply()
        }
    }
    
    fun shouldRememberLogin(): Boolean {
        return sharedPref.getBoolean(KEY_REMEMBER_LOGIN, false)
    }
    
    fun setRememberLogin(remember: Boolean) {
        sharedPref.edit().putBoolean(KEY_REMEMBER_LOGIN, remember).apply()
    }
    
    // Alarm Fence (geofence) radius in meters; default 100
    fun getAlarmFenceRadiusMeters(): Int {
        return sharedPref.getInt(KEY_ALARM_FENCE_RADIUS, 100)
    }
    
    fun setAlarmFenceRadiusMeters(radiusMeters: Int) {
        // Clamp to supported values: 50, 100, 150, 200
        val allowed = setOf(50, 100, 150, 200)
        val value = if (radiusMeters in allowed) radiusMeters else 100
        sharedPref.edit().putInt(KEY_ALARM_FENCE_RADIUS, value).apply()
    }
    
    // Alarm sound URI
    fun getAlarmSoundUri(): String? {
        return sharedPref.getString(KEY_ALARM_SOUND_URI, null)
    }
    
    fun setAlarmSoundUri(uri: String?) {
        if (uri.isNullOrEmpty()) {
            sharedPref.edit().remove(KEY_ALARM_SOUND_URI).apply()
        } else {
            sharedPref.edit().putString(KEY_ALARM_SOUND_URI, uri).apply()
        }
    }

    // Voice announcements
    fun isVoiceAnnouncementsEnabled(): Boolean {
        return sharedPref.getBoolean(KEY_VOICE_ANNOUNCEMENTS, true)
    }

    fun setVoiceAnnouncementsEnabled(enabled: Boolean) {
        sharedPref.edit().putBoolean(KEY_VOICE_ANNOUNCEMENTS, enabled).apply()
    }
    
    // Route geofence settings
    fun isRouteGeofenceEnabled(): Boolean {
        return sharedPref.getBoolean(KEY_ROUTE_GEOFENCE_ENABLED, true)
    }
    
    fun setRouteGeofenceEnabled(enabled: Boolean) {
        sharedPref.edit().putBoolean(KEY_ROUTE_GEOFENCE_ENABLED, enabled).apply()
    }
    
    fun getRouteGeofenceRadiusMeters(): Int {
        return sharedPref.getInt(KEY_ROUTE_GEOFENCE_RADIUS, 150)
    }
    
    fun setRouteGeofenceRadiusMeters(radiusMeters: Int) {
        // Clamp to supported values: 100, 150, 200, 300
        val allowed = setOf(100, 150, 200, 300)
        val value = if (radiusMeters in allowed) radiusMeters else 150
        sharedPref.edit().putInt(KEY_ROUTE_GEOFENCE_RADIUS, value).apply()
    }
    
    fun areGeofenceNotificationsEnabled(): Boolean {
        return sharedPref.getBoolean(KEY_GEOFENCE_NOTIFICATIONS, true)
    }
    
    fun setGeofenceNotificationsEnabled(enabled: Boolean) {
        sharedPref.edit().putBoolean(KEY_GEOFENCE_NOTIFICATIONS, enabled).apply()
    }
    
    // Clear user session
    fun clearUserSession() {
        sharedPref.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_REMEMBER_LOGIN)
            .remove("user_first_name")
            .remove("user_last_name")
            .remove("user_username")
            .remove("user_role")
            .remove("user_phone_number")
            .apply()
    }
    
    // Clear user data (alias for clearUserSession)
    fun clearUserData() {
        clearUserSession()
    }
    
    // Authentication without tokens - simplified approach
    
    // Get user info as a data class
    fun getUserInfo(): UserInfo {
        return UserInfo(
            id = getUserId() ?: 0,
            email = getUserEmail() ?: "",
            firstName = getFirstName() ?: "",
            lastName = getLastName() ?: ""
        )
    }
    
    // Additional user info methods
    fun getFirstName(): String? {
        return sharedPref.getString("user_first_name", null)
    }
    
    fun getLastName(): String? {
        return sharedPref.getString("user_last_name", null)
    }
    
    fun setUserInfo(firstName: String?, lastName: String?) {
        val editor = sharedPref.edit()
        if (firstName != null) editor.putString("user_first_name", firstName)
        if (lastName != null) editor.putString("user_last_name", lastName)
        editor.apply()
    }
    
    // Save complete user data
    fun saveUserData(
        userId: Int,
        email: String,
        firstName: String,
        lastName: String,
        username: String,
        role: String,
        phoneNumber: String? = null
    ) {
        val editor = sharedPref.edit()
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString("user_first_name", firstName)
        editor.putString("user_last_name", lastName)
        editor.putString("user_username", username)
        editor.putString("user_role", role)
        if (phoneNumber != null) {
            editor.putString("user_phone_number", phoneNumber)
        }
        editor.apply()
    }
    
    fun getUsername(): String? {
        return sharedPref.getString("user_username", null)
    }
    
    fun getUserRole(): String? {
        return sharedPref.getString("user_role", null)
    }
    
    fun getPhoneNumber(): String? {
        return sharedPref.getString("user_phone_number", null)
    }
    
    fun setPhoneNumber(phoneNumber: String?) {
        if (phoneNumber != null) {
            sharedPref.edit().putString("user_phone_number", phoneNumber).apply()
        } else {
            sharedPref.edit().remove("user_phone_number").apply()
        }
    }
    
    // ===== AUTHENTICATION METHODS =====
    
    /**
     * Check if user is currently logged in (simplified without tokens)
     */
    fun isUserLoggedIn(): Boolean {
        val userId = getUserId()
        val userEmail = getUserEmail()
        val authState = getAuthState()
        
        return userId != null && userEmail != null && authState == AUTH_STATE_LOGGED_IN
    }
    
    /**
     * Get current authentication state
     */
    fun getAuthState(): String {
        return sharedPref.getString(KEY_AUTH_STATE, AUTH_STATE_LOGGED_OUT) ?: AUTH_STATE_LOGGED_OUT
    }
    
    /**
     * Set authentication state
     */
    fun setAuthState(state: String) {
        sharedPref.edit().putString(KEY_AUTH_STATE, state).apply()
    }
    
    /**
     * Get login timestamp
     */
    fun getLoginTimestamp(): Long {
        return sharedPref.getLong(KEY_LOGIN_TIMESTAMP, 0L)
    }
    
    /**
     * Set login timestamp
     */
    fun setLoginTimestamp(timestamp: Long) {
        sharedPref.edit().putLong(KEY_LOGIN_TIMESTAMP, timestamp).apply()
    }
    
    /**
     * Get session expiry time
     */
    fun getSessionExpiry(): Long {
        return sharedPref.getLong(KEY_SESSION_EXPIRY, 0L)
    }
    
    /**
     * Set session expiry time (24 hours from now by default)
     */
    fun setSessionExpiry(expiryTime: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000)) {
        sharedPref.edit().putLong(KEY_SESSION_EXPIRY, expiryTime).apply()
    }
    
    /**
     * Check if session is expired
     */
    fun isSessionExpired(): Boolean {
        val expiryTime = getSessionExpiry()
        return expiryTime > 0 && System.currentTimeMillis() > expiryTime
    }
    
    /**
     * Check if auto-login is enabled
     */
    fun isAutoLoginEnabled(): Boolean {
        return sharedPref.getBoolean(KEY_AUTO_LOGIN, false)
    }
    
    /**
     * Set auto-login preference
     */
    fun setAutoLoginEnabled(enabled: Boolean) {
        sharedPref.edit().putBoolean(KEY_AUTO_LOGIN, enabled).apply()
    }
    
    /**
     * Check if biometric authentication is enabled
     */
    fun isBiometricEnabled(): Boolean {
        return sharedPref.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    /**
     * Set biometric authentication preference
     */
    fun setBiometricEnabled(enabled: Boolean) {
        sharedPref.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
    
    /**
     * Complete login process - saves all user data and sets auth state (simplified without tokens)
     */
    fun completeLogin(
        userId: Int,
        email: String,
        firstName: String,
        lastName: String,
        username: String,
        role: String,
        phoneNumber: String? = null,
        rememberLogin: Boolean = false
    ) {
        val currentTime = System.currentTimeMillis()
        
        // Save user data
        saveUserData(userId, email, firstName, lastName, username, role, phoneNumber)
        
        // Set authentication state
        setAuthState(AUTH_STATE_LOGGED_IN)
        
        // Set login timestamp
        setLoginTimestamp(currentTime)
        
        // Set session expiry (24 hours from now)
        setSessionExpiry(currentTime + (24 * 60 * 60 * 1000))
        
        // Set remember login preference
        setRememberLogin(rememberLogin)
        
        // Set auto-login if remember is enabled
        setAutoLoginEnabled(rememberLogin)
    }
    
    /**
     * Complete logout process - clears all user data and resets auth state
     */
    fun completeLogout() {
        // Clear user session data
        clearUserSession()
        
        // Reset authentication state
        setAuthState(AUTH_STATE_LOGGED_OUT)
        
        // Clear timestamps
        sharedPref.edit()
            .remove(KEY_LOGIN_TIMESTAMP)
            .remove(KEY_SESSION_EXPIRY)
            .apply()
    }
    
    /**
     * Mark session as expired
     */
    fun markSessionExpired() {
        setAuthState(AUTH_STATE_SESSION_EXPIRED)
    }
    
    /**
     * Validate current session
     */
    fun validateSession(): Boolean {
        if (!isUserLoggedIn()) {
            return false
        }
        
        if (isSessionExpired()) {
            markSessionExpired()
            return false
        }
        
        return true
    }
    
    /**
     * Get user session info (simplified without tokens)
     */
    fun getUserSession(): UserSession? {
        if (!isUserLoggedIn()) {
            return null
        }
        
        return UserSession(
            id = getUserId() ?: 0,
            email = getUserEmail() ?: "",
            firstName = getFirstName() ?: "",
            lastName = getLastName() ?: "",
            username = getUsername() ?: "",
            role = getUserRole() ?: "user",
            phoneNumber = getPhoneNumber(),
            loginTime = getLoginTimestamp(),
            sessionExpiry = getSessionExpiry()
        )
    }
    
    /**
     * Check if user has specific role
     */
    fun hasRole(role: String): Boolean {
        val userRole = getUserRole()
        return userRole?.equals(role, ignoreCase = true) ?: false
    }
    
    /**
     * Check if user is admin
     */
    fun isAdmin(): Boolean {
        return hasRole("admin")
    }
    
    /**
     * Get session duration in minutes
     */
    fun getSessionDurationMinutes(): Long {
        val loginTime = getLoginTimestamp()
        if (loginTime == 0L) return 0L
        
        return (System.currentTimeMillis() - loginTime) / (60 * 1000)
    }
    
    /**
     * Get remaining session time in minutes
     */
    fun getRemainingSessionMinutes(): Long {
        val expiryTime = getSessionExpiry()
        if (expiryTime == 0L) return 0L
        
        val remaining = expiryTime - System.currentTimeMillis()
        return if (remaining > 0) remaining / (60 * 1000) else 0L
    }
    
    /**
     * Extend session by specified minutes
     */
    fun extendSession(minutes: Int = 60) {
        val currentExpiry = getSessionExpiry()
        if (currentExpiry > 0) {
            val newExpiry = currentExpiry + (minutes * 60 * 1000L)
            setSessionExpiry(newExpiry)
        }
    }
    
    /**
     * Initialize app settings
     */
    fun initializeAppSettings() {
        val currentTime = System.currentTimeMillis()
        
        // Set first launch to false if this is not the first launch
        if (isFirstLaunch()) {
            setFirstLaunch(false)
        }
        
        // Update last launch time
        setLastLaunchTime(currentTime)
        
        // Apply saved theme mode
        applyThemeMode(getThemeMode())
        
        // Check session validity on app start
        if (isUserLoggedIn() && isSessionExpired()) {
            markSessionExpired()
        }
    }
}

// User info data class
data class UserInfo(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String
)

// User session data class with additional session information (simplified without tokens)
data class UserSession(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val role: String,
    val phoneNumber: String?,
    val loginTime: Long,
    val sessionExpiry: Long
) {
    val fullName: String
        get() = "$firstName $lastName"
    
    val displayName: String
        get() = if (firstName.isNotEmpty() && lastName.isNotEmpty()) fullName else email
    
    val isSessionValid: Boolean
        get() = System.currentTimeMillis() < sessionExpiry
    
    val sessionDurationMinutes: Long
        get() = if (loginTime > 0) (System.currentTimeMillis() - loginTime) / (60 * 1000) else 0L
    
    val remainingSessionMinutes: Long
        get() = if (sessionExpiry > 0) {
            val remaining = sessionExpiry - System.currentTimeMillis()
            if (remaining > 0) remaining / (60 * 1000) else 0L
        } else 0L
}

