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
    primary = MerahPrimary,
    secondary = MerahLight,
    tertiary = InfoBlue,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = DarkBg,
    onBackground = DarkText,
    onSurface = DarkText
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MerahPrimary,
    secondary = FintechSecondary,
    tertiary = InfoBlue,
    background = FintechBackground,
    surface = FintechSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = FintechSecondary,
    onSurface = FintechSecondary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color disabled to maintain custom Red & White brand consistency
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
