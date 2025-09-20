package com.example.bestuiintheworld

import android.app.Application
import android.content.Intent
import com.example.bestuiintheworld.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(viewModelModule)
        }
    }

    fun launchDiceGame() {
        val intent = Intent(this, DiceApp::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
