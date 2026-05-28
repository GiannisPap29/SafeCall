package com.pavloskerasidis.mobileapp_safecall.data.remote.upload

import com.pavloskerasidis.mobileapp_safecall.core.config.ApiKeyProvider
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.core.result.AppError
import com.pavloskerasidis.mobileapp_safecall.core.result.AppResult
import com.pavloskerasidis.mobileapp_safecall.domain.repository.ChunkMetadata
import com.pavloskerasidis.mobileapp_safecall.domain.repository.ChunkUploader
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.io.File

class KtorChunkUploader(
    private val client: HttpClient,
    private val keys: ApiKeyProvider,
    private val logger: Logger,
) : ChunkUploader {

    override suspend fun upload(file: File, metadata: ChunkMetadata): AppResult<Unit> {
        val endpoint = keys.uploadEndpoint
        if (endpoint.isBlank()) {
            return AppResult.Failure(AppError.Network("upload endpoint not configured"))
        }
        return runCatching {
            client.post(endpoint) {
                if (keys.uploadApiKey.isNotBlank()) {
                    header("Authorization", "Bearer ${keys.uploadApiKey}")
                }
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("call_id", metadata.callId)
                            append("sequence", metadata.sequence.toString())
                            append("start_ms", metadata.startTimestampMs.toString())
                            append("duration_ms", metadata.durationMs.toString())
                            append("sample_rate_hz", metadata.sampleRateHz.toString())
                            append(
                                key = "audio",
                                value = file.readBytes(),
                                headers = Headers.build {
                                    append(HttpHeaders.ContentType, "audio/wav")
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"${file.name}\"",
                                    )
                                },
                            )
                        },
                    ),
                )
            }
            logger.i(TAG, "uploaded ${file.name} (seq=${metadata.sequence})")
            AppResult.Success(Unit)
        }.getOrElse { t ->
            logger.w(TAG, "upload failed for ${file.name}", t)
            AppResult.Failure(AppError.Network("upload failed: ${t.message}", t))
        }
    }

    private companion object {
        const val TAG = "KtorChunkUploader"
    }
}
