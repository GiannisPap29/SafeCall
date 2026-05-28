package com.pavloskerasidis.mobileapp_safecall.data.remote.stt

import com.pavloskerasidis.mobileapp_safecall.core.result.AppError
import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.model.AudioChunk
import com.pavloskerasidis.mobileapp_safecall.domain.model.TranscriptChunk
import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechTranscriber

class GoogleSpeechTranscriber(
    private val api: GoogleSpeechApi,
) : SpeechTranscriber {

    override suspend fun transcribe(chunk: AudioChunk): AppResult<TranscriptChunk> {
        // TODO: forward to api.recognize(chunk), map HTTP failures to AppError.Network.
        return try {
            AppResult.Success(api.recognize(chunk))
        } catch (t: Throwable) {
            AppResult.Failure(AppError.Network(t.message ?: "STT failed", t))
        }
    }
}
