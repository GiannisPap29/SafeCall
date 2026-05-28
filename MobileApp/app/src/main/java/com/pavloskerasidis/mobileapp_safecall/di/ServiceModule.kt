package com.pavloskerasidis.mobileapp_safecall.di

import com.pavloskerasidis.mobileapp_safecall.domain.repository.AlertNotifier
import com.pavloskerasidis.mobileapp_safecall.service.alert.OverlayAlertNotifier
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val serviceModule = module {
    single<AlertNotifier> { OverlayAlertNotifier(context = androidContext()) }
}
