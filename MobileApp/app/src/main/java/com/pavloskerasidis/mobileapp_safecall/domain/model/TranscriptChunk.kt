package com.pavloskerasidis.mobileapp_safecall.domain.model

data class TranscriptChunk(
    val text: String,
    val startTimestampMs: Long,
    val durationMs: Int,
    val confidence: Float,
)
