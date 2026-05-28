package com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaudeMessageRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String? = null,
    val messages: List<ClaudeMessage>,
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ClaudeMessageResponse(
    val id: String,
    val model: String,
    val content: List<ClaudeContentBlock>,
    @SerialName("stop_reason") val stopReason: String? = null,
)

@Serializable
data class ClaudeContentBlock(
    val type: String,
    val text: String? = null,
)
