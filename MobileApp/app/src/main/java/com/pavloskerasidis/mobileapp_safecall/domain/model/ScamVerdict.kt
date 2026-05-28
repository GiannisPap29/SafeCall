package com.pavloskerasidis.mobileapp_safecall.domain.model

sealed interface ScamVerdict {
    val confidence: Float

    data class Safe(override val confidence: Float) : ScamVerdict
    data class Suspicious(override val confidence: Float, val reason: String) : ScamVerdict
    data class Scam(override val confidence: Float, val reason: String) : ScamVerdict
}
