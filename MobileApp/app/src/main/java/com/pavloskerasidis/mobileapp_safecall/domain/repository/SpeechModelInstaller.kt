package com.pavloskerasidis.mobileapp_safecall.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Ensures a speech-recognition model is present on the device. Idempotent — safe to call
 * repeatedly; does nothing if already installed.
 */
interface SpeechModelInstaller {
    val state: StateFlow<InstallState>
    suspend fun ensureInstalled()

    sealed interface InstallState {
        data object NotInstalled : InstallState
        data class Downloading(val progress: Float) : InstallState
        data object Unpacking : InstallState
        data object Installed : InstallState
        data class Failed(val message: String) : InstallState
    }
}
