package com.pavloskerasidis.mobileapp_safecall.data.remote.claude

import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeMessageRequest
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.dto.ClaudeMessageResponse

/** Thin transport over the Anthropic Messages API. Implementations own HTTP, auth headers, and parsing. */
interface ClaudeApi {
    suspend fun createMessage(request: ClaudeMessageRequest): ClaudeMessageResponse
}
