package com.workschedule.alarm

import java.io.Serializable
import java.util.Calendar
import java.util.UUID

data class Shift(
    val id: String = UUID.randomUUID().toString(),
    val year: Int,
    val month: Int,
    val day: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val minutesBefore: Int = 30,
    val note: String = "",
    val alarmEnabled: Boolean = true
) : Serializable {

    fun getStartCalendar(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun getAlarmCalendar(): Calendar {
        val cal = getStartCalendar()
        cal.add(Calendar.MINUTE, -minutesBefore)
        return cal
    }

    fun getDateKey(): String = "$year-$month-$day"

    fun getFormattedDate(): String {
        val months = arrayOf(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )
        return "$day ${months[month]} $year"
    }

    fun getFormattedTime(): String {
        return String.format("%02d:%02d – %02d:%02d", startHour, startMinute, endHour, endMinute)
    }

    fun getAlarmTimeFormatted(): String {
        val cal = getAlarmCalendar()
        return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

    fun isPast(): Boolean {
        return getStartCalendar().timeInMillis < System.currentTimeMillis()
    }

    fun isToday(): Boolean {
        val now = Calendar.getInstance()
        return year == now.get(Calendar.YEAR) &&
                month == now.get(Calendar.MONTH) &&
                day == now.get(Calendar.DAY_OF_MONTH)
    }
}
