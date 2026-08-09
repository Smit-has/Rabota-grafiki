package com.workschedule.alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShiftAdapter(
    private var shifts: List<Shift>,
    private val onClick: (Shift) -> Unit
) : RecyclerView.Adapter<ShiftAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvNote: TextView = view.findViewById(R.id.tvNote)
        val tvAlarmInfo: TextView = view.findViewById(R.id.tvAlarmInfo)
        val ivAlarm: ImageView = view.findViewById(R.id.ivAlarm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shift, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shift = shifts[position]
        holder.tvDate.text = shift.getFormattedDate() +
                if (shift.isToday()) "  •  Сегодня" else ""
        holder.tvTime.text = shift.getFormattedTime()

        if (shift.note.isNotEmpty()) {
            holder.tvNote.visibility = View.VISIBLE
            holder.tvNote.text = shift.note
        } else {
            holder.tvNote.visibility = View.GONE
        }

        if (shift.alarmEnabled && !shift.isPast()) {
            holder.ivAlarm.visibility = View.VISIBLE
            holder.tvAlarmInfo.visibility = View.VISIBLE
            holder.tvAlarmInfo.text = "Будильник в ${shift.getAlarmTimeFormatted()}"
        } else {
            holder.ivAlarm.visibility = View.GONE
            holder.tvAlarmInfo.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(shift) }
    }

    override fun getItemCount(): Int = shifts.size

    fun update(newShifts: List<Shift>) {
        shifts = newShifts
        notifyDataSetChanged()
    }
}
