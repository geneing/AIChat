package com.eugene.aichat.core.ui.theme

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

interface ThemeController {
    val mode: StateFlow<ThemeMode>
    fun setMode(mode: ThemeMode)
}

val LocalThemeController = compositionLocalOf<ThemeController> {
    error("ThemeController not provided. Wrap your composable in CompositionLocalProvider(LocalThemeController provides ...).")
}
