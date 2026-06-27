package com.eugene.aichat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.aichat.core.ui.theme.LocalThemeController
import com.eugene.aichat.core.ui.theme.ThemeController
import com.eugene.aichat.core.ui.theme.ThemeMode

private val LightColors = lightColorScheme(
    primary = SparkBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0EAFF),
    onPrimaryContainer = Color(0xFF0A1A4F),
    secondary = SparkViolet,
    onSecondary = Color.White,
    tertiary = SparkCyan,
    onTertiary = Color(0xFF003738),
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

private val DarkColors = darkColorScheme(
    primary = SparkBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A2C66),
    onPrimaryContainer = Color(0xFFD8E1FF),
    secondary = SparkViolet,
    onSecondary = Color.White,
    tertiary = SparkCyan,
    onTertiary = Color(0xFF003738),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

@Composable
fun AIChatTheme(
    controller: ThemeController = LocalThemeController.current,
    content: @Composable () -> Unit
) {
    val mode by controller.mode.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val useDark = when (mode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (useDark) DarkColors else LightColors,
        typography = AIChatTypography,
        shapes = AIChatShapes,
        content = content
    )
}
