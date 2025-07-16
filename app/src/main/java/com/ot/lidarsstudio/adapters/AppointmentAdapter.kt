package com.ot.lidarsstudio.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ot.lidarsstudio.R
import com.ot.lidarsstudio.utils.Appointment

class AppointmentAdapter(
    private val items: MutableList<Appointment>,
    private val onCancel: (Appointment) -> Unit,
    private val onConfirm: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.VH>() {

    inner class VH(item: View) : RecyclerView.ViewHolder(item) {
        val textDetails: TextView = item.findViewById(R.id.textAppointmentItem)
        val btnAction: Button = item.findViewById(R.id.buttonAppointmentAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val appt = items[pos]

        // עיבוד משך זמן יפה לקריאה
        val durationStr = if (appt.durationMinutes >= 60) {
            val hours = appt.durationMinutes / 60
            val minutes = appt.durationMinutes % 60
            if (minutes == 0) "$hours hr" else "$hours hr $minutes min"
        } else {
            "${appt.durationMinutes} min"
        }

        // בניית טקסט
        holder.textDetails.text = """
            ${appt.service}
            ${appt.date} at ${appt.startHour}
            Duration: $durationStr
            Status: ${appt.status}
        """.trimIndent()

        // כפתור פעולה
        holder.btnAction.visibility = View.VISIBLE
        when (appt.status) {
            "pending" -> {
                holder.btnAction.text = "Confirm"
                holder.btnAction.setOnClickListener { onConfirm(appt) }
            }
            "scheduled" -> {
                holder.btnAction.text = "Cancel"
                holder.btnAction.setOnClickListener { onCancel(appt) }
            }
            else -> {
                holder.btnAction.visibility = View.GONE
            }
        }
    }
}
