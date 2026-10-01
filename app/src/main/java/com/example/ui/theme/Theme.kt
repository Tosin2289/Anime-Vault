package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VaultColorScheme = darkColorScheme(
    primary = CrimsonAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF38101E),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = CyanAccent,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = VioletAccent,
    onTertiary = Color.White,
    background = VaultBlack,
    onBackground = TextPrimary,
    surface = VaultDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = VaultCardSurface,
    onSurfaceVariant = TextSecondary,
    outline = VaultBorder,
    outlineVariant = VaultBorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve our custom anime aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VaultColorScheme,
        typography = Typography,
        content = content
    )
}
