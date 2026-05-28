package com.pavloskerasidis.mobileapp_safecall.domain.analysis

import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk

/**
 * Fixed-capacity sliding window of recent transcript chunks.
 * Owned per-call by whoever drives the analysis pipeline.
 */
class RollingTranscriptWindow(private val capacity: Int) {

    init { require(capacity > 0) { "capacity must be > 0" } }

    private val buffer = ArrayDeque<TranscriptChunk>(capacity)

    fun push(chunk: TranscriptChunk): List<TranscriptChunk> {
        buffer.addLast(chunk)
        while (buffer.size > capacity) buffer.removeFirst()
        return buffer.toList()
    }

    fun snapshot(): List<TranscriptChunk> = buffer.toList()

    fun clear() { buffer.clear() }
}
