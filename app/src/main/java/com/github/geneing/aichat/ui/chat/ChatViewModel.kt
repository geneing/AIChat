package com.github.geneing.aichat.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.geneing.aichat.core.data.repository.ChatRepository
import com.github.geneing.aichat.core.data.repository.ModelConfigRepository
import com.github.geneing.aichat.core.domain.model.ModelConfig
import com.github.geneing.aichat.core.domain.usecase.CancelStreamUseCase
import com.github.geneing.aichat.core.domain.usecase.SendMessageUseCase
import com.github.geneing.aichat.core.domain.usecase.StreamResponseUseCase
import com.github.geneing.aichat.core.voice.VoiceSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val modelConfigRepository: ModelConfigRepository,
    private val sendMessage: SendMessageUseCase,
    private val streamResponse: StreamResponseUseCase,
    private val cancelStream: CancelStreamUseCase,
    private val voiceSession: VoiceSession,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialChatId: String? = savedStateHandle.get<String>("chatId")
    private val initialAgentId: String? = savedStateHandle.get<String>("agentId")

    private val _state = MutableStateFlow(ChatUiState(chatId = initialChatId))
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var streamJob: Job? = null
    private var observingChatId: String? = null

    init {
        observeModels()
        applyAgentIfNeeded(initialAgentId)
    }

    private fun observeMessages() {
        val chatId = _state.value.chatId
        if (chatId == null || chatId == SendMessageUseCase.NEW_CHAT_SENTINEL) {
            _state.update { it.copy(messages = emptyList()) }
            return
        }
        if (observingChatId == chatId) return
        observingChatId = chatId
        viewModelScope.launch {
            chatRepository.observeMessages(chatId).collect { msgs ->
                _state.update { it.copy(messages = msgs) }
            }
        }
    }

    private fun observeModels() {
        viewModelScope.launch {
            modelConfigRepository.observeAll().collect { models ->
                val active = models.firstOrNull { it.isDefault } ?: models.firstOrNull()
                _state.update {
                    it.copy(
                        availableModels = models,
                        activeModel = active
                    )
                }
            }
        }
    }

    private fun applyAgentIfNeeded(agentId: String?) {
        if (agentId.isNullOrBlank()) return
        // Future: load agent and bind its model + skills.
    }

    fun onIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.UpdateDraft -> _state.update { it.copy(draft = intent.text) }
            is ChatIntent.SendDraft -> sendDraft()
            ChatIntent.StopStreaming -> stopStream()
            is ChatIntent.PickModel -> setActiveModel(intent.modelId)
            ChatIntent.DismissError -> _state.update { it.copy(errorMessage = null) }
            ChatIntent.OpenAttachments -> _state.update { it.copy(isAttachmentsSheetOpen = true) }
            ChatIntent.CloseAttachments -> _state.update { it.copy(isAttachmentsSheetOpen = false) }
            is ChatIntent.AddAttachment -> _state.update {
                it.copy(pendingAttachments = (it.pendingAttachments + intent.uri).distinct())
            }
            is ChatIntent.RemoveAttachment -> _state.update {
                it.copy(pendingAttachments = it.pendingAttachments - intent.uri)
            }
            ChatIntent.OpenVoiceMode -> _state.update {
                it.copy(voiceOverlay = com.github.geneing.aichat.core.voice.VoiceUiState())
            }
            ChatIntent.CloseVoiceMode -> {
                voiceSession.shutdown()
                _state.update { it.copy(voiceOverlay = null) }
            }
            ChatIntent.StartVoiceListening -> voiceSession.startListening()
        }
    }

    private fun sendDraft() {
        val current = _state.value
        val text = current.draft.trim()
        if (text.isEmpty() || current.isStreaming) return
        val chatId = current.chatId
        val model = current.activeModel
        if (model == null) {
            _state.update { it.copy(errorMessage = "Add a model in Settings first.") }
            return
        }
        _state.update { it.copy(draft = "", isStreaming = true, errorMessage = null) }
        viewModelScope.launch {
            val result = runCatching { sendMessage(chatId, text) }
            val sent = result.getOrElse { e ->
                _state.update { it.copy(isStreaming = false, errorMessage = "Failed to send: ${e.message}") }
                return@launch
            }
            _state.update { it.copy(chatId = sent.chatId) }
            observeMessages()
            startStream(sent.chatId, sent.assistantMessageId, model)
        }
    }

    private fun startStream(chatId: String, assistantMessageId: String, model: ModelConfig) {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            try {
                streamResponse(chatId, model, assistantMessageId).collect { /* events persisted inside use case */ }
            } finally {
                _state.update { it.copy(isStreaming = false) }
            }
        }
    }

    private fun stopStream() {
        val assistantId = _state.value.messages.lastOrNull { it.isStreaming }?.id
        streamJob?.cancel()
        streamJob = null
        if (assistantId != null) {
            viewModelScope.launch { cancelStream(assistantId) }
        }
        _state.update { it.copy(isStreaming = false) }
    }

    private fun setActiveModel(modelId: String) {
        viewModelScope.launch {
            val model = modelConfigRepository.getById(modelId) ?: return@launch
            modelConfigRepository.setDefault(modelId)
            _state.update { it.copy(activeModel = model) }
        }
    }

    override fun onCleared() {
        streamJob?.cancel()
        super.onCleared()
    }
}
