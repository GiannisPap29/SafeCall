package com.pavloskerasidis.mobileapp_safecall.data.local.screening

import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeywordScamDetectorTest {

    private val detector = KeywordScamDetector()

    private fun chunk(text: String) =
        TranscriptChunk(text, startTimestampMs = 0, durationMs = 3_000, confidence = 1f)

    @Test
    fun `hard keyword flags scam`() = runTest {
        val result = detector.classify(listOf(chunk("please tell me your card number")))
        val verdict = (result as AppResult.Success).value
        assertTrue(verdict is ScamVerdict.Scam)
    }

    @Test
    fun `pressure word flags suspicious`() = runTest {
        val result = detector.classify(listOf(chunk("you must act now to avoid issues")))
        val verdict = (result as AppResult.Success).value
        assertTrue(verdict is ScamVerdict.Suspicious)
    }

    @Test
    fun `clean text is safe`() = runTest {
        val result = detector.classify(listOf(chunk("hi mom how are you doing today")))
        assertEquals(AppResult.Success(ScamVerdict.Safe(0f)), result)
    }

    @Test
    fun `empty input is safe`() = runTest {
        assertEquals(AppResult.Success(ScamVerdict.Safe(0f)), detector.classify(emptyList()))
    }

    @Test
    fun `is case-insensitive`() = runTest {
        val result = detector.classify(listOf(chunk("URGENT — your ACCOUNT HAS BEEN compromised")))
        val verdict = (result as AppResult.Success).value
        assertTrue(verdict is ScamVerdict.Scam || verdict is ScamVerdict.Suspicious)
    }
}