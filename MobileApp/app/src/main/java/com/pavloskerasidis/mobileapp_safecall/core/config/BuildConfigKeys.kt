package com.pavloskerasidis.mobileapp_safecall.core.config

import com.pavloskerasidis.mobileapp_safecall.BuildConfig

interface ApiKeyProvider {
    val claudeApiKey: String
    val claudeModel: String
    val voskModelUrl: String
    val uploadEndpoint: String
    val uploadApiKey: String
}

class BuildConfigApiKeyProvider : ApiKeyProvider {
    override val claudeApiKey: String = BuildConfig.CLAUDE_API_KEY
    override val claudeModel: String = BuildConfig.CLAUDE_MODEL
    override val voskModelUrl: String = BuildConfig.VOSK_MODEL_URL
    override val uploadEndpoint: String = BuildConfig.UPLOAD_ENDPOINT
    override val uploadApiKey: String = BuildConfig.UPLOAD_API_KEY
}
