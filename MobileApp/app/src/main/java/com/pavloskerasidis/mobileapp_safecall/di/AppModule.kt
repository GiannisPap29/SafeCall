package com.pavloskerasidis.mobileapp_safecall.di

import com.pavloskerasidis.mobileapp_safecall.core.config.ApiKeyProvider
import com.pavloskerasidis.mobileapp_safecall.core.config.BuildConfigApiKeyProvider
import com.pavloskerasidis.mobileapp_safecall.core.dispatchers.AppDispatchers
import com.pavloskerasidis.mobileapp_safecall.core.dispatchers.DefaultAppDispatchers
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.core.logging.TimberLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import timber.log.Timber
import io.ktor.client.plugins.logging.Logger as KtorLogger

val appModule = module {
    single<AppDispatchers> { DefaultAppDispatchers() }
    single<Logger> { TimberLogger() }
    single<ApiKeyProvider> { BuildConfigApiKeyProvider() }

    single { Json { ignoreUnknownKeys = true; isLenient = true } }

    single {
        HttpClient(OkHttp) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(get<Json>())
            }
            install(Logging) {
                logger = object : KtorLogger {
                    override fun log(message: String) {
                        Timber.tag("API").d(message)
                    }
                }
                level = LogLevel.INFO
            }
        }
    }
}
