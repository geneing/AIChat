package com.eugene.aichat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.aichat.core.data.repository.AgentRepository
import com.eugene.aichat.core.data.repository.ModelConfigRepository
import com.eugene.aichat.core.data.repository.SkillRepository
import com.eugene.aichat.core.domain.model.Agent
import com.eugene.aichat.core.domain.model.ModelConfig
import com.eugene.aichat.core.domain.model.Skill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModelConfigUi(
    val id: String,
    val displayName: String,
    val providerType: com.eugene.aichat.core.domain.model.ProviderType,
    val baseUrl: String,
    val model: String,
    val hasApiKey: Boolean,
    val isDefault: Boolean
)

data class SkillUi(
    val id: String,
    val name: String,
    val description: String,
    val isBuiltIn: Boolean,
    val isEnabled: Boolean
)

data class AgentUi(
    val id: String,
    val name: String,
    val description: String,
    val isBuiltIn: Boolean,
    val isEnabled: Boolean
)

data class SettingsUiState(
    val models: List<ModelConfigUi> = emptyList(),
    val skills: List<SkillUi> = emptyList(),
    val agents: List<AgentUi> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val modelConfigRepository: ModelConfigRepository,
    private val skillRepository: SkillRepository,
    private val agentRepository: AgentRepository
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = combine(
        modelConfigRepository.observeAll(),
        skillRepository.observeAll(),
        agentRepository.observeAll()
    ) { models, skills, agents ->
        SettingsUiState(
            models = models.map { it.toUi() },
            skills = skills.map { it.toUi() },
            agents = agents.map { it.toUi() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setDefault(modelId: String) {
        viewModelScope.launch { modelConfigRepository.setDefault(modelId) }
    }

    fun deleteModel(modelId: String) {
        viewModelScope.launch { modelConfigRepository.deleteById(modelId) }
    }

    fun setSkillEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch { skillRepository.setEnabled(id, enabled) }
    }

    fun deleteSkill(id: String) {
        viewModelScope.launch { skillRepository.deleteUserSkill(id) }
    }

    fun setAgentEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch { agentRepository.setEnabled(id, enabled) }
    }

    fun deleteAgent(id: String) {
        viewModelScope.launch { agentRepository.deleteUserAgent(id) }
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

    private fun Skill.toUi(): SkillUi = SkillUi(
        id = id, name = name, description = description,
        isBuiltIn = isBuiltIn, isEnabled = isEnabled
    )

    private fun Agent.toUi(): AgentUi = AgentUi(
        id = id, name = name, description = description,
        isBuiltIn = isBuiltIn, isEnabled = isEnabled
    )
}
