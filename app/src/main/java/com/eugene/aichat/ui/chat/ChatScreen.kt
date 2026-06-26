package com.eugene.aichat.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.eugene.aichat.R
import com.eugene.aichat.nav.NavArgs
import com.eugene.aichat.ui.components.AppTopBar
import com.eugene.aichat.ui.components.EmptyStateHero
import com.eugene.aichat.ui.components.InputBar
import com.eugene.aichat.ui.theme.Dimens

@Composable
fun ChatScreen(
    chatId: String,
    agentId: String?,
    navController: NavHostController
) {
    val isNew = chatId == NavArgs.NEW_CHAT
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            AppTopBar(
                title = if (isNew) stringResource(R.string.app_name) else "Chat",
                onMenu = { /* drawer hookup in step 12 */ },
                onSettings = { navController.navigate(com.eugene.aichat.nav.SettingsRoute) },
                onVoiceToggle = { /* voice mode hookup in step 8 */ }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isNew) {
                    EmptyStateHero(
                        title = stringResource(R.string.app_name),
                        subtitle = stringResource(R.string.chat_empty_subtitle)
                    )
                } else {
                    Text(
                        text = "Chat $chatId (coming in step 5)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            InputBar(
                value = "",
                onValueChange = {},
                onSend = { /* wired up in step 5 */ },
                onAttach = { /* wired up in step 7 */ },
                onVoicePress = { /* wired up in step 8 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp)
            )
        }
    }
}
