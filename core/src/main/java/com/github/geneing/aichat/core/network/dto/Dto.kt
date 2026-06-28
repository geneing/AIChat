package com.github.geneing.aichat.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatRequestDto(
    val model: String,
    val messages: List<ChatMessageDto>,
    val temperature: Float? = null,
    @SerialName("top_p") val topP: Float? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    val stream: Boolean = true,
    val tools: List<ToolDefinitionDto>? = null,
    @SerialName("tool_choice") val toolChoice: String? = "auto",
    val user: String? = null
)

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: JsonElement? = null,
    val name: String? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCallDto>? = null
)

@Serializable
data class ContentPartDto(
    val type: String,
    val text: String? = null,
    @SerialName("image_url") val imageUrl: ImageUrlDto? = null,
    @SerialName("input_audio") val inputAudio: InputAudioDto? = null
)

@Serializable
data class ImageUrlDto(
    val url: String,
    val detail: String? = "auto"
)

@Serializable
data class InputAudioDto(
    @SerialName("data") val base64Data: String,
    val format: String
)

@Serializable
data class ToolDefinitionDto(
    val type: String = "function",
    val function: ToolFunctionDto
)

@Serializable
data class ToolFunctionDto(
    val name: String,
    val description: String,
    val parameters: JsonElement
)

@Serializable
data class ToolCallDto(
    val id: String,
    val type: String = "function",
    val function: ToolCallFunctionDto
)

@Serializable
data class ToolCallFunctionDto(
    val name: String,
    val arguments: String
)

@Serializable
data class ChatStreamChunkDto(
    val id: String,
    val model: String,
    val choices: List<ChatStreamChoiceDto>
)

@Serializable
data class ChatStreamChoiceDto(
    val index: Int,
    val delta: ChatMessageDeltaDto,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class ChatMessageDeltaDto(
    val role: String? = null,
    val content: String? = null,
    val reasoning_content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCallDeltaDto>? = null
)

@Serializable
data class ToolCallDeltaDto(
    val index: Int,
    val id: String? = null,
    val type: String? = null,
    val function: ToolCallFunctionDeltaDto? = null
)

@Serializable
data class ToolCallFunctionDeltaDto(
    val name: String? = null,
    val arguments: String? = null
)

@Serializable
data class ModelListDto(
    val `object`: String? = null,
    val data: List<ModelListItemDto>
)

@Serializable
data class ModelListItemDto(
    val id: String,
    @SerialName("owned_by") val ownedBy: String? = null
)

@Serializable
data class ErrorBodyDto(
    val error: ErrorPayloadDto? = null,
    val message: String? = null
)

@Serializable
data class ErrorPayloadDto(
    val message: String,
    val type: String? = null,
    val code: String? = null
)
