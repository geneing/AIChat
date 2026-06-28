package com.github.geneing.aichat.ui.settings.sub

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.geneing.aichat.R

@Composable
fun AccountSection() {
    Text(
        text = stringResource(R.string.settings_section_account) + " — Google sign-in ships in step 17",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}
