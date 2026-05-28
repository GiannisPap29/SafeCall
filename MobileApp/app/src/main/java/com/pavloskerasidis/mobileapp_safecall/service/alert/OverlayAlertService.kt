package com.pavloskerasidis.mobileapp_safecall.service.alert

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.pavloskerasidis.mobileapp_safecall.R
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import org.koin.android.ext.android.inject

class OverlayAlertService : Service() {

    private val logger: Logger by inject()

    private var windowManager: WindowManager? = null
    private var root: LinearLayout? = null
    private var background: GradientDrawable? = null
    private var titleView: TextView? = null
    private var reasonView: TextView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            logger.w(TAG, "SYSTEM_ALERT_WINDOW not granted; stopping")
            stopSelf()
            return START_NOT_STICKY
        }
        val severity = intent?.getStringExtra(EXTRA_SEVERITY).orEmpty()
        val reason = intent?.getStringExtra(EXTRA_REASON).orEmpty()
        showOrUpdate(severity, reason)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        detachOverlay()
        windowManager = null
        super.onDestroy()
    }

    private fun showOrUpdate(severity: String, reason: String) {
        if (root == null) attachOverlay()

        val (titleRes, color) = when (severity) {
            SEVERITY_SCAM -> R.string.overlay_title_scam to COLOR_SCAM
            SEVERITY_SUSPICIOUS -> R.string.overlay_title_suspicious to COLOR_SUSPICIOUS
            else -> R.string.overlay_title_suspicious to COLOR_SUSPICIOUS
        }
        titleView?.setText(titleRes)
        reasonView?.text = reason.ifBlank { getString(R.string.overlay_default_reason) }
        background?.setColor(color)
    }

    private fun attachOverlay() {
        val view = buildView()
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(48f)
            val side = dp(12f)
            horizontalMargin = side.toFloat() / resources.displayMetrics.widthPixels
        }
        try {
            windowManager?.addView(view, params)
            root = view
        } catch (t: Throwable) {
            logger.e(TAG, "addView failed", t)
            stopSelf()
        }
    }

    private fun detachOverlay() {
        val v = root ?: return
        runCatching { windowManager?.removeView(v) }
            .onFailure { logger.w(TAG, "removeView failed", it) }
        root = null
        background = null
        titleView = null
        reasonView = null
    }

    private fun buildView(): LinearLayout {
        val pad = dp(16f)
        val bg = GradientDrawable().apply {
            cornerRadius = dp(12f).toFloat()
            setColor(COLOR_SUSPICIOUS)
        }
        background = bg

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            background = bg
        }

        val title = TextView(this).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }
        val reason = TextView(this).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.WHITE)
            val gap = dp(8f)
            setPadding(0, gap, 0, gap)
        }
        val dismiss = Button(this).apply {
            text = getString(R.string.overlay_dismiss)
            setOnClickListener {
                endActiveCall()
                stopSelf()
            }
        }

        container.addView(title)
        container.addView(reason)
        container.addView(dismiss)

        titleView = title
        reasonView = reason
        return container
    }

    private fun dp(value: Float): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun endActiveCall() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ANSWER_PHONE_CALLS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            logger.w(TAG, "ANSWER_PHONE_CALLS not granted — dismiss won't end the call")
            return
        }
        runCatching {
            val tm = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            @SuppressLint("MissingPermission")
            val ok = tm.endCall()
            logger.i(TAG, "dismiss: TelecomManager.endCall() returned $ok")
        }.onFailure { logger.w(TAG, "endCall() threw", it) }
    }

    companion object {
        const val EXTRA_REASON = "reason"
        const val EXTRA_SEVERITY = "severity"
        const val SEVERITY_SUSPICIOUS = "suspicious"
        const val SEVERITY_SCAM = "scam"

        private const val TAG = "OverlayAlertService"
        private const val COLOR_SUSPICIOUS = 0xFFEF6C00.toInt()
        private const val COLOR_SCAM = 0xFFB71C1C.toInt()
    }
}
