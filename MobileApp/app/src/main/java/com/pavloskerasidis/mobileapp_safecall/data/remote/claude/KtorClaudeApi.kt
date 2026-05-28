package com.pavloskerasidis.mobileapp_safecall.data.remote.claude

import com.pavloskerasidis.mobileapp_safecall.core.config.ApiKeyProvider
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeContentBlock
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeMessageRequest
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeMessageResponse
import io.ktor.client.HttpClient

class KtorClaudeApi(
    private val client: HttpClient,
    private val keys: ApiKeyProvider,
) : ClaudeApi {
    override suspend fun createMessage(request: ClaudeMessageRequest): ClaudeMessageResponse {
        // TODO: POST https://api.anthropic.com/v1/messages with x-api-key + anthropic-version headers.
        return ClaudeMessageResponse(
            id = "stub",
            model = request.model,
            content = listOf(ClaudeContentBlock(type = "text", text = "{}")),
            stopReason = "end_turn",
        )
    }
}
