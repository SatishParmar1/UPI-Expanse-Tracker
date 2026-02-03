package com.hello.lets.test.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
// ... inside your Theme.kt

val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    background = DarkBackground,

    // 1. Standard Cards (like the Uber/Salary rows)
    surface = DarkSurface,

    // 2. WIDGETS (like the Donut Chart background or big containers)
    surfaceVariant = DarkSurfaceVariant,

    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    error = ErrorRed,
    outlineVariant = Color.White.copy(alpha = 0.1f)
)

val LightColorScheme = lightColorScheme(
    primary = GreenPrimaryLight,
    background = LightBackground,

    // 1. Standard Cards (White)
    surface = LightSurface,

    // 2. WIDGETS (Soft Mint/Grey for contrast against the white cards)
    surfaceVariant = LightSurfaceVariant,

    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    error = Color(0xFFDC2626),
    outlineVariant = Color.Black.copy(alpha = 0.1f)
)

@Composable
fun LearnkotlineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // ❗ disable to keep your custom colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
