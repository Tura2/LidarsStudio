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
    private val isManager: Boolean, // פרמטר חדש
    private val onCancel: (Appointment) -> Unit,
    private val onComplete: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.VH>() {

    inner class VH(item: View) : RecyclerView.ViewHolder(item) {
        val textDetails: TextView = item.findViewById(R.id.textAppointmentItem)
        val btnCancel: Button = item.findViewById(R.id.buttonCancel)
        val btnComplete: Button? = item.findViewById(R.id.buttonComplete) // אופציונלי, ייתכן ונוסיף לitem layout
        val textPhone: TextView? = item.findViewById(R.id.textPhone) // ייתכן ונוסיף לitem layout
        val textName: TextView? = item.findViewById(R.id.textName)   // הוסף TextView לשם מלא
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutId = if (isManager) R.layout.item_appointment_manager else R.layout.item_appointment_customer
        val v = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val appt = items[pos]

        val durationStr = if (appt.durationMinutes >= 60) {
            val hours = appt.durationMinutes / 60
            val minutes = appt.durationMinutes % 60
            if (minutes == 0) "$hours hr" else "$hours hr $minutes min"
        } else {
            "${appt.durationMinutes} min"
        }

        val baseText = """
            ${appt.service}
            ${appt.date} at ${appt.startHour}
            Duration: $durationStr
            Status: ${appt.status}
        """.trimIndent()

        if (isManager) {
            // הצג שם מלא וטלופון במנהל
            val phoneText = appt.phone ?: "No phone"
            val nameText = appt.fullName ?: "No name"
            holder.textPhone?.text = phoneText
            holder.textName?.text = nameText
            holder.textDetails.text = baseText

            if (appt.status == "completed") {
                holder.btnCancel.visibility = View.GONE
                holder.btnComplete?.visibility = View.GONE
            } else {
                holder.btnCancel.visibility = View.VISIBLE
                holder.btnComplete?.visibility = View.VISIBLE
                holder.btnCancel.setOnClickListener { onCancel(appt) }
                holder.btnComplete?.setOnClickListener { onComplete(appt) }
            }
        } else {
            holder.textDetails.text = baseText

            if (appt.status == "completed") {
                holder.btnCancel.visibility = View.GONE
            } else {
                holder.btnCancel.visibility = View.VISIBLE
                holder.btnCancel.setOnClickListener { onCancel(appt) }
            }
        }
    }
}
