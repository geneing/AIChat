package com.eugene.aichat.core.data.repository

import com.eugene.aichat.core.data.db.entity.AgentEntity
import com.eugene.aichat.core.data.db.entity.AttachmentEntity
import com.eugene.aichat.core.data.db.entity.ChatEntity
import com.eugene.aichat.core.data.db.entity.MessageEntity
import com.eugene.aichat.core.data.db.entity.ModelConfigEntity
import com.eugene.aichat.core.data.db.entity.SkillEntity
import com.eugene.aichat.core.data.db.entity.SourceRefEntity
import com.eugene.aichat.core.domain.model.Agent
import com.eugene.aichat.core.domain.model.Attachment
import com.eugene.aichat.core.domain.model.Chat
import com.eugene.aichat.core.domain.model.Message
import com.eugene.aichat.core.domain.model.MessageContentKind
import com.eugene.aichat.core.domain.model.ModelConfig
import com.eugene.aichat.core.domain.model.ProviderType
import com.eugene.aichat.core.domain.model.Role
import com.eugene.aichat.core.domain.model.Skill
import com.eugene.aichat.core.domain.model.SourceRef

internal fun ChatEntity.toDomain(): Chat = Chat(
    id = id,
    title = title,
    agentId = agentId,
    modelConfigId = modelConfigId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isArchived = isArchived
)

internal fun Chat.toEntity(): ChatEntity = ChatEntity(
    id = id,
    title = title,
    agentId = agentId,
    modelConfigId = modelConfigId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isArchived = isArchived
)

internal fun MessageEntity.toDomain(
    attachments: List<Attachment> = emptyList(),
    sources: List<SourceRef> = emptyList()
): Message = Message(
    id = id,
    chatId = chatId,
    role = runCatching { Role.valueOf(role.uppercase()) }.getOrDefault(Role.USER),
    contentText = contentText,
    thinkingText = thinkingText,
    isStreaming = isStreaming,
    modelId = modelId,
    createdAt = createdAt,
    latencyMs = latencyMs,
    attachments = attachments,
    sources = sources
)

internal fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    chatId = chatId,
    role = role.name.lowercase(),
    contentText = contentText,
    thinkingText = thinkingText,
    isStreaming = isStreaming,
    modelId = modelId,
    createdAt = createdAt,
    latencyMs = latencyMs
)

internal fun AttachmentEntity.toDomain(): Attachment = Attachment(
    id = id,
    messageId = messageId,
    kind = runCatching { MessageContentKind.valueOf(kind.uppercase()) }
        .getOrDefault(MessageContentKind.FILE),
    mimeType = mimeType,
    localUri = localUri,
    remoteUrl = remoteUrl,
    width = width,
    height = height,
    durationMs = durationMs
)

internal fun Attachment.toEntity(): AttachmentEntity = AttachmentEntity(
    id = id,
    messageId = messageId,
    kind = kind.name.lowercase(),
    mimeType = mimeType,
    localUri = localUri,
    remoteUrl = remoteUrl,
    width = width,
    height = height,
    durationMs = durationMs
)

internal fun SourceRefEntity.toDomain(): SourceRef = SourceRef(
    id = id,
    messageId = messageId,
    title = title,
    url = url,
    snippet = snippet
)

internal fun ModelConfigEntity.toDomain(): ModelConfig = ModelConfig(
    id = id,
    displayName = displayName,
    providerType = runCatching { ProviderType.valueOf(providerType.uppercase()) }
        .getOrDefault(ProviderType.OPENAI),
    baseUrl = baseUrl,
    model = model,
    apiKey = apiKeyEncrypted,
    temperature = temperature,
    topP = topP,
    maxTokens = maxTokens,
    supportsTools = supportsTools,
    supportsVision = supportsVision,
    supportsAudio = supportsAudio,
    isDefault = isDefault
)

internal fun ModelConfig.toEntity(createdAt: Long, updatedAt: Long): ModelConfigEntity =
    ModelConfigEntity(
        id = id,
        displayName = displayName,
        providerType = providerType.name,
        baseUrl = baseUrl,
        model = model,
        apiKeyEncrypted = apiKey,
        temperature = temperature,
        topP = topP,
        maxTokens = maxTokens,
        supportsTools = supportsTools,
        supportsVision = supportsVision,
        supportsAudio = supportsAudio,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

internal fun SkillEntity.toDomain(): Skill = Skill(
    id = id,
    name = name,
    description = description,
    systemPrompt = systemPrompt,
    body = bodyMarkdown,
    tags = tagsCsv.splitCsv(),
    toolAllowList = toolAllowListCsv.splitCsv(),
    isBuiltIn = isBuiltIn,
    isEnabled = isEnabled,
    version = version
)

internal fun Skill.toEntity(createdAt: Long, updatedAt: Long): SkillEntity = SkillEntity(
    id = id,
    name = name,
    description = description,
    systemPrompt = systemPrompt,
    bodyMarkdown = body,
    tagsCsv = tags.joinToStringCsv(),
    toolAllowListCsv = toolAllowList.joinToStringCsv(),
    isBuiltIn = isBuiltIn,
    isEnabled = isEnabled,
    version = version,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun AgentEntity.toDomain(): Agent = Agent(
    id = id,
    name = name,
    description = description,
    systemPrompt = systemPrompt,
    modelConfigHint = modelConfigHint,
    modelConfigId = modelConfigId,
    skillIds = skillIdsCsv.splitCsv(),
    toolAllowList = toolAllowListCsv.splitCsv(),
    maxSteps = maxSteps,
    temperature = temperature,
    isBuiltIn = isBuiltIn,
    isEnabled = isEnabled,
    version = version
)

internal fun Agent.toEntity(createdAt: Long, updatedAt: Long): AgentEntity = AgentEntity(
    id = id,
    name = name,
    description = description,
    systemPrompt = systemPrompt,
    modelConfigHint = modelConfigHint,
    modelConfigId = modelConfigId,
    skillIdsCsv = skillIds.joinToStringCsv(),
    toolAllowListCsv = toolAllowList.joinToStringCsv(),
    maxSteps = maxSteps,
    temperature = temperature,
    isBuiltIn = isBuiltIn,
    isEnabled = isEnabled,
    version = version,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun String.splitCsv(): List<String> =
    if (isBlank()) emptyList() else split(",").map { it.trim() }.filter { it.isNotEmpty() }

private fun List<String>.joinToStringCsv(): String = joinToString(",")
