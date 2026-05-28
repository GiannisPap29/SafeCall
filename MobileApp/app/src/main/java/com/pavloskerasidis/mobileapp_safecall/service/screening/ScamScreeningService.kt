package com.pavloskerasidis.mobileapp_safecall.service.screening

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService
import com.pavloskerasidis.mobileapp_safecall.R
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.ScreenIncomingCallUseCase
import com.pavloskerasidis.mobileapp_safecall.service.capture.CaptureLauncherActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject

class ScamScreeningService : CallScreeningService() {

    private val screenIncomingCall: ScreenIncomingCallUseCase by inject()
    private val logger: Logger by inject()

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart
        logger.i(TAG, "onScreenCall number=$number")

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
                postArmNotification()
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

    /**
     * Posts a heads-up notification asking the user to tap-to-arm SafeCall for this call.
     * Tapping is treated by Android as a user-initiated foreground action, which lifts the
     * background mic-access restriction that otherwise feeds us silent buffers.
     */
    private fun postArmNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                ARM_CHANNEL_ID,
                "SafeCall arm",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { setSound(null, null) } // phone is already ringing
        )

        val tapIntent = Intent(this, CaptureLauncherActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val tapPending = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = Notification.Builder(this, ARM_CHANNEL_ID)
            .setContentTitle(getString(R.string.arm_notification_title))
            .setContentText(getString(R.string.arm_notification_text))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setCategory(Notification.CATEGORY_CALL)
            .setContentIntent(tapPending)
            .setAutoCancel(true)
            .setTimeoutAfter(NOTIF_TIMEOUT_MS)
            .build()

        manager.notify(ARM_NOTIFICATION_ID, notification)
        logger.i(TAG, "arm notification posted")
    }

    companion object {
        const val ARM_NOTIFICATION_ID = 2001
        private const val ARM_CHANNEL_ID = "safecall_arm"
        private const val TAG = "ScamScreeningService"
        private const val BLOCKLIST_TIMEOUT_MS = 500L
        private const val NOTIF_TIMEOUT_MS = 60_000L
    }
}
