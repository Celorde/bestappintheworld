package com.bestappintheworld.commons

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager


class ContextUtils(
    private val context: Context?
) {
    fun getCurrentBatteryLevel(): Int {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context?.registerReceiver(null, intentFilter)

        val nivel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val escala = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return if (nivel >= 0 && escala > 0) {
            ((nivel.toFloat() / escala.toFloat()) * 100).toInt()
        } else {
            -1 // erro ao obter
        }
    }

    companion object {
        var instance: ContextUtils? = null

        fun getInstance(context: Context? = null): ContextUtils? {
            if (instance == null)
                instance = ContextUtils(context = context)

            return instance
        }
    }
}
