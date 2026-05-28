package com.pavloskerasidis.mobileapp_safecall.domain.analysis

import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import org.junit.Assert.assertEquals
import org.junit.Test

class RollingTranscriptWindowTest {

    private fun chunk(text: String) =
        TranscriptChunk(text = text, startTimestampMs = 0, durationMs = 0, confidence = 1f)

    @Test
    fun `pushes within capacity`() {
        val w = RollingTranscriptWindow(capacity = 3)
        w.push(chunk("a"))
        w.push(chunk("b"))
        val snap = w.push(chunk("c"))
        assertEquals(listOf("a", "b", "c"), snap.map { it.text })
    }

    @Test
    fun `evicts oldest beyond capacity`() {
        val w = RollingTranscriptWindow(capacity = 2)
        w.push(chunk("a"))
        w.push(chunk("b"))
        val snap = w.push(chunk("c"))
        assertEquals(listOf("b", "c"), snap.map { it.text })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects non-positive capacity`() {
        RollingTranscriptWindow(capacity = 0)
    }
}
