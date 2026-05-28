package com.pavloskerasidis.mobileapp_safecall.data.local.stt

import com.pavloskerasidis.mobileapp_safecall.core.dispatchers.AppDispatchers
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.core.result.AppError
import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.model.AudioChunk
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechTranscriber
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.vosk.Recognizer

class VoskSpeechTranscriber(
    private val models: VoskModelProvider,
    private val dispatchers: AppDispatchers,
    private val logger: Logger,
) : SpeechTranscriber {

    override suspend fun transcribe(chunk: AudioChunk): AppResult<TranscriptChunk> =
        withContext(dispatchers.default) {
            runCatching {
                Recognizer(models.get(), chunk.sampleRateHz.toFloat()).use { recognizer ->
                    recognizer.acceptWaveForm(chunk.pcm16, chunk.pcm16.size)
                    parse(recognizer.finalResult, chunk)
                }
            }.fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = { t ->
                    logger.e(TAG, "vosk transcribe failed", t)
                    AppResult.Failure(AppError.Unknown(t.message ?: "Vosk failed", t))
                },
            )
        }

    private fun parse(rawJson: String, chunk: AudioChunk): TranscriptChunk {
        val text = Json.parseToJsonElement(rawJson)
            .jsonObject["text"]
            ?.jsonPrimitive
            ?.content
            .orEmpty()
            .trim()
        return TranscriptChunk(
            text = text,
            startTimestampMs = chunk.startTimestampMs,
            durationMs = chunk.durationMs,
            confidence = if (text.isEmpty()) 0f else 1f,
        )
    }

    private companion object {
        const val TAG = "VoskSpeechTranscriber"
    }
}
