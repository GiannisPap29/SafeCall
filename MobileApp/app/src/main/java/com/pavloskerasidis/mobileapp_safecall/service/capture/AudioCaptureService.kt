package com.pavloskerasidis.mobileapp_safecall.service.capture

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import com.pavloskerasidis.mobileapp_safecall.R
import com.pavloskerasidis.mobileapp_safecall.core.dispatchers.AppDispatchers
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.RaiseScamAlertUseCase
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Demo / presentation mode: no real audio capture.
 * Every 5 seconds we log a fake transcript chunk; at the 15-second mark we hard-fire
 * a Scam verdict (overlay) AND terminate the call via TelecomManager.endCall().
 */
class AudioCaptureService : Service() {

    private val dispatchers: AppDispatchers by inject()
    private val logger: Logger by inject()
    private val callStateMonitor: CallStateMonitor by inject()
    private val raiseAlert: RaiseScamAlertUseCase by inject()

    private val scope by lazy { CoroutineScope(SupervisorJob() + dispatchers.io) }
    private val telephonyExecutor by lazy { Executors.newSingleThreadExecutor() }
    private var captureJob: Job? = null
    private var seenActive: Boolean = false

    override fun onCreate() {
        super.onCreate()
        logger.i(TAG, "onCreate (demo mode)")
        startInForeground()
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            logger.w(TAG, "missing READ_PHONE_STATE; stopping")
            stopSelf()
            return
        }
        startMonitoring()
        scope.launch { observeCallState() }
        logger.i(TAG, "monitor registered, waiting for OFFHOOK")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        captureJob?.cancel()
        callStateMonitor.stop()
        telephonyExecutor.shutdownNow()
        scope.cancel()
        super.onDestroy()
    }

    @SuppressLint("MissingPermission") // checked in onCreate before starting
    private fun startMonitoring() {
        callStateMonitor.start(telephonyExecutor)
    }

    private suspend fun observeCallState() {
        callStateMonitor.state.collect { state ->
            logger.i(TAG, "call state → $state")
            when (state) {
                CallStateMonitor.State.Active -> {
                    if (captureJob == null) {
                        seenActive = true
                        logger.i(TAG, "starting demo pipeline")
                        captureJob = scope.launch { runDemoPipeline() }
                    }
                }
                CallStateMonitor.State.Idle -> {
                    // Ignore Idle while the demo is still running — some devices report a
                    // transient Idle mid-call which would otherwise kill the presentation.
                    if (seenActive && captureJob?.isActive != true) {
                        logger.i(TAG, "call ended (demo finished) → tearing down")
                        stopSelf()
                    } else if (seenActive) {
                        logger.i(TAG, "Idle reported but demo in progress — ignoring")
                    }
                }
                CallStateMonitor.State.Ringing -> Unit
            }
        }
    }

    /**
     * Drives the demo. Each tick = a fake "chunk" of audio analysis.
     * Tick 3 (15s in) is the scripted scam moment — alert overlay fires and the call
     * is terminated. After a short grace period we stop the service ourselves.
     */
    private suspend fun runDemoPipeline() {
        for (tick in 1..SCAM_TICK) {
            delay(CHUNK_INTERVAL_MS)
            val elapsedSec = tick * CHUNK_INTERVAL_MS / 1000
            val script = MOCK_TRANSCRIPTS[(tick - 1).coerceAtMost(MOCK_TRANSCRIPTS.lastIndex)]
            logger.i(TAG, "mock chunk #$tick (t=${elapsedSec}s) → \"$script\"")

            if (tick == SCAM_TICK) {
                val verdict = ScamVerdict.Scam(
                    confidence = 0.94f,
                    reason = "Caller asked for OTP code and bank credentials.",
                )
                logger.i(TAG, "DEMO: raising scam alert @ ${elapsedSec}s")
                raiseAlert(verdict)
                endActiveCall()
            }
        }
        delay(POST_ALERT_GRACE_MS)
        logger.i(TAG, "demo complete → stopping service")
        stopSelf()
    }

    private fun endActiveCall() {
        if (!hasPermission(Manifest.permission.ANSWER_PHONE_CALLS)) {
            logger.w(TAG, "ANSWER_PHONE_CALLS not granted — alert shown but call not ended")
            return
        }
        runCatching {
            val tm = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            @SuppressLint("MissingPermission")
            val ok = tm.endCall()
            logger.i(TAG, "TelecomManager.endCall() returned $ok")
        }.onFailure { logger.w(TAG, "endCall() threw", it) }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun startInForeground() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "SafeCall capture", NotificationManager.IMPORTANCE_LOW)
        )
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.capture_notification_title))
            .setContentText(getString(R.string.capture_notification_text))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL,
            )
        } else {
            @Suppress("DEPRECATION")
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private companion object {
        const val TAG = "AudioCaptureService"
        const val CHANNEL_ID = "safecall_capture"
        const val NOTIFICATION_ID = 1001

        const val CHUNK_INTERVAL_MS = 5_000L
        const val SCAM_TICK = 3 // 3 × 5s = 15s
        const val POST_ALERT_GRACE_MS = 3_000L

        val MOCK_TRANSCRIPTS = listOf(
            "Hello, this is the security department calling.",
            "We've detected unusual activity on your bank account.",
            "Please confirm your one-time password to verify your identity.",
        )
    }
}