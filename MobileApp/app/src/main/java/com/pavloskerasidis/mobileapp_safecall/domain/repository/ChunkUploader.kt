package com.pavloskerasidis.mobileapp_safecall.domain.repository

import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import java.io.File

interface ChunkUploader {
    suspend fun upload(file: File, metadata: ChunkMetadata): AppResult<Unit>
}

data class ChunkMetadata(
    val callId: String,
    val sequence: Int,
    val startTimestampMs: Long,
    val durationMs: Int,
    val sampleRateHz: Int,
)
