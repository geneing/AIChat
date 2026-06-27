package com.eugene.aichat.ui.settings.sub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eugene.aichat.R
import com.eugene.aichat.core.ui.theme.LocalThemeController
import com.eugene.aichat.core.ui.theme.ThemeMode

@Composable
fun ThemeSection() {
    val controller = LocalThemeController.current
    val mode by controller.mode.collectAsStateWithLifecycle()
    Text(
        text = stringResource(R.string.settings_section_theme),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = mode == ThemeMode.SYSTEM,
            onClick = { controller.setMode(ThemeMode.SYSTEM) },
            label = { Text(stringResource(R.string.theme_system)) }
        )
        FilterChip(
            selected = mode == ThemeMode.LIGHT,
            onClick = { controller.setMode(ThemeMode.LIGHT) },
            label = { Text(stringResource(R.string.theme_light)) }
        )
        FilterChip(
            selected = mode == ThemeMode.DARK,
            onClick = { controller.setMode(ThemeMode.DARK) },
            label = { Text(stringResource(R.string.theme_dark)) }
        )
    }
}
