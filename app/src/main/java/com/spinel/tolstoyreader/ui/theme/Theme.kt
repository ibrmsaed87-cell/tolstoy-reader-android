package com.spinel.tolstoyreader.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import android.app.Activity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val paperBackground = Color(0xFFFBF8F1) // Warm ivory
private val paperSurface = Color(0xFFF1EBE0) // Slightly different and clear from Background
private val paperSurfaceVariant = Color(0xFFE5DECF) // Third calm tone for secondary cards
private val paperOnBackground = Color(0xFF2D2C2A) // Dark charcoal instead of sharp black
private val literaryPrimary = Color(0xFF8B5A5A) // Desaturated burgundy / warm taupe
private val secondaryContainer = Color(0xFFD6C8B8)
private val onSecondaryContainer = Color(0xFF3E362E)


private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBCAAA4),
    secondary = Color(0xFFA1887F),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFFBDBDBD)
)

private val LightColorScheme = lightColorScheme(
    primary = literaryPrimary,
    onPrimary = Color.White,
    secondary = Color(0xFF6D5A5A),
    onSecondary = Color.White,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    background = paperBackground,
    onBackground = paperOnBackground,
    surface = paperSurface,
    onSurface = paperOnBackground,
    surfaceVariant = paperSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A4643)
)


@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
      WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
