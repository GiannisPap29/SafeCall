package com.pavloskerasidis.mobileapp_safecall.service.alert

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import org.koin.android.ext.android.inject

class OverlayAlertService : Service() {

    private val logger: Logger by inject()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reason = intent?.getStringExtra(EXTRA_REASON).orEmpty()
        logger.i(TAG, "Overlay request reason=$reason")
        // TODO: inflate a TYPE_APPLICATION_OVERLAY view via WindowManager.
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_REASON = "reason"
        private const val TAG = "OverlayAlertService"
    }
}
