package com.dktech.baseandroidviewdktech.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.emulator.retro.console.game.utils.isNetworkConnected

class InternetStatusReceiverNovoSyx(private val onStateChanged: (Boolean) -> Unit) :
    BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            onStateChanged(context.isNetworkConnected())
        }
    }
}