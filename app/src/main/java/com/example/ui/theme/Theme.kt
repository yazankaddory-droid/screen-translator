package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


private val DarkColorScheme =
  darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D56),
    onPrimaryContainer = CyanPrimary,
    secondary = ElectricAccent,
    tertiary = GlowGreen,
    background = DeepNavyBackground,
    surface = DarkSurfaceCard,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = GlowGreen,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant
  )

@Composable
fun ScreenTranslatorTheme(
  darkTheme: Boolean = true, // Default to sleek futuristic dark theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
