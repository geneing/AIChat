package com.github.geneing.aichat.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.geneing.aichat.core.data.repository.AgentRepository
import com.github.geneing.aichat.core.data.repository.ModelConfigRepository
import com.github.geneing.aichat.core.domain.model.Agent
import com.github.geneing.aichat.core.domain.model.ModelConfig
import com.github.geneing.aichat.core.domain.model.Skill
import com.github.geneing.aichat.core.data.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AgentEditorUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val systemPrompt: String = "",
    val modelConfigId: String? = null,
    val skillIds: Set<String> = emptySet(),
    val toolAllowListCsv: String = "",
    val maxSteps: Int = 6,
    val temperature: Float? = null,
    val availableModels: List<ModelConfig> = emptyList(),
    val availableSkills: List<Skill> = emptyList(),
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEditing: Boolean = false
)

@HiltViewModel
class AgentEditorViewModel @Inject constructor(
    private val agentRepository: AgentRepository,
    private val skillRepository: SkillRepository,
    private val modelConfigRepository: ModelConfigRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AgentEditorUiState())
    val state: StateFlow<AgentEditorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                modelConfigRepository.observeAll(),
                skillRepository.observeAll()
            ) { models, skills ->
                _state.value to (models to skills)
            }.collect { (cur, modelsSkills) ->
                val (models, skills) = modelsSkills
                _state.update {
                    it.copy(availableModels = models, availableSkills = skills)
                }
            }
        }
        val id = savedStateHandle.get<String>("agentId")
        if (id != null) loadExisting(id)
    }

    private fun loadExisting(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val existing = agentRepository.getById(id)
            if (existing == null) {
                _state.update { it.copy(isLoading = false, errorMessage = "Agent not found") }
                return@launch
            }
            _state.update {
                it.copy(
                    id = existing.id,
                    name = existing.name,
                    description = existing.description,
                    systemPrompt = existing.systemPrompt,
                    modelConfigId = existing.modelConfigId,
                    skillIds = existing.skillIds.toSet(),
                    toolAllowListCsv = existing.toolAllowList.joinToString(","),
                    maxSteps = existing.maxSteps,
                    temperature = existing.temperature,
                    isLoading = false,
                    isEditing = true
                )
            }
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setDescription(v: String) = _state.update { it.copy(description = v) }
    fun setSystemPrompt(v: String) = _state.update { it.copy(systemPrompt = v) }
    fun setModelConfigId(v: String?) = _state.update { it.copy(modelConfigId = v) }
    fun setMaxSteps(v: Int) = _state.update { it.copy(maxSteps = v.coerceIn(1, 20)) }
    fun setTemperature(v: Float?) = _state.update { it.copy(temperature = v) }
    fun toggleSkill(id: String) = _state.update {
        it.copy(skillIds = if (id in it.skillIds) it.skillIds - id else it.skillIds + id)
    }
    fun setToolAllowListCsv(v: String) = _state.update { it.copy(toolAllowListCsv = v) }

    fun save() {
        val cur = _state.value
        if (cur.name.isBlank()) {
            _state.update { it.copy(errorMessage = "Name is required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                agentRepository.upsert(
                    Agent(
                        id = cur.id,
                        name = cur.name,
                        description = cur.description,
                        systemPrompt = cur.systemPrompt,
                        modelConfigHint = null,
                        modelConfigId = cur.modelConfigId,
                        skillIds = cur.skillIds.toList(),
                        toolAllowList = cur.toolAllowListCsv.splitCsv(),
                        maxSteps = cur.maxSteps,
                        temperature = cur.temperature,
                        isBuiltIn = false,
                        isEnabled = true,
                        version = 1
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

internal fun String.splitCsvAgent(): List<String> =
    if (isBlank()) emptyList() else split(",").map { it.trim() }.filter { it.isNotEmpty() }
