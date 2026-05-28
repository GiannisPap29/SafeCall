package com.pavloskerasidis.mobileapp_safecall.domain.repository

import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict

/** Surfaces a scam verdict to the user (overlay, notification, etc.). Single responsibility: alerting. */
interface AlertNotifier {
    suspend fun raise(verdict: ScamVerdict)
    suspend fun dismiss()
}
