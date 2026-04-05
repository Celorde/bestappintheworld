package com.bestappintheworld

import android.app.Application
import android.content.Intent
import com.bestappintheworld.commons.ContextUtils
import com.example.bestappintheworld.DiceApp
import com.bestappintheworld.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(viewModelModule)
        }

        //TODO NÃO PODE FAZER ESSA CAGADA, MUDAR DEPOIS
        ContextUtils.getInstance(this)
    }

    fun launchDiceGame() {
        val intent = Intent(this, DiceApp::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
