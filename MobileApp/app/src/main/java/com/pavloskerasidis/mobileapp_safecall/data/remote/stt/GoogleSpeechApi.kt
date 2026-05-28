package com.pavloskerasidis.mobileapp_safecall.data.remote.stt

import com.pavloskerasidis.mobileapp_safecall.domain.model.AudioChunk
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk

/** Thin transport over Google Cloud Speech-to-Text v2. Implementations own HTTP + base64 audio encoding. */
interface GoogleSpeechApi {
    suspend fun recognize(chunk: AudioChunk): TranscriptChunk
}
