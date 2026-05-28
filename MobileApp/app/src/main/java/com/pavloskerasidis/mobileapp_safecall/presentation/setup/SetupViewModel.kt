package com.pavloskerasidis.mobileapp_safecall.presentation.setup

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SetupUiState(
    val isDefaultScreener: Boolean = false,
    val hasRecordAudio: Boolean = false,
    val hasOverlay: Boolean = false,
)

class SetupViewModel(
    private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(SetupUiState())
    val state: StateFlow<SetupUiState> = _state.asStateFlow()

    fun refresh(
        isDefaultScreener: Boolean,
        hasRecordAudio: Boolean,
        hasOverlay: Boolean,
    ) {
        viewModelScope.launch {
            _state.value = SetupUiState(isDefaultScreener, hasRecordAudio, hasOverlay)
        }
    }

    fun buildRoleRequestIntent(): Intent {
        val roleManager = appContext.getSystemService(Context.ROLE_SERVICE) as RoleManager
        return roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
    }
}
