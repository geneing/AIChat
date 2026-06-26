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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.eugene.aichat.R
import com.eugene.aichat.nav.NavArgs
import com.eugene.aichat.nav.SettingsRoute
import com.eugene.aichat.ui.chat.components.MessageList
import com.eugene.aichat.ui.components.AppTopBar
import com.eugene.aichat.ui.components.EmptyStateHero
import com.eugene.aichat.ui.components.InputBar
import com.eugene.aichat.ui.components.ModelSelector
import com.eugene.aichat.ui.theme.Dimens

@Composable
fun ChatScreen(
    chatId: String,
    agentId: String?,
    navController: NavHostController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isNew = chatId == NavArgs.NEW_CHAT
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (msg != null) {
            snackbar.showSnackbar(msg)
            viewModel.onIntent(ChatIntent.DismissError)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                AppTopBar(
                    title = if (isNew && state.messages.isEmpty()) stringResource(R.string.app_name)
                    else "AIChat",
                    onMenu = { /* sidebar hookup in step 12 */ },
                    onSettings = { navController.navigate(SettingsRoute) },
                    onVoiceToggle = { /* voice hookup in step 8 */ },
                    centerSlot = {
                        ModelSelector(
                            models = state.availableModels,
                            activeModel = state.activeModel,
                            onPick = { viewModel.onIntent(ChatIntent.PickModel(it)) }
                        )
                    }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (state.messages.isEmpty()) {
                        EmptyStateHero(
                            title = stringResource(R.string.app_name),
                            subtitle = stringResource(R.string.chat_empty_subtitle),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        MessageList(
                            messages = state.messages,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                InputBar(
                    value = state.draft,
                    onValueChange = { viewModel.onIntent(ChatIntent.UpdateDraft(it)) },
                    onSend = { viewModel.onIntent(ChatIntent.SendDraft(chatId)) },
                    onAttach = { /* attachments in step 7 */ },
                    onVoicePress = { /* voice mode in step 8 */ },
                    onStop = { viewModel.onIntent(ChatIntent.StopStreaming) },
                    isStreaming = state.isStreaming,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp)
                )
            }

            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
            ) { data -> Snackbar(snackbarData = data) }
        }
    }
}
