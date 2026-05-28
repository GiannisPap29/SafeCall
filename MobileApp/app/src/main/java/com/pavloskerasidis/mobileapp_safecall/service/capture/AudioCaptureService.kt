package com.pavloskerasidis.mobileapp_safecall.service.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.pavloskerasidis.mobileapp_safecall.R
import com.pavloskerasidis.mobileapp_safecall.core.dispatchers.AppDispatchers
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.AnalyzeLiveTranscriptUseCase
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.RaiseScamAlertUseCase
import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechTranscriber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject

class AudioCaptureService : Service() {

    private val dispatchers: AppDispatchers by inject()
    private val logger: Logger by inject()
    private val transcriber: SpeechTranscriber by inject()
    private val analyze: AnalyzeLiveTranscriptUseCase by inject()
    private val raiseAlert: RaiseScamAlertUseCase by inject()

    private val scope by lazy { CoroutineScope(SupervisorJob() + dispatchers.io) }

    override fun onCreate() {
        super.onCreate()
        startInForeground()
        logger.i(TAG, "AudioCaptureService started")
        // TODO: open AudioRecord(VOICE_COMMUNICATION, 16kHz, MONO, PCM_16BIT),
        // feed bytes through AudioChunker, push chunks via transcriber → analyze → raiseAlert.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        // TODO: release AudioRecord
        super.onDestroy()
    }

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
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
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
    }
}
