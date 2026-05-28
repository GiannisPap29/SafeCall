package com.pavloskerasidis.mobileapp_safecall.data.remote.claude

import com.pavloskerasidis.mobileapp_safecall.core.config.ApiKeyProvider
import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import com.pavloskerasidis.mobileapp_safecall.domain.repository.ScamDetector

class ClaudeScamDetector(
    private val api: ClaudeApi,
    private val keys: ApiKeyProvider,
) : ScamDetector {

    override suspend fun classify(rollingTranscript: List<TranscriptChunk>): AppResult<ScamVerdict> {
        // TODO: build ClaudeMessageRequest with SYSTEM_PROMPT + rolling transcript,
        // call api.createMessage(...), parse the JSON verdict the model returns,
        // map to ScamVerdict.{Safe, Suspicious, Scam}.
        return AppResult.Success(ScamVerdict.Safe(confidence = 0f))
    }

    private companion object {
        const val SYSTEM_PROMPT = "You are a scam-detection assistant analysing a live phone-call transcript. " +
            "Respond ONLY in JSON: {\"verdict\":\"safe|suspicious|scam\",\"confidence\":0.0-1.0,\"reason\":\"...\"}. " +
            "Flag pressure tactics, urgency, money/credentials requests, family-impersonation, voice manipulation."
    }
}
