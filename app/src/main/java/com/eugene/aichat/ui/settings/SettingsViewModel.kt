package com.eugene.aichat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.aichat.core.data.repository.ModelConfigRepository
import com.eugene.aichat.core.domain.model.ModelConfig
import com.eugene.aichat.core.domain.model.ProviderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModelConfigUi(
    val id: String,
    val displayName: String,
    val providerType: ProviderType,
    val baseUrl: String,
    val model: String,
    val hasApiKey: Boolean,
    val isDefault: Boolean
)

data class SettingsUiState(
    val models: List<ModelConfigUi> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val modelConfigRepository: ModelConfigRepository
) : ViewModel() {

    private val internal = MutableStateFlow(SettingsUiState())

    val state: StateFlow<SettingsUiState> = combine(
        internal,
        modelConfigRepository.observeAll()
    ) { _, models ->
        SettingsUiState(models = models.map { it.toUi() })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setDefault(modelId: String) {
        viewModelScope.launch { modelConfigRepository.setDefault(modelId) }
    }

    fun deleteModel(modelId: String) {
        viewModelScope.launch { modelConfigRepository.deleteById(modelId) }
    }

    private fun ModelConfig.toUi(): ModelConfigUi = ModelConfigUi(
        id = id,
        displayName = displayName,
        providerType = providerType,
        baseUrl = baseUrl,
        model = model,
        hasApiKey = apiKey.isNotEmpty(),
        isDefault = isDefault
    )
}
