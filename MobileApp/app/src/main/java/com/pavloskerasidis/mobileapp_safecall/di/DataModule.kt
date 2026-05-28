package com.pavloskerasidis.mobileapp_safecall.di

import com.pavloskerasidis.mobileapp_safecall.data.local.BlocklistDataStore
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.ClaudeApi
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.ClaudeScamDetector
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.KtorClaudeApi
import com.pavloskerasidis.mobileapp_safecall.data.remote.stt.GoogleSpeechApi
import com.pavloskerasidis.mobileapp_safecall.data.remote.stt.GoogleSpeechTranscriber
import com.pavloskerasidis.mobileapp_safecall.data.remote.stt.KtorGoogleSpeechApi
import com.pavloskerasidis.mobileapp_safecall.domain.repository.CallBlocklistRepository
import com.pavloskerasidis.mobileapp_safecall.domain.repository.ScamDetector
import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechTranscriber
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single<ClaudeApi> { KtorClaudeApi(client = get(), keys = get()) }
    single<ScamDetector> { ClaudeScamDetector(api = get(), keys = get()) }

    single<GoogleSpeechApi> { KtorGoogleSpeechApi(client = get(), keys = get()) }
    single<SpeechTranscriber> { GoogleSpeechTranscriber(api = get()) }

    single<CallBlocklistRepository> { BlocklistDataStore(context = androidContext()) }
}
