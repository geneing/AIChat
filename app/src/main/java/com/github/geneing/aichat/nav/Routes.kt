package com.github.geneing.aichat.nav

import kotlinx.serialization.Serializable

object NavArgs {
    const val NEW_CHAT = "new"
}

@Serializable
data class ChatRoute(
    val chatId: String = NavArgs.NEW_CHAT,
    val agentId: String? = null
)

@Serializable
data object HomeRoute

@Serializable
data object HistoryRoute

@Serializable
data object SettingsRoute

@Serializable
data class ModelEditorRoute(val modelId: String? = null)

@Serializable
data class SkillEditorRoute(val skillId: String? = null)

@Serializable
data class AgentEditorRoute(val agentId: String? = null)
