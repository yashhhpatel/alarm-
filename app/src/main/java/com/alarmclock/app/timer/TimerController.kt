package com.alarmclock.app.timer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

object TimerController {
    fun start(context: Context, title: String, totalMillis: Long) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_TITLE, title)
            putExtra(TimerService.EXTRA_TOTAL_MILLIS, totalMillis)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pause(context: Context, id: Long) {
        send(context, TimerService.ACTION_PAUSE, id)
    }

    fun resume(context: Context, id: Long) {
        send(context, TimerService.ACTION_RESUME, id)
    }

    fun cancel(context: Context, id: Long) {
        send(context, TimerService.ACTION_CANCEL, id)
    }

    private fun send(context: Context, action: String, id: Long) {
        val intent = Intent(context, TimerService::class.java).apply {
            this.action = action
            putExtra(TimerService.EXTRA_ID, id)
        }
        ContextCompat.startForegroundService(context, intent)
    }
}
