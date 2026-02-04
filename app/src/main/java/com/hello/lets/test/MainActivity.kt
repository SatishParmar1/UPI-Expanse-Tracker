package com.hello.lets.test

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.hello.lets.test.data.AppDatabase
import com.hello.lets.test.data.preferences.AppPreferences
import com.hello.lets.test.screen.ui.deshboard.Deshboard
import com.hello.lets.test.screen.ui.lock.LockScreen
import com.hello.lets.test.screen.ui.onboarding.OnboardingScreen
import com.hello.lets.test.screen.ui.splash.SplashScreen
import com.hello.lets.test.ui.theme.LearnkotlineTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main Activity that handles app lock, onboarding, and navigation.
 * Uses FragmentActivity for biometric authentication support.
 */
class MainActivity : FragmentActivity() {
    
    private lateinit var appPreferences: AppPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the Android 12+ splash screen API
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        appPreferences = AppPreferences.getInstance(this)
        val db = AppDatabase.getDatabase(this)
        
        setContent {
            LearnkotlineTheme {
                var showSplash by remember { mutableStateOf(true) }
                var isAuthenticated by remember { mutableStateOf(false) }
                var needsOnboarding by remember { mutableStateOf<Boolean?>(null) }
                val isAppLockEnabled = remember { appPreferences.isAppLockEnabled }
                
                // Check onboarding status
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val isComplete = db.userProfileDao().isOnboardingComplete() ?: false
                        withContext(Dispatchers.Main) {
                            needsOnboarding = !isComplete
                        }
                    }
                }
                
                when {
                    showSplash -> {
                        SplashScreen(
                            onSplashComplete = { showSplash = false }
                        )
                    }
                    needsOnboarding == null -> {
                        // Still loading onboarding status
                        SplashScreen(onSplashComplete = {})
                    }
                    needsOnboarding == true -> {
                        OnboardingScreen(
                            onComplete = { needsOnboarding = false }
                        )
                    }
                    isAppLockEnabled && !isAuthenticated -> {
                        LockScreen(
                            onAuthenticated = { isAuthenticated = true },
                            onAuthError = { /* Handle error if needed */ }
                        )
                    }
                    else -> {
                        Deshboard()
                    }
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Optionally re-lock when app goes to background
        // This can be controlled via a setting
    }
}
