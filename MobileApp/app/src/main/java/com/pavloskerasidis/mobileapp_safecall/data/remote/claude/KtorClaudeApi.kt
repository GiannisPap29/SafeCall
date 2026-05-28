package com.pavloskerasidis.mobileapp_safecall.data.remote.claude

import com.pavloskerasidis.mobileapp_safecall.core.config.ApiKeyProvider
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeMessageRequest
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeMessageResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorClaudeApi(
    private val client: HttpClient,
    private val keys: ApiKeyProvider,
) : ClaudeApi {

    override suspend fun createMessage(request: ClaudeMessageRequest): ClaudeMessageResponse =
        client.post(ENDPOINT) {
            header("x-api-key", keys.claudeApiKey)
            header("anthropic-version", ANTHROPIC_VERSION)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    private companion object {
        const val ENDPOINT = "https://api.anthropic.com/v1/messages"
        const val ANTHROPIC_VERSION = "2023-06-01"
    }
}
