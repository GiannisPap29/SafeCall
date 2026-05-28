package com.pavloskerasidis.mobileapp_safecall.presentation.setup

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechModelInstaller
import com.pavloskerasidis.mobileapp_safecall.domain.usecase.InstallSpeechModelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SetupUiState(
    val isDefaultScreener: Boolean = false,
    val hasRecordAudio: Boolean = false,
    val hasReadPhoneState: Boolean = false,
    val hasNotifications: Boolean = false,
    val hasOverlay: Boolean = false,
    val model: SpeechModelInstaller.InstallState = SpeechModelInstaller.InstallState.NotInstalled,
)

class SetupViewModel(
    private val appContext: Context,
    private val installSpeechModel: InstallSpeechModelUseCase,
    private val modelInstaller: SpeechModelInstaller,
) : ViewModel() {

    private val permissions = MutableStateFlow(PermissionState())

    val state: StateFlow<SetupUiState> =
        combine(permissions, modelInstaller.state) { perms, model ->
            SetupUiState(
                isDefaultScreener = perms.isDefaultScreener,
                hasRecordAudio = perms.hasRecordAudio,
                hasReadPhoneState = perms.hasReadPhoneState,
                hasNotifications = perms.hasNotifications,
                hasOverlay = perms.hasOverlay,
                model = model,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SetupUiState(model = modelInstaller.state.value),
        )

    fun refresh(
        isDefaultScreener: Boolean,
        hasRecordAudio: Boolean,
        hasReadPhoneState: Boolean,
        hasNotifications: Boolean,
        hasOverlay: Boolean,
    ) {
        permissions.value = PermissionState(
            isDefaultScreener = isDefaultScreener,
            hasRecordAudio = hasRecordAudio,
            hasReadPhoneState = hasReadPhoneState,
            hasNotifications = hasNotifications,
            hasOverlay = hasOverlay,
        )
    }

    fun buildRoleRequestIntent(): Intent {
        val roleManager = appContext.getSystemService(Context.ROLE_SERVICE) as RoleManager
        return roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
    }

    fun installModel() {
        viewModelScope.launch { installSpeechModel() }
    }

    private data class PermissionState(
        val isDefaultScreener: Boolean = false,
        val hasRecordAudio: Boolean = false,
        val hasReadPhoneState: Boolean = false,
        val hasNotifications: Boolean = false,
        val hasOverlay: Boolean = false,
    )
}
