package com.hello.lets.test.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.hello.lets.test.R // <--- Make sure this import matches your package!

// 1. Define the Provider (You already had this)
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// 2. Define the specific Font Name
val literataFontName = GoogleFont("Literata")

// 3. Create the FontFamily
// This connects the name "Literata" to the provider so it can be downloaded.
val LiterataFontFamily = FontFamily(
    Font(googleFont = literataFontName, fontProvider = provider),
    // You can add specific weights if you want specific variants to load:
    Font(googleFont = literataFontName, fontProvider = provider, weight = FontWeight.Bold)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = LiterataFontFamily, // <--- 4. Assign it here
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* // Example: Using it for titles too
    titleLarge = TextStyle(
        fontFamily = LiterataFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
    )
    */
)