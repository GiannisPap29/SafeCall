package com.pavloskerasidis.mobileapp_safecall.di

import com.pavloskerasidis.mobileapp_safecall.presentation.setup.SetupViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        SetupViewModel(
            appContext = androidContext(),
            installSpeechModel = get(),
            modelInstaller = get(),
        )
    }
}
