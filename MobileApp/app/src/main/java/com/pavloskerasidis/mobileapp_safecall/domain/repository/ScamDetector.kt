package com.pavloskerasidis.mobileapp_safecall.domain.repository

import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk

/** Classifies a rolling transcript window as Safe / Suspicious / Scam. Single responsibility: scam classification. */
interface ScamDetector {
    suspend fun classify(rollingTranscript: List<TranscriptChunk>): AppResult<ScamVerdict>
}
