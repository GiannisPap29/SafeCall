package com.pavloskerasidis.mobileapp_safecall.data.remote.stt

import com.pavloskerasidis.mobileapp_safecall.core.config.ApiKeyProvider
import com.pavloskerasidis.mobileapp_safecall.domain.model.AudioChunk
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import io.ktor.client.HttpClient

class KtorGoogleSpeechApi(
    private val client: HttpClient,
    private val keys: ApiKeyProvider,
) : GoogleSpeechApi {
    override suspend fun recognize(chunk: AudioChunk): TranscriptChunk {
        // TODO: POST https://speech.googleapis.com/v2/projects/.../recognizers/_:recognize with API key.
        return TranscriptChunk(
            text = "",
            startTimestampMs = chunk.startTimestampMs,
            durationMs = chunk.durationMs,
            confidence = 0f,
        )
    }
}
