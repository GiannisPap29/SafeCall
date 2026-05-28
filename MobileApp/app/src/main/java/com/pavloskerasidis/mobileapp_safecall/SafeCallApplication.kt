package com.pavloskerasidis.mobileapp_safecall

import android.app.Application
import com.pavloskerasidis.mobileapp_safecall.di.appModule
import com.pavloskerasidis.mobileapp_safecall.di.dataModule
import com.pavloskerasidis.mobileapp_safecall.di.domainModule
import com.pavloskerasidis.mobileapp_safecall.di.presentationModule
import com.pavloskerasidis.mobileapp_safecall.di.serviceModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SafeCallApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@SafeCallApplication)
            modules(
                appModule,
                domainModule,
                dataModule,
                serviceModule,
                presentationModule,
            )
        }
    }
}
