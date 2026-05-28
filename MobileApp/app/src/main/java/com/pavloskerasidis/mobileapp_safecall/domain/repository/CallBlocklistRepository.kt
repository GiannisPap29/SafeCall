package com.pavloskerasidis.mobileapp_safecall.domain.repository

/** Persists and queries user-managed blocked numbers. Single responsibility: blocklist state. */
interface CallBlocklistRepository {
    suspend fun isBlocked(number: String): Boolean
    suspend fun block(number: String)
    suspend fun unblock(number: String)
}
