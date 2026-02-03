package com.hello.lets.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hello.lets.test.screen.ui.deshboard.Deshboard
import com.hello.lets.test.screen.ui.splash.SplashScreen
import com.hello.lets.test.ui.theme.LearnkotlineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the Android 12+ splash screen API (optional, for native support)
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnkotlineTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    SplashScreen(
                        onSplashComplete = { showSplash = false }
                    )
                } else {
                    Deshboard()
                }
            }
        }
    }
}
