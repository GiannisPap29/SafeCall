package com.pavloskerasidis.mobileapp_safecall.presentation.setup

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pavloskerasidis.mobileapp_safecall.R
import com.pavloskerasidis.mobileapp_safecall.domain.repository.SpeechModelInstaller.InstallState
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetupScreen(
    modifier: Modifier = Modifier,
    viewModel: SetupViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    fun probe() {
        viewModel.refresh(
            isDefaultScreener = isDefaultScreener(context),
            hasRecordAudio = hasPermission(context, Manifest.permission.RECORD_AUDIO),
            hasReadPhoneState = hasPermission(context, Manifest.permission.READ_PHONE_STATE),
            hasOverlay = Settings.canDrawOverlays(context),
        )
    }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { probe() }

    val phoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { probe() }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { probe() }

    val overlayLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { probe() }

    LaunchedEffect(Unit) { probe() }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.setup_title))

        Button(onClick = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
            Text(
                if (state.hasRecordAudio) stringResource(R.string.setup_mic_granted)
                else stringResource(R.string.setup_mic_grant)
            )
        }

        Button(onClick = { phoneLauncher.launch(Manifest.permission.READ_PHONE_STATE) }) {
            Text(
                if (state.hasReadPhoneState) stringResource(R.string.setup_phone_granted)
                else stringResource(R.string.setup_phone_grant)
            )
        }

        Button(onClick = { roleLauncher.launch(viewModel.buildRoleRequestIntent()) }) {
            Text(
                if (state.isDefaultScreener) stringResource(R.string.setup_screener_granted)
                else stringResource(R.string.setup_screener_grant)
            )
        }

        Button(onClick = {
            overlayLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}"),
                )
            )
        }) {
            Text(
                if (state.hasOverlay) stringResource(R.string.setup_overlay_granted)
                else stringResource(R.string.setup_overlay_grant)
            )
        }

        ModelInstallSection(
            state = state.model,
            onInstall = viewModel::installModel,
        )
    }
}

@Composable
private fun ModelInstallSection(
    state: InstallState,
    onInstall: () -> Unit,
) {
    when (state) {
        InstallState.Installed -> Button(onClick = {}, enabled = false) {
            Text(stringResource(R.string.setup_model_installed))
        }
        is InstallState.Downloading -> {
            val pct = (state.progress * 100).toInt()
            Text(stringResource(R.string.setup_model_installing, pct))
            LinearProgressIndicator(progress = { state.progress })
        }
        InstallState.Unpacking -> {
            Text(stringResource(R.string.setup_model_unpacking))
            LinearProgressIndicator()
        }
        is InstallState.Failed -> Button(onClick = onInstall) {
            Text(stringResource(R.string.setup_model_failed))
        }
        InstallState.NotInstalled -> Button(onClick = onInstall) {
            Text(stringResource(R.string.setup_model_install))
        }
    }
}

private fun isDefaultScreener(context: Context): Boolean {
    val rm = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
    return rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
}

private fun hasPermission(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
