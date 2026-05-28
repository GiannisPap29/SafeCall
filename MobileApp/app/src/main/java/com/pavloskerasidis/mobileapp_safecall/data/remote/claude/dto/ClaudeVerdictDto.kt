package com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClaudeVerdictDto(
    val verdict: String,
    val confidence: Float = 0f,
    val reason: String = "",
)
