package com.example.gzingapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopNavigationReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_STOP_NAV = "com.example.gzingapp.action.STOP_NAVIGATION"
    }
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_STOP_NAV) {
            val act = Intent(context, MapActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("stop_navigation", true)
            }
            context.startActivity(act)
        }
    }
}

