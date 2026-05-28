package com.pavloskerasidis.mobileapp_safecall.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pavloskerasidis.mobileapp_safecall.domain.repository.CallBlocklistRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.blocklistDataStore by preferencesDataStore(name = "safecall_blocklist")
private val BLOCKED_NUMBERS = stringSetPreferencesKey("blocked_numbers")

class BlocklistDataStore(
    private val context: Context,
) : CallBlocklistRepository {

    override suspend fun isBlocked(number: String): Boolean =
        context.blocklistDataStore.data
            .map { it[BLOCKED_NUMBERS].orEmpty() }
            .first()
            .contains(number)

    override suspend fun block(number: String) {
        context.blocklistDataStore.edit { prefs ->
            prefs[BLOCKED_NUMBERS] = prefs[BLOCKED_NUMBERS].orEmpty() + number
        }
    }

    override suspend fun unblock(number: String) {
        context.blocklistDataStore.edit { prefs ->
            prefs[BLOCKED_NUMBERS] = prefs[BLOCKED_NUMBERS].orEmpty() - number
        }
    }
}
