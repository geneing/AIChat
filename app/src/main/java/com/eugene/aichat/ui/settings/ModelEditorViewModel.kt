package com.eugene.aichat.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.aichat.core.data.repository.ModelConfigRepository
import com.eugene.aichat.core.domain.model.ModelConfig
import com.eugene.aichat.core.domain.model.ProviderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ModelEditorUiState(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String = "",
    val providerType: ProviderType = ProviderType.OPENAI,
    val baseUrl: String = defaultBaseUrl(ProviderType.OPENAI),
    val model: String = "",
    val apiKey: String = "",
    val temperature: Float = 0.7f,
    val topP: Float = 1.0f,
    val maxTokens: Int = 4096,
    val supportsTools: Boolean = true,
    val supportsVision: Boolean = false,
    val supportsAudio: Boolean = false,
    val isDefault: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val isEditing: Boolean = false
)

@HiltViewModel
class ModelEditorViewModel @Inject constructor(
    private val modelConfigRepository: ModelConfigRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ModelEditorUiState())
    val state: StateFlow<ModelEditorUiState> = _state.asStateFlow()

    init {
        // Pull the optional modelId from the route arguments (populated by Nav).
        val modelId: String? = savedStateHandle.get<String>("modelId")
        if (modelId != null) loadExisting(modelId)
    }

    private fun loadExisting(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val existing = modelConfigRepository.getById(id)
            if (existing == null) {
                _state.update { it.copy(isLoading = false, errorMessage = "Model not found") }
                return@launch
            }
            _state.update {
                it.copy(
                    id = existing.id,
                    displayName = existing.displayName,
                    providerType = existing.providerType,
                    baseUrl = existing.baseUrl,
                    model = existing.model,
                    apiKey = existing.apiKey,
                    temperature = existing.temperature,
                    topP = existing.topP,
                    maxTokens = existing.maxTokens,
                    supportsTools = existing.supportsTools,
                    supportsVision = existing.supportsVision,
                    supportsAudio = existing.supportsAudio,
                    isDefault = existing.isDefault,
                    isLoading = false,
                    isEditing = true
                )
            }
        }
    }

    fun setDisplayName(value: String) = _state.update { it.copy(displayName = value) }
    fun setProviderType(value: ProviderType) = _state.update {
        it.copy(
            providerType = value,
            baseUrl = if (it.baseUrl.isBlank() || it.baseUrl == defaultBaseUrl(it.providerType)) {
                defaultBaseUrl(value)
            } else it.baseUrl
        )
    }
    fun setBaseUrl(value: String) = _state.update { it.copy(baseUrl = value) }
    fun setModel(value: String) = _state.update { it.copy(model = value) }
    fun setApiKey(value: String) = _state.update { it.copy(apiKey = value) }
    fun setTemperature(value: Float) = _state.update { it.copy(temperature = value) }
    fun setMaxTokens(value: Int) = _state.update { it.copy(maxTokens = value) }
    fun setSupportsTools(value: Boolean) = _state.update { it.copy(supportsTools = value) }
    fun setSupportsVision(value: Boolean) = _state.update { it.copy(supportsVision = value) }
    fun setSupportsAudio(value: Boolean) = _state.update { it.copy(supportsAudio = value) }
    fun setDefault(value: Boolean) = _state.update { it.copy(isDefault = value) }

    fun save() {
        val current = _state.value
        if (current.displayName.isBlank() || current.model.isBlank()) {
            _state.update { it.copy(errorMessage = "Display name and model are required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                modelConfigRepository.upsert(
                    ModelConfig(
                        id = current.id,
                        displayName = current.displayName,
                        providerType = current.providerType,
                        baseUrl = current.baseUrl,
                        model = current.model,
                        apiKey = current.apiKey,
                        temperature = current.temperature,
                        topP = current.topP,
                        maxTokens = current.maxTokens,
                        supportsTools = current.supportsTools,
                        supportsVision = current.supportsVision,
                        supportsAudio = current.supportsAudio,
                        isDefault = current.isDefault
                    )
                )
            }.onSuccess {
                _state.update { it.copy(isLoading = false, isSaved = true) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "Save failed") }
            }
        }
    }
}

internal fun defaultBaseUrl(provider: ProviderType): String = when (provider) {
    ProviderType.OPENAI -> "https://api.openai.com/v1"
    ProviderType.OPENROUTER -> "https://openrouter.ai/api/v1"
    ProviderType.OPENCODE -> "https://opencode.ai/api/v1"
}
