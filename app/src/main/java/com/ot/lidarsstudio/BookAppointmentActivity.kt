package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ot.lidarsstudio.utils.Appointment
import java.text.SimpleDateFormat
import java.util.*

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
            startActivity(Intent(this, TattooRequestActivity::class.java))
        }

        btnConfirmNow.setOnClickListener {
            pendingAppointment?.let { saveAppointmentToFirestore(it) }
        }

        btnCancel.setOnClickListener {
            confirmedAppointment?.let { cancelAppointment(it) }
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
        val layoutActiveAppointmentBlock = findViewById<LinearLayout>(R.id.layoutActiveAppointmentBlock)
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
        view.findViewById<ImageButton>(R.id.btnClose)
            .setOnClickListener { dialog.dismiss() }

        options.forEach { (id, name) ->
            view.findViewById<MaterialButton>(id).setOnClickListener {
                selectedServiceName = name
                dialog.dismiss()
                showDateTimePicker()
            }
        }
        view.findViewById<ImageButton>(R.id.btnClose).setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showDateTimePicker() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_select_date_time, null)
        dialog.setContentView(view)
        view.findViewById<ImageButton>(R.id.btnCloseDateTime)
            .setOnClickListener { dialog.dismiss() }

        val layoutDates = view.findViewById<LinearLayout>(R.id.layoutDates)
        val layoutHours = view.findViewById<GridLayout>(R.id.layoutHours)
        val textChooseHour = view.findViewById<TextView>(R.id.textChooseHour)
        var selectedDateBtn: MaterialButton? = null

        layoutDates.removeAllViews()
        fetchAvailableDates(
            onResult = { dates ->
                dates.forEach { dateId ->
                    // format yyyy-MM-dd → d.M
                    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateId)
                    val display = parsed?.let { SimpleDateFormat("d.M", Locale.getDefault()).format(it) } ?: dateId

                    val btn = MaterialButton(this).apply {
                        text = display
                        setPadding(36, 12, 36, 12)
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                        backgroundTintList = ContextCompat.getColorStateList(context, R.color.primaryColor)
                    }

                    btn.setOnClickListener {
                        selectedDateBtn?.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.secondaryColor
                        )
                        btn.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.primaryColor
                        )
                        selectedDateBtn = btn
                        selectedDate = dateId

                        layoutHours.removeAllViews()
                        textChooseHour.visibility = View.GONE
                        loadAndDisplaySlotsFor(dateId, view, layoutHours, textChooseHour, dialog)
                    }

                    layoutDates.addView(btn)
                }
            },
            onError = {
                Toast.makeText(this, "Failed to load dates", Toast.LENGTH_SHORT).show()
            }
        )

        dialog.show()
    }

    // Fetches all document IDs under availableAppointments
    private fun fetchAvailableDates(onResult: (List<String>) -> Unit, onError: () -> Unit) {
        db.collection("availableAppointments")
            .get()
            .addOnSuccessListener { snapshot ->
                val dates = snapshot.documents.map { it.id }.sorted()
                onResult(dates)
            }
            .addOnFailureListener {
                onError()
            }
    }

    // Loads slots for a specific date and populates hour buttons
    private fun loadAndDisplaySlotsFor(
        date: String,
        bottomSheetView: View,
        layoutHours: GridLayout,
        textChooseHour: TextView,
        dialog: BottomSheetDialog
    ) {
        db.collection("availableAppointments")
            .document(date)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "No slots for $date", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val slotsMap = doc.get("slots") as? Map<String, Any> ?: emptyMap()
                val available = slotsMap.filter { (_, v) ->
                    (v as? Map<*, *>)?.get("isBooked") == false
                }.keys.sorted()

                if (available.isEmpty()) {
                    Toast.makeText(this, "No available slots for $date", Toast.LENGTH_SHORT).show()
                    textChooseHour.visibility = View.GONE
                    bottomSheetView.findViewById<ScrollView>(R.id.scrollHours).visibility = View.GONE
                    return@addOnSuccessListener
                }

                layoutHours.removeAllViews()
                available.forEach { time ->
                    Log.d("BookAppointment", "Adding button for $time")
                    val timeBtn = MaterialButton(this).apply {
                        text = time
                        setPadding(36, 12, 36, 12)
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                        backgroundTintList = ContextCompat.getColorStateList(
                            context, R.color.primaryColor
                        )
                    }

                    timeBtn.setOnClickListener {
                        selectedHour = time
                        val duration = getDurationForService(selectedServiceName)
                        pendingAppointment = Appointment(
                            id = "",
                            service = selectedServiceName,
                            date = selectedDate,
                            startHour = selectedHour,
                            durationMinutes = duration,
                            status = "scheduled"
                        )
                        dialog.dismiss()
                        updateAppointmentViews()
                    }

                    layoutHours.addView(timeBtn)
                }

                textChooseHour.visibility = View.VISIBLE
                bottomSheetView.findViewById<ScrollView>(R.id.scrollHours).visibility = View.VISIBLE
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load slots", Toast.LENGTH_SHORT).show()
                textChooseHour.visibility = View.GONE
            }
    }

    private fun saveAppointmentToFirestore(appt: Appointment) {
        val user = auth.currentUser ?: return
        val userId = user.uid

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("fullName") ?: "Unknown"
                val phone = doc.getString("phone") ?: "0000000000"
                val userDocId = "${name}_${phone}".replace(" ", "_")
                val apptId     = "${appt.date}_${appt.startHour}".replace(" ", "_")


                val appointmentWithId = appt.copy(
                    id = apptId,
                    userId = userId,
                    phone = phone,
                    fullName = name
                )

                db.collection("appointments")
                    .document(userDocId)
                    .collection("userAppointments")
                    .document(apptId)
                    .set(appointmentWithId)
                    .addOnSuccessListener {
                        confirmedAppointment = appointmentWithId
                        pendingAppointment   = null
                        removeFromAvailableAppointments(
                            appt.date,
                            appt.startHour,
                            appt.durationMinutes
                        )
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
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = fmt.parse(startHour) ?: return
        val cal = Calendar.getInstance().apply { time = start }
        val steps = duration / 30
        val updates = hashMapOf<String, Any>()

        repeat(steps) {
            val slot = fmt.format(cal.time)
            updates["slots.$slot.isBooked"] = true
            cal.add(Calendar.MINUTE, 30)
        }

        db.collection("availableAppointments")
            .document(date)
            .update(updates)
            .addOnSuccessListener {
                Log.d("BookAppointment", "Slots marked booked")
            }
            .addOnFailureListener { e -> Log.e("BookAppointment", "Failed to update slots", e) }
    }

    private fun cancelAppointment(appt: Appointment) {
        val user = auth.currentUser ?: return
        val userId = user.uid

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                doc.getString("fullName") ?: "Unknown"
                doc.getString("phone") ?: "0000000000"
                val userDocId = "${'$'}{name}_{'$'}{phone}".replace(" ", "_")

                db.collection("appointments")
                    .document(userDocId)
                    .collection("userAppointments")
                    .document(appt.id)
                    .delete()
                    .addOnSuccessListener {
                        confirmedAppointment = null
                        restoreAvailableAppointments(
                            appt.date,
                            appt.startHour,
                            appt.durationMinutes
                        )
                        updateAppointmentViews()
                        Toast.makeText(this, "Appointment cancelled.", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun restoreAvailableAppointments(date: String, startHour: String, duration: Int) {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = fmt.parse(startHour) ?: return
        val cal = Calendar.getInstance().apply { time = start }
        val steps = duration / 30
        val updates = hashMapOf<String, Any>()

        repeat(steps) {
            val slot = fmt.format(cal.time)
            updates["slots.$slot.isBooked"] = false
            cal.add(Calendar.MINUTE, 30)
        }

        db.collection("availableAppointments")
            .document(date)
            .update(updates)
            .addOnSuccessListener {
                Log.d("BookAppointment", "Slots restored free")
            }
            .addOnFailureListener { e -> Log.e("BookAppointment", "Failed to restore slots", e) }
    }
}
