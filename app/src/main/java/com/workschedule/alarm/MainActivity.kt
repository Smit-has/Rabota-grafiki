package com.workschedule.alarm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var shiftsContainer: LinearLayout
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AlarmHelper.createNotificationChannel(this)

        shiftsContainer = findViewById(R.id.shiftsContainer)
        tvEmpty = findViewById(R.id.tvEmpty)

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            startActivity(Intent(this, AddShiftActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadShifts()
        AlarmHelper.rescheduleAll(this)
    }

    private fun loadShifts() {
        shiftsContainer.removeAllViews()
        val shifts = ShiftStorage.getUpcomingShifts(this)

        if (shifts.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            return
        }

        tvEmpty.visibility = View.GONE

        for (shift in shifts) {
            val tv = TextView(this)
            val alarmText = if (shift.alarmEnabled && !shift.isPast()) {
                "\nБудильник: ${shift.getAlarmTimeFormatted()}"
            } else ""
            tv.text = "\( {shift.getFormattedDate()}\n \){shift.getFormattedTime()}$alarmText" +
                    if (shift.note.isNotEmpty()) "\n${shift.note}" else ""
            tv.textSize = 16f
            tv.setPadding(24, 24, 24, 24)
            tv.setBackgroundColor(0xFFE3F2FD.toInt())
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            tv.layoutParams = params

            tv.setOnClickListener {
                val intent = Intent(this, AddShiftActivity::class.java)
                intent.putExtra("shift_id", shift.id)
                startActivity(intent)
            }
            shiftsContainer.addView(tv)
        }
    }
}
