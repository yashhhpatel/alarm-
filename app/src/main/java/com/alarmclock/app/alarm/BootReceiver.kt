package com.alarmclock.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alarmclock.app.AlarmClockApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val app = context.applicationContext as AlarmClockApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val enabled = app.alarmRepository.getEnabled()
                AlarmScheduler(context).rescheduleAll(enabled)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
