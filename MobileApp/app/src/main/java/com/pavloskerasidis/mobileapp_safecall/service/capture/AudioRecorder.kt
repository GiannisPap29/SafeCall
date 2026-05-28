package com.pavloskerasidis.mobileapp_safecall.service.capture

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

/**
 * Opens an [AudioRecord] on VOICE_COMMUNICATION and emits raw PCM16 chunks as they arrive.
 *
 * NOTE: Android prevents third-party apps from capturing the far-end (modem) audio of a cellular
 * call. What we actually get is the local microphone, which in a loudspeaker call still picks up
 * the remote speaker acoustically. Good enough for an MVP scam classifier.
 */
class AudioRecorder(
    private val logger: Logger,
) {
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun pcmStream(sampleRateHz: Int, bytesPerRead: Int): Flow<ByteArray> = flow {
        val minBuffer = AudioRecord.getMinBufferSize(sampleRateHz, CHANNEL_CONFIG, ENCODING)
        check(minBuffer != AudioRecord.ERROR && minBuffer != AudioRecord.ERROR_BAD_VALUE) {
            "AudioRecord min buffer size unavailable"
        }
        val bufferSize = maxOf(minBuffer, bytesPerRead)

        val record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRateHz,
            CHANNEL_CONFIG,
            ENCODING,
            bufferSize,
        )
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            error("AudioRecord failed to initialize")
        }

        record.startRecording()
        logger.i(TAG, "recording @ ${sampleRateHz}Hz buffer=$bufferSize")
        val buffer = ByteArray(bytesPerRead)
        try {
            while (currentCoroutineContext().isActive) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) emit(buffer.copyOf(read))
            }
        } finally {
            record.stop()
            record.release()
            logger.i(TAG, "recording stopped")
        }
    }

    private companion object {
        const val TAG = "AudioRecorder"
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }
}
