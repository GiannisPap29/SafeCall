package com.pavloskerasidis.mobileapp_safecall.core.config

import com.pavloskerasidis.mobileapp_safecall.BuildConfig

interface ApiKeyProvider {
    val claudeApiKey: String
    val googleSttApiKey: String
    val claudeModel: String
}

class BuildConfigApiKeyProvider : ApiKeyProvider {
    override val claudeApiKey: String = BuildConfig.CLAUDE_API_KEY
    override val googleSttApiKey: String = BuildConfig.GOOGLE_STT_API_KEY
    override val claudeModel: String = BuildConfig.CLAUDE_MODEL
}
