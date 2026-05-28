package com.pavloskerasidis.mobileapp_safecall.service.audio

import com.pavloskerasidis.mobileapp_safecall.domain.model.AudioChunk

/**
 * Accumulates raw PCM16 samples and emits fixed-duration [AudioChunk]s.
 * Reusable across capture pipelines — not Android-specific.
 */
class AudioChunker(
    private val sampleRateHz: Int,
    private val chunkDurationMs: Int,
) {
    private val bytesPerChunk: Int =
        (sampleRateHz * (chunkDurationMs / 1000.0) * BYTES_PER_SAMPLE).toInt()

    private val buffer = ArrayDeque<Byte>(bytesPerChunk * 2)
    private var chunkStartMs: Long = 0L

    fun append(samples: ByteArray, atTimestampMs: Long): List<AudioChunk> {
        if (buffer.isEmpty()) chunkStartMs = atTimestampMs
        samples.forEach { buffer.addLast(it) }

        val out = mutableListOf<AudioChunk>()
        while (buffer.size >= bytesPerChunk) {
            val payload = ByteArray(bytesPerChunk) { buffer.removeFirst() }
            out += AudioChunk(
                pcm16 = payload,
                sampleRateHz = sampleRateHz,
                startTimestampMs = chunkStartMs,
                durationMs = chunkDurationMs,
            )
            chunkStartMs += chunkDurationMs
        }
        return out
    }

    private companion object {
        const val BYTES_PER_SAMPLE = 2
    }
}
