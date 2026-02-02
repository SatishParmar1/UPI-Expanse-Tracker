package com.hello.lets.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hello.lets.test.screen.ui.Addition
import com.hello.lets.test.screen.ui.deshboard.Deshboard
import com.hello.lets.test.ui.theme.LearnkotlineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnkotlineTheme {

                Deshboard()
            }
        }
    }
}

