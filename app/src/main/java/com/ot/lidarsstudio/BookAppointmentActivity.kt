package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ot.lidarsstudio.utils.Appointment
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat
import android.widget.Toast

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var selectedServiceName: String = ""
    private var selectedDate: String = ""
    private var selectedHour: String = ""
    private var pendingAppointment: Appointment? = null
    private var confirmedAppointment: Appointment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val buttonGel = findViewById<MaterialButton>(R.id.buttonGelNails)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        val btnConfirmNow = findViewById<MaterialButton>(R.id.buttonConfirmPendingAppointment)
        val btnCancel = findViewById<MaterialButton>(R.id.buttonCancelAppointment)

        btnClose.setOnClickListener { finish() }

        buttonGel.setOnClickListener {
            pendingAppointment = null
            updateAppointmentViews()
            showGelNailOptions()
        }

        val buttonTattoo = findViewById<MaterialButton>(R.id.buttonTattoo)
        buttonTattoo.setOnClickListener {
            val intent = Intent(this, TattooRequestActivity::class.java)
            startActivity(intent)
        }


        btnConfirmNow.setOnClickListener {
            pendingAppointment?.let {
                saveAppointmentToFirestore(it)
            }
        }

        btnCancel.setOnClickListener {
            confirmedAppointment?.let {
                cancelAppointment(it)
            }
        }

        updateAppointmentViews()
    }

    private fun getDurationForService(service: String): Int {
        return when (service) {
            "Regular Gel" -> 60
            "Gel with Anatomy" -> 90
            "Painted Gel" -> 90
            else -> 60
        }
    }

    private fun updateAppointmentViews() {
        val textConfirmed = findViewById<TextView>(R.id.textActiveAppointmentDetails)
        val textPending = findViewById<TextView>(R.id.textPendingAppointmentDetails)
        val layoutActiveAppointmentBlock =
            findViewById<LinearLayout>(R.id.layoutActiveAppointmentBlock)
        val textConfirmedTitle = findViewById<TextView>(R.id.textActiveAppointment)
        val textPendingTitle = findViewById<TextView>(R.id.textPendingAppointment)
        val btnConfirmNow = findViewById<MaterialButton>(R.id.buttonConfirmPendingAppointment)

        confirmedAppointment?.let {
            textConfirmed.text = "Service: ${it.service}\nDate: ${it.date}\nHour: ${it.startHour}"
            textConfirmedTitle.visibility = View.VISIBLE
            layoutActiveAppointmentBlock.visibility = View.VISIBLE
        } ?: run {
            textConfirmedTitle.visibility = View.GONE
            layoutActiveAppointmentBlock.visibility = View.GONE
        }

        pendingAppointment?.let {
            textPending.text = "Service: ${it.service}\nDate: ${it.date}\nHour: ${it.startHour}"
            textPending.visibility = View.VISIBLE
            textPendingTitle.visibility = View.VISIBLE
            btnConfirmNow.visibility = View.VISIBLE
        } ?: run {
            textPending.visibility = View.GONE
            textPendingTitle.visibility = View.GONE
            btnConfirmNow.visibility = View.GONE
        }
    }

    private fun showGelNailOptions() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_gel_options, null)

        val options = mapOf(
            R.id.btnRegularGel to "Regular Gel",
            R.id.btnAnatomyGel to "Gel with Anatomy",
            R.id.btnPaintedGel to "Painted Gel"
        )

        for ((id, name) in options) {
            view.findViewById<MaterialButton>(id).setOnClickListener {
                selectedServiceName = name
                dialog.dismiss()
                showDateTimePicker()
            }
        }

        view.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showDateTimePicker() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_select_date_time, null)
        dialog.setContentView(view)

        val layoutDates = view.findViewById<LinearLayout>(R.id.layoutDates)
        val layoutHours = view.findViewById<GridLayout>(R.id.layoutHours)
        val textChooseHour = view.findViewById<TextView>(R.id.textChooseHour)

        val sampleDates = listOf(
            "2025-07-16",
            "2025-07-17",
            "2025-07-18",
            "2025-07-19",
            "2025-07-20",
            "2025-07-21"
        )
        var selectedDateBtn: MaterialButton? = null

        for (date in sampleDates) {
            val btn = MaterialButton(this).apply {
                text = date
                setPadding(36, 12, 36, 12)
                setTextColor(ContextCompat.getColor(context, R.color.white))
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.primaryColor)
            }

            btn.setOnClickListener {
                selectedDateBtn?.backgroundTintList = ContextCompat.getColorStateList(
                    this@BookAppointmentActivity,
                    R.color.secondaryColor
                )
                btn.backgroundTintList = ContextCompat.getColorStateList(
                    this@BookAppointmentActivity,
                    R.color.primaryColor
                )
                selectedDateBtn = btn
                selectedDate = date

                layoutHours.removeAllViews()
                textChooseHour.visibility = View.GONE

                // טען את slots מ-Firestore לפי התאריך הנבחר
                db.collection("availableAppointments")
                    .document(date)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val slotsMap = doc.get("slots") as? Map<String, Any> ?: emptyMap()
                            val availableSlots = slotsMap.filter { (_, value) ->
                                val details = value as? Map<String, Any>
                                val isBooked = details?.get("isBooked") as? Boolean ?: true
                                !isBooked
                            }.keys.sorted()

                            layoutHours.removeAllViews()  // נקה קודם כל את הקונטיינר

                            if (availableSlots.isEmpty()) {
                                Toast.makeText(
                                    this,
                                    "No available slots for $date",
                                    Toast.LENGTH_SHORT
                                ).show()
                                textChooseHour.visibility = View.GONE
                                view.findViewById<ScrollView>(R.id.scrollHours).visibility =
                                    View.GONE
                            } else {
                                for (time in availableSlots) {
                                    Log.d("BookAppointment", "Adding button for $time")
                                    val timeBtn = MaterialButton(this).apply {
                                        text = time
                                        setPadding(36, 12, 36, 12)
                                        backgroundTintList = ContextCompat.getColorStateList(
                                            context,
                                            R.color.primaryColor
                                        )
                                        setTextColor(ContextCompat.getColor(context, R.color.white))
                                    }
                                    timeBtn.setOnClickListener {
                                        selectedHour = time
                                        val duration = getDurationForService(selectedServiceName)
                                        pendingAppointment = Appointment(
                                            id = "", service = selectedServiceName,
                                            date = selectedDate, startHour = selectedHour,
                                            durationMinutes = duration, status = "scheduled"
                                        )
                                        dialog.dismiss()
                                        updateAppointmentViews()
                                    }
                                    layoutHours.addView(timeBtn)
                                }
                                textChooseHour.visibility = View.VISIBLE
                                view.findViewById<ScrollView>(R.id.scrollHours).visibility =
                                    View.VISIBLE
                            }
                        } else {
                            Toast.makeText(this, "No available slots for $date", Toast.LENGTH_SHORT)
                                .show()
                            textChooseHour.visibility = View.GONE
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to load available slots", Toast.LENGTH_SHORT)
                            .show()
                        textChooseHour.visibility = View.GONE
                    }

            }

            layoutDates.addView(btn)
        }

        dialog.show()
    }


    private fun generateTimeSlots(start: String, end: String, intervalMinutes: Int): List<String> {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTime = format.parse(start) ?: return emptyList()
        val endTime = format.parse(end) ?: return emptyList()

        val slots = mutableListOf<String>()
        val cal = Calendar.getInstance().apply { time = startTime }

        while (cal.time <= endTime) {
            slots.add(format.format(cal.time))
            cal.add(Calendar.MINUTE, intervalMinutes)
        }
        return slots
    }

    private fun saveAppointmentToFirestore(appt: Appointment) {
        val user = auth.currentUser ?: return
        val userId = user.uid

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val name = document.getString("fullName") ?: "Unknown"
                val phone = document.getString("phone") ?: "0000000000"

                val userDocId = "${name}_${phone}".replace(" ", "_")
                val appointmentId = "${appt.date}_${appt.startHour}".replace(" ", "_")

                val docRef = db.collection("appointments")
                    .document(userDocId)
                    .collection("userAppointments")
                    .document(appointmentId)

                val appointmentWithId = appt.copy(
                    id = appointmentId,
                    userId = userId,
                    phone = phone,
                    fullName = name
                )

                docRef.set(appointmentWithId)
                    .addOnSuccessListener {
                        confirmedAppointment = appointmentWithId
                        pendingAppointment = null
                        removeFromAvailableAppointments(appt.date, appt.startHour, appt.durationMinutes)
                        updateAppointmentViews()
                        Toast.makeText(this, "Appointment confirmed!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save appointment", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to retrieve user info", Toast.LENGTH_SHORT).show()
            }
    }





    private fun removeFromAvailableAppointments(date: String, startHour: String, duration: Int) {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTime = format.parse(startHour) ?: return
        val cal = Calendar.getInstance().apply { time = startTime }

        val steps = duration / 30
        val updates = hashMapOf<String, Any>()

        repeat(steps) {
            val slotTime = format.format(cal.time)
            // עדכון השדה isBooked לשם הסלוט המתאים בתוך slots
            updates["slots.$slotTime.isBooked"] = true
            cal.add(Calendar.MINUTE, 30)
        }

        val docRef = db.collection("availableAppointments").document(date)
        docRef.update(updates)
            .addOnSuccessListener {
                Log.d("BookAppointment", "Successfully updated availableAppointments - marked slots as booked")
            }
            .addOnFailureListener { e ->
                Log.e("BookAppointment", "Failed to update availableAppointments", e)
            }
    }

    private fun cancelAppointment(appt: Appointment) {
        val user = auth.currentUser ?: return
        val userId = user.uid

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val name = document.getString("fullName") ?: "Unknown"
                val phone = document.getString("phone") ?: "0000000000"
                val userDocId = "${name}_${phone}".replace(" ", "_")

                db.collection("appointments").document(userDocId)
                    .collection("userAppointments")
                    .document(appt.id)
                    .delete()
                    .addOnSuccessListener {
                        confirmedAppointment = null
                        restoreAvailableAppointments(appt.date, appt.startHour, appt.durationMinutes)
                        updateAppointmentViews()
                        Toast.makeText(this, "Appointment cancelled.", Toast.LENGTH_SHORT).show()
                    }
            }
    }




    private fun restoreAvailableAppointments(date: String, startHour: String, duration: Int) {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTime = format.parse(startHour) ?: return
        val cal = Calendar.getInstance().apply { time = startTime }

        val steps = duration / 30
        val updates = hashMapOf<String, Any>()

        repeat(steps) {
            val slotTime = format.format(cal.time)
            updates["slots.$slotTime.isBooked"] = false
            cal.add(Calendar.MINUTE, 30)
        }

        val docRef = db.collection("availableAppointments").document(date)
        docRef.update(updates)
            .addOnSuccessListener {
                Log.d("BookAppointment", "Successfully restored availableAppointments - marked slots as free")
            }
            .addOnFailureListener { e ->
                Log.e("BookAppointment", "Failed to restore availableAppointments", e)
            }
    }
}
