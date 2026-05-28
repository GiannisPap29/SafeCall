package com.pavloskerasidis.mobileapp_safecall.data.local.stt

import android.content.Context
import com.pavloskerasidis.mobileapp_safecall.core.config.ApiKeyProvider
import com.pavloskerasidis.mobileapp_safecall.core.dispatchers.AppDispatchers
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechModelInstaller
import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechModelInstaller.InstallState
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipInputStream

class VoskModelInstaller(
    private val context: Context,
    private val httpClient: HttpClient,
    private val dispatchers: AppDispatchers,
    private val keys: ApiKeyProvider,
    private val logger: Logger,
) : SpeechModelInstaller {

    private val _state = MutableStateFlow<InstallState>(initialState())
    override val state: StateFlow<InstallState> = _state.asStateFlow()

    private val modelDir: File get() = File(context.filesDir, MODEL_DIR)
    private val zipFile: File get() = File(context.filesDir, ZIP_NAME)

    override suspend fun ensureInstalled() {
        if (isAlreadyInstalled()) {
            _state.value = InstallState.Installed
            return
        }
        withContext(dispatchers.io) {
            runCatching {
                download()
                unpack()
                zipFile.delete()
            }.fold(
                onSuccess = { _state.value = InstallState.Installed },
                onFailure = { t ->
                    logger.e(TAG, "model install failed", t)
                    _state.value = InstallState.Failed(t.message ?: "Install failed")
                },
            )
        }
    }

    private fun initialState(): InstallState =
        if (isAlreadyInstalled()) InstallState.Installed else InstallState.NotInstalled

    private fun isAlreadyInstalled(): Boolean =
        modelDir.exists() && File(modelDir, "am").exists()

    private suspend fun download() {
        logger.i(TAG, "downloading model from ${keys.voskModelUrl}")
        val response = httpClient.get(keys.voskModelUrl)
        val total = response.contentLength() ?: -1L
        val channel: ByteReadChannel = response.bodyAsChannel()

        zipFile.outputStream().use { out ->
            val buffer = ByteArray(BUFFER_BYTES)
            var received = 0L
            while (!channel.isClosedForRead) {
                val read = channel.readAvailable(buffer, 0, buffer.size)
                if (read <= 0) break
                out.write(buffer, 0, read)
                received += read
                val progress = if (total > 0) received.toFloat() / total else 0f
                _state.value = InstallState.Downloading(progress.coerceIn(0f, 1f))
            }
        }
    }

    private fun unpack() {
        _state.value = InstallState.Unpacking
        if (modelDir.exists()) modelDir.deleteRecursively()
        modelDir.mkdirs()

        ZipInputStream(zipFile.inputStream()).use { zis ->
            generateSequence { zis.nextEntry }.forEach { entry ->
                val relative = entry.name.substringAfter('/', missingDelimiterValue = "")
                if (relative.isEmpty()) return@forEach
                val out = File(modelDir, relative)
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile?.mkdirs()
                    out.outputStream().use { zis.copyTo(it) }
                }
            }
        }
        logger.i(TAG, "model unpacked to ${modelDir.absolutePath}")
    }

    private companion object {
        const val TAG = "VoskModelInstaller"
        const val MODEL_DIR = "vosk-model"
        const val ZIP_NAME = "vosk-model.zip"
        const val BUFFER_BYTES = 16_384
    }
}
