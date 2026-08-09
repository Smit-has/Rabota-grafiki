package com.workschedule.alarm

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.workschedule.alarm.databinding.ActivityAddShiftBinding
import java.util.Calendar

class AddShiftActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddShiftBinding

    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0
    private var startHour = 9
    private var startMinute = 0
    private var endHour = 18
    private var endMinute = 0
    private var editingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddShiftBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_shift)

        val now = Calendar.getInstance()
        year = intent.getIntExtra("prefill_year", now.get(Calendar.YEAR))
        month = intent.getIntExtra("prefill_month", now.get(Calendar.MONTH))
        day = intent.getIntExtra("prefill_day", now.get(Calendar.DAY_OF_MONTH))

        editingId = intent.getStringExtra("shift_id")
        if (editingId != null) {
            val shifts = ShiftStorage.loadShifts(this)
            val shift = shifts.find { it.id == editingId }
            if (shift != null) {
                year = shift.year
                month = shift.month
                day = shift.day
                startHour = shift.startHour
                startMinute = shift.startMinute
                endHour = shift.endHour
                endMinute = shift.endMinute
                binding.etMinutesBefore.setText(shift.minutesBefore.toString())
                binding.etNote.setText(shift.note)
                binding.switchAlarm.isChecked = shift.alarmEnabled
                binding.btnDelete.visibility = View.VISIBLE
                supportActionBar?.title = "Редактировать смену"
            }
        }

        updateDateButton()
        updateTimeButtons()

        binding.btnSelectDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    year = y
                    month = m
                    day = d
                    updateDateButton()
                },
                year, month, day
            ).show()
        }

        binding.btnStartTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, h, m ->
                    startHour = h
                    startMinute = m
                    updateTimeButtons()
                },
                startHour, startMinute, true
            ).show()
        }

        binding.btnEndTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, h, m ->
                    endHour = h
                    endMinute = m
                    updateTimeButtons()
                },
                endHour, endMinute, true
            ).show()
        }

        binding.btnSave.setOnClickListener { saveShift() }

        binding.btnDelete.setOnClickListener {
            editingId?.let { id ->
                AlarmHelper.cancelAlarm(this, id)
                ShiftStorage.delete(this, id)
                Toast.makeText(this, "Смена удалена", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateDateButton() {
        val months = arrayOf(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )
        binding.btnSelectDate.text = "$day ${months[month]} $year"
    }

    private fun updateTimeButtons() {
        binding.btnStartTime.text = String.format("%02d:%02d", startHour, startMinute)
        binding.btnEndTime.text = String.format("%02d:%02d", endHour, endMinute)
    }

    private fun saveShift() {
        val minutesBefore = binding.etMinutesBefore.text.toString().toIntOrNull() ?: 30
        val note = binding.etNote.text.toString().trim()
        val alarmEnabled = binding.switchAlarm.isChecked

        val shift = Shift(
            id = editingId ?: java.util.UUID.randomUUID().toString(),
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
                "Смена сохранена. Будильник в ${shift.getAlarmTimeFormatted()}",
                Toast.LENGTH_LONG
            ).show()
        } else {
            AlarmHelper.cancelAlarm(this, shift.id)
            Toast.makeText(this, "Смена сохранена", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
