package com.github.geneing.aichat.ui.chat

import android.net.Uri
import com.github.geneing.aichat.core.domain.model.Message
import com.github.geneing.aichat.core.domain.model.ModelConfig
import com.github.geneing.aichat.core.voice.VoiceUiState

data class ChatUiState(
    val chatId: String? = null,
    val messages: List<Message> = emptyList(),
    val draft: String = "",
    val isStreaming: Boolean = false,
    val activeModel: ModelConfig? = null,
    val availableModels: List<ModelConfig> = emptyList(),
    val activeAgentId: String? = null,
    val errorMessage: String? = null,
    val pendingAttachments: List<Uri> = emptyList(),
    val isAttachmentsSheetOpen: Boolean = false,
    val voiceOverlay: VoiceUiState? = null
)

sealed interface ChatIntent {
    data class UpdateDraft(val text: String) : ChatIntent
    data class SendDraft(val chatId: String?) : ChatIntent
    data object StopStreaming : ChatIntent
    data class PickModel(val modelId: String) : ChatIntent
    data object DismissError : ChatIntent
    data object OpenAttachments : ChatIntent
    data object CloseAttachments : ChatIntent
    data class AddAttachment(val uri: Uri) : ChatIntent
    data class RemoveAttachment(val uri: Uri) : ChatIntent
    data object OpenVoiceMode : ChatIntent
    data object CloseVoiceMode : ChatIntent
    data object StartVoiceListening : ChatIntent
}
