package com.pavloskerasidis.mobileapp_safecall.data.remote.claude

import com.pavloskerasidis.mobileapp_safecall.core.config.ApiKeyProvider
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.core.result.AppError
import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeMessage
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeMessageRequest
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeMessageResponse
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeVerdictDto
import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import com.pavloskerasidis.mobileapp_safecall.domain.repository.ScamDetector
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

class ClaudeScamDetector(
    private val api: ClaudeApi,
    private val keys: ApiKeyProvider,
    private val json: Json,
    private val logger: Logger,
) : ScamDetector {

    override suspend fun classify(rollingTranscript: List<TranscriptChunk>): AppResult<ScamVerdict> {
        if (rollingTranscript.isEmpty()) {
            return AppResult.Success(ScamVerdict.Safe(confidence = 0f))
        }

        val request = ClaudeMessageRequest(
            model = keys.claudeModel,
            maxTokens = MAX_TOKENS,
            system = SYSTEM_PROMPT,
            messages = listOf(
                ClaudeMessage(role = "user", content = formatTranscript(rollingTranscript))
            ),
        )

        return try {
            AppResult.Success(parseVerdict(api.createMessage(request)))
        } catch (e: ResponseException) {
            AppResult.Failure(e.toAppError())
        } catch (t: Throwable) {
            AppResult.Failure(AppError.Network(t.message ?: "Claude request failed", t))
        }
    }

    private fun formatTranscript(chunks: List<TranscriptChunk>): String =
        chunks.joinToString(separator = " ") { it.text }.trim()

    private fun parseVerdict(response: ClaudeMessageResponse): ScamVerdict {
        val text = response.content.firstOrNull { it.type == "text" }?.text?.trim().orEmpty()
        if (text.isEmpty()) return ScamVerdict.Safe(confidence = 0f)

        return runCatching {
            val dto = json.decodeFromString<ClaudeVerdictDto>(extractJsonObject(text))
            when (dto.verdict.lowercase()) {
                "scam" -> ScamVerdict.Scam(dto.confidence, dto.reason)
                "suspicious" -> ScamVerdict.Suspicious(dto.confidence, dto.reason)
                else -> ScamVerdict.Safe(dto.confidence)
            }
        }.getOrElse { t ->
            logger.w(TAG, "unparseable Claude verdict: $text", t)
            ScamVerdict.Safe(confidence = 0f)
        }
    }

    private fun extractJsonObject(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start in 0 until end) text.substring(start, end + 1) else text
    }

    private fun ResponseException.toAppError(): AppError {
        val status = response.status
        val msg = message ?: "Claude request failed (${status.value})"
        return when (status) {
            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> AppError.Auth(msg)
            else -> AppError.Network(msg, this)
        }
    }

    private companion object {
        const val TAG = "ClaudeScamDetector"
        const val MAX_TOKENS = 128
        const val SYSTEM_PROMPT =
            "You are a scam-detection assistant analysing a live phone-call transcript. " +
                "Respond ONLY with JSON in this exact shape, no prose: " +
                "{\"verdict\":\"safe|suspicious|scam\",\"confidence\":0.0-1.0,\"reason\":\"short reason\"}. " +
                "Flag pressure tactics, urgency, requests for money / cards / passwords / 2FA codes, " +
                "family-impersonation, fake authority (bank, police, tax office), and unnatural / scripted speech."
    }
}
