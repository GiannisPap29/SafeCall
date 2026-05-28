package com.pavloskerasidis.mobileapp_safecall.service.screening

import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.ScreenIncomingCallUseCase
import com.pavloskerasidis.mobileapp_safecall.service.capture.AudioCaptureService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject

class ScamScreeningService : CallScreeningService() {

    private val screenIncomingCall: ScreenIncomingCallUseCase by inject()
    private val logger: Logger by inject()

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart
        logger.i(TAG, "onScreenCall number=$number")

        // Android only permits foreground-service starts inside this synchronous callback
        // window, so the blocklist check must run inline. DataStore reads are sub-millisecond;
        // we cap with a timeout and fail-open to Allow so a hung lookup never silently drops a call.
        val decision = runCatching {
            runBlocking {
                withTimeoutOrNull(BLOCKLIST_TIMEOUT_MS) { screenIncomingCall(number) }
            }
        }.getOrElse { t ->
            logger.w(TAG, "blocklist check failed; defaulting to Allow", t)
            null
        } ?: ScreenIncomingCallUseCase.Decision.Allow

        when (decision) {
            ScreenIncomingCallUseCase.Decision.Reject -> rejectCall(callDetails)
            ScreenIncomingCallUseCase.Decision.Allow -> {
                startCapture()
                allowCall(callDetails)
            }
        }
    }

    private fun rejectCall(details: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        respondToCall(details, response)
    }

    private fun allowCall(details: Call.Details) {
        respondToCall(details, CallResponse.Builder().build())
    }

    private fun startCapture() {
        startForegroundService(Intent(this, AudioCaptureService::class.java))
    }

    private companion object {
        const val TAG = "ScamScreeningService"
        const val BLOCKLIST_TIMEOUT_MS = 500L
    }
}
