package com.workschedule.alarm

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object AlarmHelper {

    const val CHANNEL_ID = "work_shift_alarms_v2"
    const val EXTRA_SHIFT_ID = "shift_id"
    const val EXTRA_SHIFT_NOTE = "shift_note"
    const val EXTRA_SHIFT_TIME = "shift_time"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            try {
                manager.deleteNotificationChannel("work_shift_alarms")
            } catch (_: Exception) {
            }

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = context.getString(R.string.notification_channel_desc)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 500, 300, 500, 300, 500)
            channel.setSound(soundUri, audioAttributes)
            channel.setBypassDnd(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            manager.createNotificationChannel(channel)
        }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    fun scheduleAlarm(context: Context, shift: Shift) {
        if (!shift.alarmEnabled) {
            cancelAlarm(context, shift.id)
            return
        }

        if (!canScheduleExactAlarms(context)) {
            Toast.makeText(context, R.string.permission_needed, Toast.LENGTH_LONG).show()
            openExactAlarmSettings(context)
            return
        }

        val alarmTime = shift.getAlarmCalendar().timeInMillis
        if (alarmTime <= System.currentTimeMillis()) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_SHIFT_ID, shift.id)
        intent.putExtra(EXTRA_SHIFT_NOTE, if (shift.note.isEmpty()) shift.getFormattedTime() else shift.note)
        intent.putExtra(EXTRA_SHIFT_TIME, shift.getFormattedTime())

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            shift.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, R.string.permission_needed, Toast.LENGTH_LONG).show()
            openExactAlarmSettings(context)
        }
    }

    fun cancelAlarm(context: Context, shiftId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            shiftId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun rescheduleAll(context: Context) {
        val shifts = ShiftStorage.loadShifts(context)
        for (shift in shifts) {
            if (shift.alarmEnabled && !shift.isPast()) {
                scheduleAlarm(context, shift)
            }
        }
    }
}
