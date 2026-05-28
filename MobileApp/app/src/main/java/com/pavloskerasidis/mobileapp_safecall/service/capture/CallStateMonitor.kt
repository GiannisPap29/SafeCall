package com.pavloskerasidis.mobileapp_safecall.service.capture

import android.Manifest
import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.pavloskerasidis.mobileapp_safecall.core.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executor

/**
 * Watches `TelephonyManager` call state. Translates the framework's int codes into a
 * three-state domain enum that the capture pipeline can react to.
 */
class CallStateMonitor(
    context: Context,
    private val logger: Logger,
) {

    enum class State { Idle, Ringing, Active }

    private val telephony: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private val _state = MutableStateFlow(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    private var modernCallback: TelephonyCallback? = null
    @Suppress("DEPRECATION")
    private var legacyListener: PhoneStateListener? = null

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun start(executor: Executor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val cb = ModernListener { _state.value = it }
            modernCallback = cb
            telephony.registerTelephonyCallback(executor, cb)
        } else {
            @Suppress("DEPRECATION")
            val listener = object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    _state.value = state.toAppState()
                }
            }
            legacyListener = listener
            @Suppress("DEPRECATION")
            telephony.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
        }
        logger.i(TAG, "monitor started")
    }

    fun stop() {
        modernCallback?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephony.unregisterTelephonyCallback(it)
            }
        }
        legacyListener?.let {
            @Suppress("DEPRECATION")
            telephony.listen(it, PhoneStateListener.LISTEN_NONE)
        }
        modernCallback = null
        legacyListener = null
        logger.i(TAG, "monitor stopped")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private class ModernListener(
        private val onState: (State) -> Unit,
    ) : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            onState(state.toAppState())
        }
    }

    private companion object {
        const val TAG = "CallStateMonitor"

        fun Int.toAppState(): State = when (this) {
            TelephonyManager.CALL_STATE_OFFHOOK -> State.Active
            TelephonyManager.CALL_STATE_RINGING -> State.Ringing
            else -> State.Idle
        }
    }
}
