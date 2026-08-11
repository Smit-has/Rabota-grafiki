package com.workschedule.alarm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AlarmHelper.createNotificationChannel(this)
        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            startActivity(Intent(this, AddShiftActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        showShifts()
        AlarmHelper.rescheduleAll(this)
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
            t.text = s.getFormattedDate() + "\n" + s.getFormattedTime()
            t.textSize = 16f
            t.setPadding(20, 20, 20, 20)
            t.setBackgroundColor(0xFFE3F2FD.toInt())
            val p = LinearLayout.LayoutParams(-1, -2)
            p.bottomMargin = 12
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
