package com.pavloskerasidis.mobileapp_safecall.domain.usecase

import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechModelInstaller

class InstallSpeechModelUseCase(
    private val installer: SpeechModelInstaller,
) {
    suspend operator fun invoke() = installer.ensureInstalled()
}
