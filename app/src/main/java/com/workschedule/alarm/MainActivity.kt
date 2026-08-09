package com.workschedule.alarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.workschedule.alarm.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ShiftAdapter
    private var currentYear = 0
    private var currentMonth = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        AlarmHelper.createNotificationChannel(this)
        requestNotificationPermission()

        val now = Calendar.getInstance()
        currentYear = now.get(Calendar.YEAR)
        currentMonth = now.get(Calendar.MONTH)

        adapter = ShiftAdapter(emptyList()) { shift ->
            val intent = Intent(this, AddShiftActivity::class.java)
            intent.putExtra("shift_id", shift.id)
            startActivity(intent)
        }
        binding.rvShifts.layoutManager = LinearLayoutManager(this)
        binding.rvShifts.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddShiftActivity::class.java))
        }

        binding.btnPrevMonth.setOnClickListener {
            currentMonth--
            if (currentMonth < 0) {
                currentMonth = 11
                currentYear--
            }
            renderCalendar()
        }

        binding.btnNextMonth.setOnClickListener {
            currentMonth++
            if (currentMonth > 11) {
                currentMonth = 0
                currentYear++
            }
            renderCalendar()
        }

        renderCalendar()
        loadShifts()
    }

    override fun onResume() {
        super.onResume()
        loadShifts()
        renderCalendar()
        AlarmHelper.rescheduleAll(this)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }

    private fun loadShifts() {
        val shifts = ShiftStorage.getUpcomingShifts(this)
        adapter.update(shifts)
        binding.tvEmpty.visibility = if (shifts.isEmpty()) View.VISIBLE else View.GONE
        binding.rvShifts.visibility = if (shifts.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun renderCalendar() {
        val months = arrayOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
        binding.tvMonthYear.text = "${months[currentMonth]} $currentYear"

        binding.calendarGrid.removeAllViews()

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, currentYear)
        cal.set(Calendar.MONTH, currentMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        var firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance()

        val workDays = ShiftStorage.loadShifts(this)
            .filter { it.year == currentYear && it.month == currentMonth }
            .map { it.day }
            .toSet()

        for (i in 0 until offset) {
            val empty = TextView(this)
            empty.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 48.dpToPx()
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            binding.calendarGrid.addView(empty)
        }

        for (day in 1..daysInMonth) {
            val tv = TextView(this)
            tv.text = day.toString()
            tv.gravity = Gravity.CENTER
            tv.textSize = 14f
            tv.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 48.dpToPx()
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(2, 2, 2, 2)
            }

            val isToday = currentYear == today.get(Calendar.YEAR) &&
                    currentMonth == today.get(Calendar.MONTH) &&
                    day == today.get(Calendar.DAY_OF_MONTH)

            when {
                workDays.contains(day) -> {
                    tv.setBackgroundResource(R.drawable.day_work)
                    tv.setTextColor(ContextCompat.getColor(this, R.color.primary_dark))
                    tv.setTypeface(null, android.graphics.Typeface.BOLD)
                }
                isToday -> {
                    tv.setBackgroundResource(R.drawable.day_today)
                    tv.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                else -> {
                    tv.setBackgroundResource(R.drawable.day_background)
                    tv.setTextColor(ContextCompat.getColor(this, R.color.black))
                }
            }

            tv.setOnClickListener {
                val intent = Intent(this, AddShiftActivity::class.java)
                intent.putExtra("prefill_year", currentYear)
                intent.putExtra("prefill_month", currentMonth)
                intent.putExtra("prefill_day", day)
                startActivity(intent)
            }

            binding.calendarGrid.addView(tv)
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
