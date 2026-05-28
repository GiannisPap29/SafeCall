package com.pavloskerasidis.mobileapp_safecall.domain.usecase

import com.pavloskerasidis.mobileapp_safecall.domain.repository.CallBlocklistRepository

class ScreenIncomingCallUseCase(
    private val blocklist: CallBlocklistRepository,
) {
    sealed interface Decision {
        data object Allow : Decision
        data object Reject : Decision
    }

    suspend operator fun invoke(number: String?): Decision {
        if (number.isNullOrBlank()) return Decision.Allow
        return if (blocklist.isBlocked(number)) Decision.Reject else Decision.Allow
    }
}
