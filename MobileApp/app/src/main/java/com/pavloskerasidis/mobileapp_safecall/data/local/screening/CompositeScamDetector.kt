package com.pavloskerasidis.mobileapp_safecall.data.local.screening

import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import com.pavloskerasidis.mobileapp_safecall.domain.repository.ScamDetector

/**
 * Tries [fast] first; only falls through to [slow] when fast returns Safe.
 * Lets a cheap offline detector short-circuit before an expensive network call.
 */
class CompositeScamDetector(
    private val fast: ScamDetector,
    private val slow: ScamDetector,
) : ScamDetector {

    override suspend fun classify(rollingTranscript: List<TranscriptChunk>): AppResult<ScamVerdict> {
        val fastResult = fast.classify(rollingTranscript)
        if (fastResult is AppResult.Success && fastResult.value !is ScamVerdict.Safe) {
            return fastResult
        }
        return slow.classify(rollingTranscript)
    }
}