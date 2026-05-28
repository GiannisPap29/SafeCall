package com.pavloskerasidis.mobileapp_safecall.data.local.screening

import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import com.pavloskerasidis.mobileapp_safecall.domain.repository.ScamDetector

/**
 * Cheap, offline first-line detector. Flags obvious scam phrases by substring match.
 * Used as a fast path before falling through to the AI detector.
 */
class KeywordScamDetector : ScamDetector {

    override suspend fun classify(rollingTranscript: List<TranscriptChunk>): AppResult<ScamVerdict> {
        val text = rollingTranscript.joinToString(separator = " ") { it.text }.lowercase()
        if (text.isBlank()) return AppResult.Success(ScamVerdict.Safe(0f))

        SCAM_KEYWORDS.firstOrNull { it in text }?.let { hit ->
            return AppResult.Success(
                ScamVerdict.Scam(confidence = 1f, reason = "Matched: \"$hit\"")
            )
        }
        SUSPICIOUS_KEYWORDS.firstOrNull { it in text }?.let { hit ->
            return AppResult.Success(
                ScamVerdict.Suspicious(confidence = 0.7f, reason = "Matched: \"$hit\"")
            )
        }
        return AppResult.Success(ScamVerdict.Safe(0f))
    }

    private companion object {
        val SCAM_KEYWORDS = listOf(
            // English — hard scam indicators
            "card number", "credit card", "social security", "ssn",
            "verification code", "one time password", "one-time password", "otp",
            "gift card", "wire transfer", "western union", "bitcoin", "crypto",
            "bank password", "your password",
            // Greek
            "αριθμό κάρτας", "κωδικό κάρτας", "κωδικός μιας χρήσης",
        )

        val SUSPICIOUS_KEYWORDS = listOf(
            // English — pressure / authority
            "urgent", "act now", "immediately", "your account has been",
            "frozen", "compromised", "warrant for your arrest", "warrant",
            "from the police", "tax office", "irs",
            // Greek
            "επείγον", "άμεσα", "αστυνομία", "ένταλμα", "εφορία",
        )
    }
}