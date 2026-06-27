package com.eugene.aichat.ui.sidebar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eugene.aichat.core.data.repository.AgentRepository
import com.eugene.aichat.core.data.repository.ChatRepository
import com.eugene.aichat.core.data.repository.SkillRepository
import com.eugene.aichat.core.domain.model.Agent
import com.eugene.aichat.core.domain.model.Chat
import com.eugene.aichat.core.domain.model.Skill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val chats: List<Chat> = emptyList(),
    val enabledSkills: List<Skill> = emptyList(),
    val enabledAgents: List<Agent> = emptyList()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val skillRepository: SkillRepository,
    private val agentRepository: AgentRepository
) : ViewModel() {

    val state: StateFlow<HistoryUiState> = combine(
        chatRepository.observeChats(),
        skillRepository.observeEnabled(),
        agentRepository.observeEnabled()
    ) { chats, skills, agents ->
        HistoryUiState(chats = chats, enabledSkills = skills, enabledAgents = agents)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun archive(chatId: String) {
        viewModelScope.launch { chatRepository.setArchived(chatId, true) }
    }

    fun delete(chatId: String) {
        viewModelScope.launch { chatRepository.deleteChat(chatId) }
    }
}
