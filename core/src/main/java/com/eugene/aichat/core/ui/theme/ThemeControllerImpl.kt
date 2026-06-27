package com.eugene.aichat.core.ui.theme

import com.eugene.aichat.core.data.prefs.UserPreferences
import com.eugene.aichat.core.di.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeControllerImpl @Inject constructor(
    private val preferences: UserPreferences,
    @AppScope private val appScope: CoroutineScope
) : ThemeController {

    override val mode: StateFlow<ThemeMode> = preferences.themeMode
        .map { runCatching { ThemeMode.valueOf(it) }.getOrDefault(ThemeMode.SYSTEM) }
        .stateIn(appScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    override fun setMode(mode: ThemeMode) {
        appScope.launch { preferences.setThemeMode(mode.name) }
    }
}
