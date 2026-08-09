package com.workschedule.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val shiftId = intent.getStringExtra(AlarmHelper.EXTRA_SHIFT_ID) ?: return
        val note = intent.getStringExtra(AlarmHelper.EXTRA_SHIFT_NOTE) ?: ""
        val time = intent.getStringExtra(AlarmHelper.EXTRA_SHIFT_TIME) ?: ""

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmHelper.EXTRA_SHIFT_ID, shiftId)
            putExtra(AlarmHelper.EXTRA_SHIFT_NOTE, note)
            putExtra(AlarmHelper.EXTRA_SHIFT_TIME, time)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
