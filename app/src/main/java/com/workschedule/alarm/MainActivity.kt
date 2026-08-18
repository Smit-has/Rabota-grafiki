package com.workschedule.alarm

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private var year = 0
    private var month = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AlarmHelper.createNotificationChannel(this)

        val now = Calendar.getInstance()
        year = now.get(Calendar.YEAR)
        month = now.get(Calendar.MONTH)

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            startActivity(Intent(this, AddShiftActivity::class.java))
        }
        findViewById<Button>(R.id.btnPrev).setOnClickListener {
            month--
            if (month < 0) {
                month = 11
                year--
            }
            drawCalendar()
        }
        findViewById<Button>(R.id.btnNext).setOnClickListener {
            month++
            if (month > 11) {
                month = 0
                year++
            }
            drawCalendar()
        }
    }

    override fun onResume() {
        super.onResume()
        drawCalendar()
        showShifts()
        AlarmHelper.rescheduleAll(this)
    }

    private fun drawCalendar() {
        val months = arrayOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
        findViewById<TextView>(R.id.tvMonth).text = months[month] + " " + year

        val grid = findViewById<GridLayout>(R.id.calendarGrid)
        grid.removeAllViews()

        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        val first = cal.get(Calendar.DAY_OF_WEEK)
        val offset = if (first == Calendar.SUNDAY) 6 else first - 2
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val workDays = ShiftStorage.loadShifts(this)
            .filter { it.year == year && it.month == month }
            .map { it.day }
            .toSet()

        val today = Calendar.getInstance()
        val cellH = (72 * resources.displayMetrics.density).toInt()

        for (i in 0 until offset) {
            val empty = TextView(this)
            empty.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = cellH
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            grid.addView(empty)
        }

        for (day in 1..daysInMonth) {
            val tv = TextView(this)
            tv.text = day.toString()
            tv.gravity = Gravity.CENTER
            tv.textSize = 17f
            tv.setTextColor(Color.parseColor("#212121"))
            tv.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = cellH
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(3, 3, 3, 3)
            }

            val isToday = year == today.get(Calendar.YEAR) &&
                    month == today.get(Calendar.MONTH) &&
                    day == today.get(Calendar.DAY_OF_MONTH)

            if (workDays.contains(day)) {
                tv.setBackgroundColor(Color.parseColor("#C8E6C9"))
                tv.setTypeface(null, Typeface.BOLD)
                tv.setTextColor(Color.parseColor("#1B5E20"))
            } else if (isToday) {
                tv.setBackgroundColor(Color.parseColor("#BBDEFB"))
                tv.setTextColor(Color.parseColor("#0D47A1"))
            }

            tv.setOnClickListener {
                val intent = Intent(this, AddShiftActivity::class.java)
                intent.putExtra("prefill_year", year)
                intent.putExtra("prefill_month", month)
                intent.putExtra("prefill_day", day)
                startActivity(intent)
            }
            grid.addView(tv)
        }
    }

    private fun showShifts() {
        val container = findViewById<LinearLayout>(R.id.shiftsContainer)
        val empty = findViewById<TextView>(R.id.tvEmpty)
        container.removeAllViews()
        val list = ShiftStorage.getUpcomingShifts(this)
        if (list.isEmpty()) {
            empty.visibility = View.VISIBLE
            return
        }
        empty.visibility = View.GONE
        for (s in list) {
            val t = TextView(this)
            var text = s.getFormattedDate() + "\n" + s.getFormattedTime()
            if (s.alarmEnabled && !s.isPast()) {
                text = text + "\nБудильник: " + s.getAlarmTimeFormatted()
            }
            if (s.note.isNotEmpty()) {
                text = text + "\n" + s.note
            }
            t.text = text
            t.textSize = 15f
            t.setTextColor(Color.parseColor("#212121"))
            t.setPadding(20, 16, 20, 16)
            t.setBackgroundColor(Color.parseColor("#E3F2FD"))
            val p = LinearLayout.LayoutParams(-1, -2)
            p.bottomMargin = 10
            t.layoutParams = p
            t.setOnClickListener {
                val i = Intent(this, AddShiftActivity::class.java)
                i.putExtra("shift_id", s.id)
                startActivity(i)
            }
            container.addView(t)
        }
    }
}
