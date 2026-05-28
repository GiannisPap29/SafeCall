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
import androidx.core.content.ContextCompat
import com.pavloskerasidis.mobileapp_safecall.R
import com.pavloskerasidis.mobileapp_safecall.core.dispatchers.AppDispatchers
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.analysis.RollingTranscriptWindow
import com.pavloskerasidis.mobileapp_safecall.domain.model.AudioChunk
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.AnalyzeLiveTranscriptUseCase
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.RaiseScamAlertUseCase
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.TranscribeAudioChunkUseCase
import com.pavloskerasidis.mobileapp_safecall.service.audio.AudioChunker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.concurrent.Executors

class AudioCaptureService : Service() {

    private val dispatchers: AppDispatchers by inject()
    private val logger: Logger by inject()
    private val recorder: AudioRecorder by inject()
    private val callStateMonitor: CallStateMonitor by inject()
    private val transcribe: TranscribeAudioChunkUseCase by inject()
    private val analyze: AnalyzeLiveTranscriptUseCase by inject()
    private val raiseAlert: RaiseScamAlertUseCase by inject()

    private val scope by lazy { CoroutineScope(SupervisorJob() + dispatchers.io) }
    private val telephonyExecutor by lazy { Executors.newSingleThreadExecutor() }
    private var captureJob: Job? = null
    private var seenActive: Boolean = false

    override fun onCreate() {
        super.onCreate()
        logger.i(TAG, "onCreate")
        startInForeground()
        if (!hasPermission(Manifest.permission.RECORD_AUDIO) ||
            !hasPermission(Manifest.permission.READ_PHONE_STATE)
        ) {
            logger.w(TAG, "missing RECORD_AUDIO or READ_PHONE_STATE; stopping")
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
                        logger.i(TAG, "starting capture pipeline")
                        captureJob = scope.launch { runPipeline() }
                    }
                }
                CallStateMonitor.State.Idle -> {
                    if (seenActive) {
                        logger.i(TAG, "call ended → tearing down")
                        stopSelf()
                    }
                }
                CallStateMonitor.State.Ringing -> Unit
            }
        }
    }

    @SuppressLint("MissingPermission") // checked in onCreate before starting
    private suspend fun runPipeline() {
        val chunker = AudioChunker(SAMPLE_RATE_HZ, CHUNK_DURATION_MS)
        val window = RollingTranscriptWindow(WINDOW_CAPACITY)
        try {
            recorder.pcmStream(SAMPLE_RATE_HZ, READ_BYTES).collect { bytes ->
                val now = System.currentTimeMillis()
                chunker.append(bytes, now).forEach { chunk ->
                    processChunk(chunk, window)
                }
            }
        } catch (t: Throwable) {
            logger.e(TAG, "capture pipeline failed", t)
            stopSelf()
        }
    }

    private suspend fun processChunk(chunk: AudioChunk, window: RollingTranscriptWindow) {
        logger.i(TAG, "chunk @${chunk.startTimestampMs} bytes=${chunk.pcm16.size}")
        val transcript = when (val r = transcribe(chunk)) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> {
                logger.w(TAG, "transcribe failed: ${r.error.message}")
                return
            }
        }
        logger.i(TAG, "transcript: '${transcript.text}' (conf=${transcript.confidence})")
        if (transcript.text.isBlank()) return

        val snapshot = window.push(transcript)
        when (val r = analyze(snapshot)) {
            is AppResult.Success -> {
                logger.i(TAG, "verdict: ${r.value}")
                raiseAlert(r.value)
            }
            is AppResult.Failure -> logger.w(TAG, "analyze failed: ${r.error.message}")
        }
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

        const val SAMPLE_RATE_HZ = 16_000
        const val CHUNK_DURATION_MS = 3_000
        const val READ_BYTES = 8_192
        const val WINDOW_CAPACITY = 10
    }
}
