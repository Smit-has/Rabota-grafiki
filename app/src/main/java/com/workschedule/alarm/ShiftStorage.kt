package com.workschedule.alarm

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object ShiftStorage {

    private const val PREFS_NAME = "work_shifts"
    private const val KEY_SHIFTS = "shifts"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveShifts(context: Context, shifts: List<Shift>) {
        val array = JSONArray()
        for (shift in shifts) {
            val obj = JSONObject().apply {
                put("id", shift.id)
                put("year", shift.year)
                put("month", shift.month)
                put("day", shift.day)
                put("startHour", shift.startHour)
                put("startMinute", shift.startMinute)
                put("endHour", shift.endHour)
                put("endMinute", shift.endMinute)
                put("minutesBefore", shift.minutesBefore)
                put("note", shift.note)
                put("alarmEnabled", shift.alarmEnabled)
            }
            array.put(obj)
        }
        prefs(context).edit().putString(KEY_SHIFTS, array.toString()).apply()
    }

    fun loadShifts(context: Context): MutableList<Shift> {
        val json = prefs(context).getString(KEY_SHIFTS, "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<Shift>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Shift(
                    id = obj.getString("id"),
                    year = obj.getInt("year"),
                    month = obj.getInt("month"),
                    day = obj.getInt("day"),
                    startHour = obj.getInt("startHour"),
                    startMinute = obj.getInt("startMinute"),
                    endHour = obj.getInt("endHour"),
                    endMinute = obj.getInt("endMinute"),
                    minutesBefore = obj.optInt("minutesBefore", 30),
                    note = obj.optString("note", ""),
                    alarmEnabled = obj.optBoolean("alarmEnabled", true)
                )
            )
        }
        list.sortBy { it.getStartCalendar().timeInMillis }
        return list
    }

    fun addOrUpdate(context: Context, shift: Shift) {
        val shifts = loadShifts(context)
        val index = shifts.indexOfFirst { it.id == shift.id }
        if (index >= 0) {
            shifts[index] = shift
        } else {
            shifts.add(shift)
        }
        saveShifts(context, shifts)
    }

    fun delete(context: Context, shiftId: String) {
        val shifts = loadShifts(context).filter { it.id != shiftId }
        saveShifts(context, shifts)
    }

    fun getShiftsForDay(context: Context, year: Int, month: Int, day: Int): List<Shift> {
        return loadShifts(context).filter {
            it.year == year && it.month == month && it.day == day
        }
    }

    fun getUpcomingShifts(context: Context, limit: Int = 20): List<Shift> {
        val now = System.currentTimeMillis()
        return loadShifts(context)
            .filter { it.getStartCalendar().timeInMillis >= now - 12 * 60 * 60 * 1000 }
            .take(limit)
    }
}
