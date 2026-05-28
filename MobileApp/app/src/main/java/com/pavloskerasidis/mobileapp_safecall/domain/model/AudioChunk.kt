package com.pavloskerasidis.mobileapp_safecall.domain.model

data class AudioChunk(
    val pcm16: ByteArray,
    val sampleRateHz: Int,
    val startTimestampMs: Long,
    val durationMs: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioChunk) return false
        return sampleRateHz == other.sampleRateHz &&
            startTimestampMs == other.startTimestampMs &&
            durationMs == other.durationMs &&
            pcm16.contentEquals(other.pcm16)
    }

    override fun hashCode(): Int {
        var result = pcm16.contentHashCode()
        result = 31 * result + sampleRateHz
        result = 31 * result + startTimestampMs.hashCode()
        result = 31 * result + durationMs
        return result
    }
}
