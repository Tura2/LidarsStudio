package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ot.lidarsstudio.adapters.AppointmentAdapter
import com.ot.lidarsstudio.utils.Appointment

class ProfileActivity : AppCompatActivity() {

    private lateinit var textUserName: TextView
    private lateinit var textUserEmail: TextView
    private lateinit var textUserPhone: TextView
    private lateinit var textUserBirth: TextView

    private lateinit var buttonEditProfile: ImageButton
    private lateinit var buttonToggleUpcoming: Button
    private lateinit var buttonToggleHistory: Button
    private lateinit var buttonAddAppointments: Button

    private lateinit var appointmentContainer: FrameLayout
    private val inflater by lazy { LayoutInflater.from(this) }

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // bind views
        textUserName = findViewById(R.id.textUserName)
        textUserEmail = findViewById(R.id.textUserEmail)
        textUserPhone = findViewById(R.id.textUserPhone)
        textUserBirth = findViewById(R.id.textUserBirth)

        buttonEditProfile = findViewById(R.id.buttonEditProfile)
        buttonToggleUpcoming = findViewById(R.id.buttonToggleUpcoming)
        buttonToggleHistory = findViewById(R.id.buttonToggleHistory)
        buttonAddAppointments = findViewById(R.id.buttonAddAppointments)
        appointmentContainer = findViewById(R.id.appointmentContentContainer)

        buttonAddAppointments.visibility = View.GONE

        // Load profile
        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    textUserName.text = doc.getString("fullName") ?: ""
                    textUserEmail.text = doc.getString("email") ?: ""
                    textUserPhone.text = doc.getString("phone") ?: ""
                    textUserBirth.text = doc.getString("dob") ?: ""

                    val email = doc.getString("email") ?: ""
                    if (email == "lidar@example.com") {
                        buttonAddAppointments.visibility = View.VISIBLE
                    }
                }
        }

        buttonAddAppointments.setOnClickListener {
            startActivity(Intent(this, AddAvailableAppointmentsActivity::class.java))
        }

        showUpcomingAppointment()
        buttonToggleUpcoming.isEnabled = false

        buttonToggleUpcoming.setOnClickListener {
            showUpcomingAppointment()
            buttonToggleUpcoming.isEnabled = false
            buttonToggleHistory.isEnabled = true
        }

        buttonToggleHistory.setOnClickListener {
            showAppointmentHistory()
            buttonToggleUpcoming.isEnabled = true
            buttonToggleHistory.isEnabled = false
        }

        buttonEditProfile.setOnClickListener {
            // TODO: startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun showUpcomingAppointment() {
        appointmentContainer.removeAllViews()
        val view = inflater.inflate(R.layout.layout_upcoming_appointment, appointmentContainer, false)
        appointmentContainer.addView(view)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerAppointments)
        recycler.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid ?: return
        val appointments = mutableListOf<Appointment>()

        val adapter = AppointmentAdapter(
            appointments,
            onCancel = { appt -> cancelAppointment(appt) },
            onConfirm = { appt -> confirmAppointment(appt) }
        )
        recycler.adapter = adapter

        db.collection("appointments")
            .document(userId)
            .collection("userAppointments")
            .whereEqualTo("status", "scheduled")
            .get()
            .addOnSuccessListener { result ->
                appointments.clear()
                for (doc in result.documents) {
                    val appt = doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                    if (appt != null) appointments.add(appt)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun showAppointmentHistory() {
        appointmentContainer.removeAllViews()
        val view = inflater.inflate(R.layout.layout_appointment_history, appointmentContainer, false)
        appointmentContainer.addView(view)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerAppointments)
        recycler.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid ?: return
        val appointments = mutableListOf<Appointment>()

        val adapter = AppointmentAdapter(
            appointments,
            onCancel = {},  // לא נדרש היסטורית
            onConfirm = {}  // לא נדרש היסטורית
        )
        recycler.adapter = adapter

        db.collection("appointments")
            .document(userId)
            .collection("userAppointments")
            .whereEqualTo("status", "past")
            .get()
            .addOnSuccessListener { result ->
                appointments.clear()
                for (doc in result.documents) {
                    val appt = doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                    if (appt != null) appointments.add(appt)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun cancelAppointment(appt: Appointment) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("appointments")
            .document(userId)
            .collection("userAppointments")
            .document(appt.id)
            .update("status", "cancelled")
            .addOnSuccessListener { showUpcomingAppointment() }
    }

    private fun confirmAppointment(appt: Appointment) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("appointments")
            .document(userId)
            .collection("userAppointments")
            .document(appt.id)
            .update("status", "scheduled")
            .addOnSuccessListener { showUpcomingAppointment() }
    }
}
