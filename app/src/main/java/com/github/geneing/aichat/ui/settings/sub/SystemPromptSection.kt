package com.github.geneing.aichat.ui.settings.sub

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.geneing.aichat.R

@Composable
fun SystemPromptSection() {
    Text(
        text = stringResource(R.string.settings_section_system_prompt) + " — editor ships in step 3",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}
