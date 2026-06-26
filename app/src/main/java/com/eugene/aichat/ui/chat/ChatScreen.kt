package com.eugene.aichat.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.eugene.aichat.R
import com.eugene.aichat.nav.NavArgs
import com.eugene.aichat.nav.SettingsRoute
import com.eugene.aichat.ui.chat.components.MessageList
import com.eugene.aichat.ui.components.AppTopBar
import com.eugene.aichat.ui.components.AttachmentsSheet
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
    val context = LocalContext.current

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (msg != null) {
            snackbar.showSnackbar(msg)
            viewModel.onIntent(ChatIntent.DismissError)
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.onIntent(ChatIntent.AddAttachment(uri))
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { /* placeholder — wired in step 7+ with FileProvider URI */ }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // The actual URI is created by the caller; for the scaffold we
            // just open the gallery as a stand-in.
            photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
                    onAttach = { viewModel.onIntent(ChatIntent.OpenAttachments) },
                    onVoicePress = { /* voice mode in step 8 */ },
                    onStop = { viewModel.onIntent(ChatIntent.StopStreaming) },
                    isStreaming = state.isStreaming,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp)
                )
            }

            if (state.isAttachmentsSheetOpen) {
                AttachmentsSheet(
                    onTakePhoto = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            // Real implementation would launch TakePicture with a FileProvider URI.
                            photoLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onPickFromGallery = {
                        photoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onVoiceMemo = { /* voice memo UI ships in step 8 */ },
                    onDismiss = { viewModel.onIntent(ChatIntent.CloseAttachments) }
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
