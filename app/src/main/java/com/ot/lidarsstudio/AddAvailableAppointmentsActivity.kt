package com.ot.lidarsstudio

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddAvailableAppointmentsActivity : AppCompatActivity() {

    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var btnCreateSlots: Button
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var spinnerStartTime: Spinner
    private lateinit var spinnerEndTime: Spinner

    private lateinit var textSelectedStartDate: TextView
    private lateinit var textSelectedEndDate: TextView

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    private var isAddMode = true

    private val db = FirebaseFirestore.getInstance()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_available_appointments)

        btnStartDate = findViewById(R.id.buttonPickStartDate)
        btnEndDate = findViewById(R.id.buttonPickEndDate)
        btnCreateSlots = findViewById(R.id.buttonCreateSlots)
        toggleGroup = findViewById(R.id.toggleAddRemoveGroup)
        spinnerStartTime = findViewById(R.id.spinnerStartTime)
        spinnerEndTime = findViewById(R.id.spinnerEndTime)

        textSelectedStartDate = findViewById(R.id.textSelectedStartDate)
        textSelectedEndDate = findViewById(R.id.textSelectedEndDate)

        // טיימינג אפשרי (30 דקות) להתחלה וסיום
        val timeSlots = generateTimeSlots("08:00", "20:00", 30)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStartTime.adapter = adapter
        spinnerEndTime.adapter = adapter

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isAddMode = checkedId == R.id.buttonToggleAdd
                Toast.makeText(this, "Mode: ${if (isAddMode) "Add" else "Remove"}", Toast.LENGTH_SHORT).show()
            }
        }

        // ברירת מחדל: Add נבחר
        toggleGroup.check(R.id.buttonToggleAdd)

        btnStartDate.setOnClickListener {
            showDatePicker { cal ->
                startDate = cal
                textSelectedStartDate.text = "Selected start date: ${dateFormat.format(cal.time)}"
                textSelectedStartDate.visibility = View.VISIBLE
            }
        }

        btnEndDate.setOnClickListener {
            showDatePicker { cal ->
                endDate = cal
                textSelectedEndDate.text = "Selected end date: ${dateFormat.format(cal.time)}"
                textSelectedEndDate.visibility = View.VISIBLE
            }
        }

        btnCreateSlots.setOnClickListener {
            createAppointmentSlots()
        }
    }

    private fun showDatePicker(onPicked: (Calendar) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val cal = Calendar.getInstance()
            cal.set(y, m, d, 0, 0, 0)
            if (cal.before(now)) {
                Toast.makeText(this, "Cannot select past dates", Toast.LENGTH_SHORT).show()
            } else {
                onPicked(cal)
            }
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun createAppointmentSlots() {
        val start = startDate ?: run {
            Toast.makeText(this, "Pick start date", Toast.LENGTH_SHORT).show()
            return
        }
        val end = endDate ?: run {
            Toast.makeText(this, "Pick end date", Toast.LENGTH_SHORT).show()
            return
        }

        if (end.before(start)) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show()
            return
        }

        val startTimeStr = spinnerStartTime.selectedItem as? String ?: run {
            Toast.makeText(this, "Select start time", Toast.LENGTH_SHORT).show()
            return
        }
        val endTimeStr = spinnerEndTime.selectedItem as? String ?: run {
            Toast.makeText(this, "Select end time", Toast.LENGTH_SHORT).show()
            return
        }

        val startTime = timeFormat.parse(startTimeStr) ?: run {
            Toast.makeText(this, "Invalid start time", Toast.LENGTH_SHORT).show()
            return
        }
        val endTime = timeFormat.parse(endTimeStr) ?: run {
            Toast.makeText(this, "Invalid end time", Toast.LENGTH_SHORT).show()
            return
        }

        if (endTime.before(startTime)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }

        val cal = start.clone() as Calendar
        while (!cal.after(end)) {
            val dateStr = dateFormat.format(cal.time)
            // מחולל סלוטים בין שעות התחלה לסיום
            val slots = generateTimeSlots(startTimeStr, endTimeStr, 30)
            if (isAddMode) {
                // ADD MODE
                db.collection("availableAppointments")
                    .document(dateStr)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val existing = snapshot.get("slots") as? Map<String, Any> ?: mapOf()
                        val merged = existing.toMutableMap()
                        for (slot in slots) {
                            merged[slot] = mapOf("isBooked" to false)
                        }
                        db.collection("availableAppointments")
                            .document(dateStr)
                            .set(mapOf("slots" to merged))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Added slots to $dateStr", Toast.LENGTH_SHORT).show()
                            }
                    }
            } else {
                // REMOVE MODE
                db.collection("availableAppointments")
                    .document(dateStr)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val existing = snapshot.get("slots") as? MutableMap<String, Any> ?: return@addOnSuccessListener
                        for (slot in slots) {
                            existing.remove(slot)
                        }
                        db.collection("availableAppointments")
                            .document(dateStr)
                            .update("slots", existing)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Removed slots from $dateStr", Toast.LENGTH_SHORT).show()
                            }
                    }
            }
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun generateTimeSlots(start: String, end: String, intervalMinutes: Int): List<String> {
        val startTime = timeFormat.parse(start) ?: return emptyList()
        val endTime = timeFormat.parse(end) ?: return emptyList()

        val calendar = Calendar.getInstance().apply { time = startTime }
        val endCalendar = Calendar.getInstance().apply { time = endTime }

        val slots = mutableListOf<String>()
        while (!calendar.after(endCalendar)) {
            slots.add(timeFormat.format(calendar.time))
            calendar.add(Calendar.MINUTE, intervalMinutes)
        }
        return slots
    }
}
