package com.pavloskerasidis.mobileapp_safecall.data.local.stt

import android.content.Context
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import org.vosk.Model
import java.io.File

/**
 * Lazily loads the Vosk model from the app's internal storage.
 *
 * The model directory must be present at [filesDir]/vosk-model and contain the standard
 * Vosk layout (am/, conf/, graph/, ivector/, …). Drop a model from
 * https://alphacephei.com/vosk/models there. Greek small model: `vosk-model-small-el-0.7`.
 */
class VoskModelProvider(
    private val context: Context,
    private val logger: Logger,
) {

    @Volatile private var cached: Model? = null

    fun get(): Model {
        cached?.let { return it }
        return synchronized(this) {
            cached ?: load().also { cached = it }
        }
    }

    private fun load(): Model {
        val dir = File(context.filesDir, MODEL_DIR)
        check(dir.exists() && dir.isDirectory) {
            "Vosk model missing at ${dir.absolutePath}. Download a model from " +
                "https://alphacephei.com/vosk/models and unzip its contents into that folder."
        }
        logger.i(TAG, "loading Vosk model from ${dir.absolutePath}")
        return Model(dir.absolutePath)
    }

    private companion object {
        const val TAG = "VoskModelProvider"
        const val MODEL_DIR = "vosk-model"
    }
}
