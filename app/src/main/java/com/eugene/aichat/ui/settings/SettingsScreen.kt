package com.eugene.aichat.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.eugene.aichat.R
import com.eugene.aichat.ui.settings.sub.AccountSection
import com.eugene.aichat.ui.settings.sub.AgentsSection
import com.eugene.aichat.ui.settings.sub.ModelsSection
import com.eugene.aichat.ui.settings.sub.SkillsSection
import com.eugene.aichat.ui.settings.sub.SystemPromptSection
import com.eugene.aichat.ui.settings.sub.ThemeSection

@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AccountSection()
            ThemeSection()
            ModelsSection(navController = navController)
            SystemPromptSection()
            SkillsSection(navController = navController)
            AgentsSection(navController = navController)
            Text(
                text = "Settings — System prompt, Skills, Agents, Account sign-in land in upcoming steps.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
