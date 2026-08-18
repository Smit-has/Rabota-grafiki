package com.workschedule.alarm

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ShiftStorage {

    private const val PREFS_NAME = "work_shifts"
    private const val KEY_SHIFTS = "shifts"

    fun saveShifts(context: Context, shifts: List<Shift>) {
        val array = JSONArray()
        for (shift in shifts) {
            val obj = JSONObject()
            obj.put("id", shift.id)
            obj.put("year", shift.year)
            obj.put("month", shift.month)
            obj.put("day", shift.day)
            obj.put("startHour", shift.startHour)
            obj.put("startMinute", shift.startMinute)
            obj.put("endHour", shift.endHour)
            obj.put("endMinute", shift.endMinute)
            obj.put("minutesBefore", shift.minutesBefore)
            obj.put("note", shift.note)
            obj.put("alarmEnabled", shift.alarmEnabled)
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SHIFTS, array.toString())
            .commit()
    }

    fun loadShifts(context: Context): MutableList<Shift> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SHIFTS, "[]") ?: "[]"
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

    fun getUpcomingShifts(context: Context, limit: Int = 30): List<Shift> {
        val now = System.currentTimeMillis() - 12L * 60 * 60 * 1000
        return loadShifts(context)
            .filter { it.getStartCalendar().timeInMillis >= now }
            .take(limit)
    }
}
