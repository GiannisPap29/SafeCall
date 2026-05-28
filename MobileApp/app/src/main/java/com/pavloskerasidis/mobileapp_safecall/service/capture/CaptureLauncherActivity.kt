package com.pavloskerasidis.mobileapp_safecall.service.capture

import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import com.pavloskerasidis.mobileapp_safecall.service.screening.ScamScreeningService
import timber.log.Timber

/**
 * Invisible launcher reached by tapping the arm notification posted on incoming calls.
 * The user-initiated tap puts the app in foreground state, lifting Android's bg-mic
 * restriction so the FGS we start here can actually read real audio.
 */
class CaptureLauncherActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(TAG).i("user-initiated launch → starting FGS")
        startForegroundService(Intent(this, AudioCaptureService::class.java))
        getSystemService(NotificationManager::class.java)
            .cancel(ScamScreeningService.ARM_NOTIFICATION_ID)
        finish()
    }

    private companion object {
        const val TAG = "CaptureLauncher"
    }
}
