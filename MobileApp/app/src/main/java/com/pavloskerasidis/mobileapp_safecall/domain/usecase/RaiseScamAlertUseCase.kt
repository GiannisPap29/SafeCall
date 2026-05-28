package com.pavloskerasidis.mobileapp_safecall.domain.usecase

import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict
import com.pavloskerasidis.mobileapp_safecall.domain.repository.AlertNotifier

class RaiseScamAlertUseCase(
    private val notifier: AlertNotifier,
) {
    suspend operator fun invoke(verdict: ScamVerdict) {
        when (verdict) {
            is ScamVerdict.Safe -> notifier.dismiss()
            is ScamVerdict.Suspicious, is ScamVerdict.Scam -> notifier.raise(verdict)
        }
    }
}
