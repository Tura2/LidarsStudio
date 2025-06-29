package com.ot.lidarsstudio.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ot.lidarsstudio.R
import com.ot.lidarsstudio.models.Appointment
import java.text.SimpleDateFormat
import java.util.*

class AppointmentAdapter(
    private val items: MutableList<Appointment>,
    private val onCancel: (Appointment) -> Unit,
    private val onConfirm: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.VH>() {

    inner class VH(item: View) : RecyclerView.ViewHolder(item) {
        val textDetails: TextView = item.findViewById(R.id.textAppointmentItem)
        val btnAction: Button   = item.findViewById(R.id.buttonAppointmentAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, pos: Int) {
        val appt = items[pos]
        val dfDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val dt = dfDate.format(appt.dateTime.toDate())
        holder.textDetails.text = "${appt.service}\n$dt\nStatus: ${appt.status}"

        holder.btnAction.visibility = View.VISIBLE
        if (appt.status == "pending") {
            holder.btnAction.text = "Confirm"
            holder.btnAction.setOnClickListener { onConfirm(appt) }
        } else if (appt.status == "scheduled") {
            holder.btnAction.text = "Cancel"
            holder.btnAction.setOnClickListener { onCancel(appt) }
        } else {
            holder.btnAction.visibility = View.GONE
        }
    }
}
