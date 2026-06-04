package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BlackAndGreenColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.Black,
    primaryContainer = GreenDark,
    onPrimaryContainer = Color.White,
    secondary = GreenNeon,
    onSecondary = Color.Black,
    tertiary = GreenLight,
    onTertiary = Color.Black,
    background = BackgroundBlack,
    onBackground = TextSlate100,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = TextSlate100,
    outline = BorderZinc800,
    error = ErrorRed,
    onError = Color.Black
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Force sleek Black & Green theme for absolute design consistency.
    MaterialTheme(
        colorScheme = BlackAndGreenColorScheme,
        typography = Typography,
        content = content
    )
}
