package com.pavloskerasidis.mobileapp_safecall.data.remote.claude

import com.pavloskerasidis.mobileapp_safecall.core.config.ApiKeyProvider
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.core.result.AppError
import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeContentBlock
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeMessageRequest
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeMessageResponse
import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ClaudeScamDetectorTest {

    private val api: ClaudeApi = mockk()
    private val keys: ApiKeyProvider = mockk(relaxed = true) {
        coEvery { claudeModel } returns "claude-haiku-4-5"
    }
    private val logger: Logger = mockk(relaxed = true)
    private val detector = ClaudeScamDetector(
        api = api,
        keys = keys,
        json = Json { ignoreUnknownKeys = true; isLenient = true },
        logger = logger,
    )

    private fun chunk(text: String) =
        TranscriptChunk(text = text, startTimestampMs = 0, durationMs = 3_000, confidence = 1f)

    private fun reply(rawText: String) = ClaudeMessageResponse(
        id = "stub",
        model = "claude-haiku-4-5",
        content = listOf(ClaudeContentBlock(type = "text", text = rawText)),
        stopReason = "end_turn",
    )

    @Test
    fun `empty transcript skips API and returns Safe`() = runTest {
        val result = detector.classify(emptyList())
        assertEquals(AppResult.Success(ScamVerdict.Safe(0f)), result)
    }

    @Test
    fun `scam verdict is mapped`() = runTest {
        val captured = slot<ClaudeMessageRequest>()
        coEvery { api.createMessage(capture(captured)) } returns
            reply("""{"verdict":"scam","confidence":0.92,"reason":"asked for card number"}""")

        val result = detector.classify(listOf(chunk("give me your card number")))

        assertEquals(
            AppResult.Success(ScamVerdict.Scam(0.92f, "asked for card number")),
            result,
        )
        assertEquals("claude-haiku-4-5", captured.captured.model)
    }

    @Test
    fun `suspicious verdict is mapped`() = runTest {
        coEvery { api.createMessage(any()) } returns
            reply("""{"verdict":"suspicious","confidence":0.55,"reason":"urgency"}""")
        val result = detector.classify(listOf(chunk("act now")))
        assertEquals(
            AppResult.Success(ScamVerdict.Suspicious(0.55f, "urgency")),
            result,
        )
    }

    @Test
    fun `markdown-fenced JSON is still parsed`() = runTest {
        coEvery { api.createMessage(any()) } returns
            reply("```json\n{\"verdict\":\"safe\",\"confidence\":0.9,\"reason\":\"normal chat\"}\n```")
        val result = detector.classify(listOf(chunk("how are you")))
        assertEquals(AppResult.Success(ScamVerdict.Safe(0.9f)), result)
    }

    @Test
    fun `unparseable response degrades to Safe`() = runTest {
        coEvery { api.createMessage(any()) } returns reply("I think this might be a scam but idk")
        val result = detector.classify(listOf(chunk("anything")))
        assertEquals(AppResult.Success(ScamVerdict.Safe(0f)), result)
    }

    @Test
    fun `generic throwable maps to network error`() = runTest {
        coEvery { api.createMessage(any()) } throws IOException("boom")
        val result = detector.classify(listOf(chunk("anything")))
        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Network)
    }
}
