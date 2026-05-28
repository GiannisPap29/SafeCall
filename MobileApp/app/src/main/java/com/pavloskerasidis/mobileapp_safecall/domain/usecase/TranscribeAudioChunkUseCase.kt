package com.pavloskerasidis.mobileapp_safecall.domain.usecase

import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.model.AudioChunk
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechTranscriber

class TranscribeAudioChunkUseCase(
    private val transcriber: SpeechTranscriber,
) {
    suspend operator fun invoke(chunk: AudioChunk): AppResult<TranscriptChunk> =
        transcriber.transcribe(chunk)
}
