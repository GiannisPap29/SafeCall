package com.pavloskerasidis.mobileapp_safecall.di

import com.pavloskerasidis.mobileapp_safecall.domain.usecase.AnalyzeLiveTranscriptUseCase
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.RaiseScamAlertUseCase
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.ScreenIncomingCallUseCase
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.TranscribeAudioChunkUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { ScreenIncomingCallUseCase(blocklist = get()) }
    factory { TranscribeAudioChunkUseCase(transcriber = get()) }
    factory { AnalyzeLiveTranscriptUseCase(detector = get()) }
    factory { RaiseScamAlertUseCase(notifier = get()) }
}
