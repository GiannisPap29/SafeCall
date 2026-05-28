package com.pavloskerasidis.mobileapp_safecall.service.screening

import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService
import com.pavloskerasidis.mobileapp_safecall.core.dispatchers.AppDispatchers
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.ScreenIncomingCallUseCase
import com.pavloskerasidis.mobileapp_safecall.service.capture.AudioCaptureService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ScamScreeningService : CallScreeningService() {

    private val screenIncomingCall: ScreenIncomingCallUseCase by inject()
    private val dispatchers: AppDispatchers by inject()
    private val logger: Logger by inject()

    private val scope by lazy { CoroutineScope(SupervisorJob() + dispatchers.default) }

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart
        logger.i(TAG, "onScreenCall number=$number")

        scope.launch {
            when (screenIncomingCall(number)) {
                ScreenIncomingCallUseCase.Decision.Reject -> rejectCall(callDetails)
                ScreenIncomingCallUseCase.Decision.Allow -> {
                    allowCall(callDetails)
                    startCapture()
                }
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
        // TODO: only start when the call actually connects; for now fire-and-forget the foreground service.
        startForegroundService(Intent(this, AudioCaptureService::class.java))
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "ScamScreeningService"
    }
}
