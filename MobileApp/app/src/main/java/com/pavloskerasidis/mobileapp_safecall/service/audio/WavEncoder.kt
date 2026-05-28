package com.pavloskerasidis.mobileapp_safecall.service.audio

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Writes a PCM16 byte stream to a 16-bit mono RIFF/WAVE file.
 * Standard 44-byte header; little-endian; one channel.
 */
class WavEncoder {

    fun encode(pcm16: ByteArray, sampleRateHz: Int, target: File) {
        val header = buildHeader(pcm16.size, sampleRateHz)
        target.outputStream().use { out ->
            out.write(header)
            out.write(pcm16)
        }
    }

    private fun buildHeader(pcmBytes: Int, sampleRateHz: Int): ByteArray {
        val totalDataLen = pcmBytes + 36
        val byteRate = sampleRateHz * CHANNELS * BITS_PER_SAMPLE / 8
        val blockAlign = CHANNELS * BITS_PER_SAMPLE / 8

        return ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(totalDataLen)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16)                       // PCM subchunk size
            putShort(1)                      // PCM format
            putShort(CHANNELS.toShort())
            putInt(sampleRateHz)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(BITS_PER_SAMPLE.toShort())
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(pcmBytes)
        }.array()
    }

    private companion object {
        const val CHANNELS = 1
        const val BITS_PER_SAMPLE = 16
    }
}
