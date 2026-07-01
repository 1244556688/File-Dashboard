package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberCyan,
    secondary = CyberPurple,
    tertiary = CyberGreen,
    background = CyberDark,
    surface = CyberSurface,
    surfaceVariant = CyberSurfaceVariant,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = IceBlue,
    onSurface = Color.White,
    onSurfaceVariant = IceBlue,
    error = CyberRed,
    outline = CyberBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for cyberpunk control center
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve neon visual identity
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
