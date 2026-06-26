package com.eugene.aichat.core.domain.model

enum class Role { SYSTEM, USER, ASSISTANT, TOOL }

enum class MessageContentKind { TEXT, IMAGE, AUDIO }

data class MessageContent(
    val kind: MessageContentKind,
    val text: String? = null,
    val localUri: String? = null,
    val remoteUrl: String? = null,
    val mimeType: String? = null
)

data class Attachment(
    val id: String,
    val messageId: String,
    val kind: MessageContentKind,
    val mimeType: String,
    val localUri: String?,
    val remoteUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null
)

data class SourceRef(
    val id: String,
    val messageId: String,
    val title: String?,
    val url: String,
    val snippet: String?
)

data class Message(
    val id: String,
    val chatId: String,
    val role: Role,
    val contentText: String?,
    val thinkingText: String? = null,
    val isStreaming: Boolean = false,
    val modelId: String? = null,
    val createdAt: Long,
    val latencyMs: Long? = null,
    val attachments: List<Attachment> = emptyList(),
    val sources: List<SourceRef> = emptyList()
)

data class Chat(
    val id: String,
    val title: String,
    val agentId: String?,
    val modelConfigId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false
)

data class ModelConfig(
    val id: String,
    val displayName: String,
    val providerType: ProviderType,
    val baseUrl: String,
    val model: String,
    val apiKey: String,
    val temperature: Float,
    val topP: Float,
    val maxTokens: Int,
    val supportsTools: Boolean,
    val supportsVision: Boolean,
    val supportsAudio: Boolean,
    val isDefault: Boolean
)

enum class ProviderType {
    OPENAI,
    OPENROUTER,
    OPENCODE
}

data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val body: String,
    val tags: List<String>,
    val toolAllowList: List<String>,
    val isBuiltIn: Boolean,
    val isEnabled: Boolean,
    val version: Int
)

data class Agent(
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val modelConfigHint: String?,
    val modelConfigId: String?,
    val skillIds: List<String>,
    val toolAllowList: List<String>,
    val maxSteps: Int,
    val temperature: Float?,
    val isBuiltIn: Boolean,
    val isEnabled: Boolean,
    val version: Int
)
