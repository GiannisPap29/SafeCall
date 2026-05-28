package com.pavloskerasidis.mobileapp_safecall.domain.usecase

import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import com.pavloskerasidis.mobileapp_safecall.domain.repository.ScamDetector

class AnalyzeLiveTranscriptUseCase(
    private val detector: ScamDetector,
) {
    suspend operator fun invoke(window: List<TranscriptChunk>): AppResult<ScamVerdict> =
        detector.classify(window)
}
