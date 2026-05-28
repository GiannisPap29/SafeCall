package com.pavloskerasidis.mobileapp_safecall.di

import com.pavloskerasidis.mobileapp_safecall.data.local.BlocklistDataStore
import com.pavloskerasidis.mobileapp_safecall.data.local.screening.CompositeScamDetector
import com.pavloskerasidis.mobileapp_safecall.data.local.screening.KeywordScamDetector
import com.pavloskerasidis.mobileapp_safecall.data.local.stt.VoskModelInstaller
import com.pavloskerasidis.mobileapp_safecall.data.local.stt.VoskModelProvider
import com.pavloskerasidis.mobileapp_safecall.data.local.stt.VoskSpeechTranscriber
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.ClaudeApi
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.ClaudeScamDetector
import com.pavloskerasidis.mobileapp_safecall.data.remote.claude.KtorClaudeApi
import com.pavloskerasidis.mobileapp_safecall.data.remote.upload.KtorChunkUploader
import com.pavloskerasidis.mobileapp_safecall.domain.repository.CallBlocklistRepository
import com.pavloskerasidis.mobileapp_safecall.domain.repository.ChunkUploader
import com.pavloskerasidis.mobileapp_safecall.domain.repository.ScamDetector
import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechModelInstaller
import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechTranscriber
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val KEYWORD = named("keyword")
private val AI = named("ai")

val dataModule = module {
    single<ClaudeApi> { KtorClaudeApi(client = get(), keys = get()) }

    single<ScamDetector>(KEYWORD) { KeywordScamDetector() }
    single<ScamDetector>(AI) {
        ClaudeScamDetector(api = get(), keys = get(), json = get(), logger = get())
    }
    single<ScamDetector> {
        CompositeScamDetector(fast = get(KEYWORD), slow = get(AI))
    }

    single { VoskModelProvider(context = androidContext(), logger = get()) }
    single<SpeechTranscriber> {
        VoskSpeechTranscriber(models = get(), dispatchers = get(), logger = get())
    }
    single<SpeechModelInstaller> {
        VoskModelInstaller(
            context = androidContext(),
            httpClient = get(),
            dispatchers = get(),
            keys = get(),
            logger = get(),
        )
    }

    single<CallBlocklistRepository> { BlocklistDataStore(context = androidContext()) }

    single<ChunkUploader> {
        KtorChunkUploader(client = get(), keys = get(), logger = get())
    }
}