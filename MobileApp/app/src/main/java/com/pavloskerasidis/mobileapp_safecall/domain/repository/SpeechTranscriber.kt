package com.pavloskerasidis.mobileapp_safecall.domain.repository

import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.model.AudioChunk
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk

/** Converts PCM audio chunks to text. Single responsibility: speech-to-text. */
interface SpeechTranscriber {
    suspend fun transcribe(chunk: AudioChunk): AppResult<TranscriptChunk>
}
