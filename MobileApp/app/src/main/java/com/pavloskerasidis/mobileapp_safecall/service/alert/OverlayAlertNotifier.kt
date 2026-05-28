package com.pavloskerasidis.mobileapp_safecall.service.alert

import android.content.Context
import android.content.Intent
import com.pavloskerasidis.mobileapp_safecall.domain.model.ScamVerdict
import com.pavloskerasidis.mobileapp_safecall.domain.repository.AlertNotifier

class OverlayAlertNotifier(
    private val context: Context,
) : AlertNotifier {

    override suspend fun raise(verdict: ScamVerdict) {
        val (severity, reason) = when (verdict) {
            is ScamVerdict.Safe -> return
            is ScamVerdict.Suspicious -> OverlayAlertService.SEVERITY_SUSPICIOUS to verdict.reason
            is ScamVerdict.Scam -> OverlayAlertService.SEVERITY_SCAM to verdict.reason
        }
        context.startService(
            Intent(context, OverlayAlertService::class.java)
                .putExtra(OverlayAlertService.EXTRA_SEVERITY, severity)
                .putExtra(OverlayAlertService.EXTRA_REASON, reason)
        )
    }

    override suspend fun dismiss() {
        context.stopService(Intent(context, OverlayAlertService::class.java))
    }
}
