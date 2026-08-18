package com.workschedule.alarm

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.UUID

class AddShiftActivity : AppCompatActivity() {

    private var year = 0
    private var month = 0
    private var day = 0
    private var startHour = 9
    private var startMinute = 0
    private var endHour = 18
    private var endMinute = 0
    private var editingId: String? = null

    private lateinit var btnSelectDate: Button
    private lateinit var btnStartTime: Button
    private lateinit var btnEndTime: Button
    private lateinit var etMinutesBefore: EditText
    private lateinit var etNote: EditText
    private lateinit var cbAlarm: CheckBox
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_shift)

        btnSelectDate = findViewById(R.id.btnSelectDate)
        btnStartTime = findViewById(R.id.btnStartTime)
        btnEndTime = findViewById(R.id.btnEndTime)
        etMinutesBefore = findViewById(R.id.etMinutesBefore)
        etNote = findViewById(R.id.etNote)
        cbAlarm = findViewById(R.id.cbAlarm)
        btnDelete = findViewById(R.id.btnDelete)

        val now = Calendar.getInstance()
        year = intent.getIntExtra("prefill_year", now.get(Calendar.YEAR))
        month = intent.getIntExtra("prefill_month", now.get(Calendar.MONTH))
        day = intent.getIntExtra("prefill_day", now.get(Calendar.DAY_OF_MONTH))

        editingId = intent.getStringExtra("shift_id")
        if (editingId != null) {
            val shift = ShiftStorage.loadShifts(this).find { it.id == editingId }
            if (shift != null) {
                year = shift.year
                month = shift.month
                day = shift.day
                startHour = shift.startHour
                startMinute = shift.startMinute
                endHour = shift.endHour
                endMinute = shift.endMinute
                etMinutesBefore.setText(shift.minutesBefore.toString())
                etNote.setText(shift.note)
                cbAlarm.isChecked = shift.alarmEnabled
                btnDelete.visibility = View.VISIBLE
            }
        }

        updateDateButton()
        updateTimeButtons()

        btnSelectDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                year = y
                month = m
                day = d
                updateDateButton()
            }, year, month, day).show()
        }

        btnStartTime.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                startHour = h
                startMinute = m
                updateTimeButtons()
            }, startHour, startMinute, true).show()
        }

        btnEndTime.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                endHour = h
                endMinute = m
                updateTimeButtons()
            }, endHour, endMinute, true).show()
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener { saveShift() }

        btnDelete.setOnClickListener {
            val id = editingId
            if (id != null) {
                AlarmHelper.cancelAlarm(this, id)
                ShiftStorage.delete(this, id)
                Toast.makeText(this, "Smena udalena", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateDateButton() {
        val months = arrayOf(
            "yanvarya", "fevralya", "marta", "aprelya", "maya", "iyunya",
            "iyulya", "avgusta", "sentyabrya", "oktyabrya", "noyabrya", "dekabrya"
        )
        btnSelectDate.text = day.toString() + " " + months[month] + " " + year
    }

    private fun updateTimeButtons() {
        btnStartTime.text = String.format("%02d:%02d", startHour, startMinute)
        btnEndTime.text = String.format("%02d:%02d", endHour, endMinute)
    }

    private fun saveShift() {
        val minutesBefore = etMinutesBefore.text.toString().toIntOrNull() ?: 30
        val note = etNote.text.toString().trim()
        val alarmEnabled = cbAlarm.isChecked

        val id = if (editingId != null) editingId!! else UUID.randomUUID().toString()

        val shift = Shift(
            id = id,
            year = year,
            month = month,
            day = day,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            minutesBefore = minutesBefore,
            note = note,
            alarmEnabled = alarmEnabled
        )

        ShiftStorage.addOrUpdate(this, shift)

        if (alarmEnabled) {
            AlarmHelper.scheduleAlarm(this, shift)
            Toast.makeText(
                this,
                "Sohraneno. Budilnik: " + shift.getAlarmTimeFormatted(),
                Toast.LENGTH_LONG
            ).show()
        } else {
            AlarmHelper.cancelAlarm(this, shift.id)
            Toast.makeText(this, "Sohraneno", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}
