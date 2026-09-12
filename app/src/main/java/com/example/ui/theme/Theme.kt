package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HydraColorScheme = darkColorScheme(
    primary = HydraCyan,
    onPrimary = HydraBgDark,
    primaryContainer = HydraSurfaceVariant,
    onPrimaryContainer = HydraCyan,
    secondary = HydraPurple,
    onSecondary = HydraBgDark,
    secondaryContainer = HydraSurfaceVariant,
    onSecondaryContainer = HydraPurple,
    tertiary = HydraAndroidGreen,
    onTertiary = HydraBgDark,
    background = HydraBgDark,
    onBackground = HydraTextPrimary,
    surface = HydraSurface,
    onSurface = HydraTextPrimary,
    surfaceVariant = HydraSurfaceVariant,
    onSurfaceVariant = HydraTextSecondary,
    outline = HydraBorder,
    outlineVariant = HydraBorderActive,
    error = HydraRed,
    onError = HydraBgDark
)

@Composable
fun HydraNetTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HydraColorScheme,
        typography = Typography,
        content = content
    )
}
