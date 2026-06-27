package com.eugene.aichat.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.aichat.core.data.repository.AgentRepository
import com.eugene.aichat.core.data.repository.SkillRepository
import com.eugene.aichat.core.domain.model.Agent
import com.eugene.aichat.core.domain.model.Skill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SkillEditorUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val systemPrompt: String = "",
    val body: String = "",
    val tagsCsv: String = "",
    val toolAllowListCsv: String = "",
    val isEnabled: Boolean = true,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEditing: Boolean = false
)

@HiltViewModel
class SkillEditorViewModel @Inject constructor(
    private val skillRepository: SkillRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(SkillEditorUiState())
    val state: StateFlow<SkillEditorUiState> = _state.asStateFlow()

    init {
        val id = savedStateHandle.get<String>("skillId")
        if (id != null) loadExisting(id)
    }

    private fun loadExisting(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val existing = skillRepository.getById(id)
            if (existing == null) {
                _state.update { it.copy(isLoading = false, errorMessage = "Skill not found") }
                return@launch
            }
            _state.update {
                it.copy(
                    id = existing.id,
                    name = existing.name,
                    description = existing.description,
                    systemPrompt = existing.systemPrompt,
                    body = existing.body,
                    tagsCsv = existing.tags.joinToString(","),
                    toolAllowListCsv = existing.toolAllowList.joinToString(","),
                    isEnabled = existing.isEnabled,
                    isLoading = false,
                    isEditing = true
                )
            }
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setDescription(v: String) = _state.update { it.copy(description = v) }
    fun setSystemPrompt(v: String) = _state.update { it.copy(systemPrompt = v) }
    fun setBody(v: String) = _state.update { it.copy(body = v) }
    fun setTagsCsv(v: String) = _state.update { it.copy(tagsCsv = v) }
    fun setToolAllowListCsv(v: String) = _state.update { it.copy(toolAllowListCsv = v) }
    fun setEnabled(v: Boolean) = _state.update { it.copy(isEnabled = v) }

    fun save() {
        val cur = _state.value
        if (cur.name.isBlank()) {
            _state.update { it.copy(errorMessage = "Name is required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                skillRepository.upsert(
                    Skill(
                        id = cur.id,
                        name = cur.name,
                        description = cur.description,
                        systemPrompt = cur.systemPrompt,
                        body = cur.body,
                        tags = cur.tagsCsv.splitCsv(),
                        toolAllowList = cur.toolAllowListCsv.splitCsv(),
                        isBuiltIn = false,
                        isEnabled = cur.isEnabled,
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

internal fun String.splitCsv(): List<String> =
    if (isBlank()) emptyList() else split(",").map { it.trim() }.filter { it.isNotEmpty() }
