package com.hello.lets.test.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Manages app preferences for lock settings and other configurations.
 */
class AppPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "upi_tracker_prefs"
        
        // Lock settings
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        
        // Appearance settings
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_ACCENT_COLOR = "accent_color"
        
        // Budget settings
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        
        // First run
        private const val KEY_FIRST_RUN = "first_run"
        
        @Volatile
        private var INSTANCE: AppPreferences? = null
        
        fun getInstance(context: Context): AppPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferences(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
    
    // Lock Settings
    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false) // Default: disabled
        set(value) = prefs.edit { putBoolean(KEY_APP_LOCK_ENABLED, value) }
    
    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false) // Default: disabled
        set(value) = prefs.edit { putBoolean(KEY_BIOMETRIC_ENABLED, value) }
    
    // Appearance Settings
    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true) // Default: dark mode
        set(value) = prefs.edit { putBoolean(KEY_DARK_MODE, value) }
    
    var accentColor: String
        get() = prefs.getString(KEY_ACCENT_COLOR, "#4CAF50") ?: "#4CAF50"
        set(value) = prefs.edit { putString(KEY_ACCENT_COLOR, value) }
    
    // Budget Settings
    var monthlyBudget: Float
        get() = prefs.getFloat(KEY_MONTHLY_BUDGET, 15000f) // Default: ₹15,000
        set(value) = prefs.edit { putFloat(KEY_MONTHLY_BUDGET, value) }
    
    // First Run
    var isFirstRun: Boolean
        get() = prefs.getBoolean(KEY_FIRST_RUN, true)
        set(value) = prefs.edit { putBoolean(KEY_FIRST_RUN, value) }
    
    /**
     * Clear all preferences.
     */
    fun clear() {
        prefs.edit { clear() }
    }
}
