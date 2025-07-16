package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.*

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

    private var isManager = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

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

        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    Log.d("ProfileActivity", "User document loaded: $doc")
                    textUserName.text = doc.getString("fullName") ?: ""
                    textUserEmail.text = doc.getString("email") ?: ""
                    textUserPhone.text = doc.getString("phone") ?: ""
                    textUserBirth.text = doc.getString("dob") ?: ""

                    val email = doc.getString("email") ?: ""
                    val role = doc.getString("role") ?: ""

                    isManager = email == "lidar@example.com" || role == "manager"

                    Log.d("ProfileActivity", "Is Manager? $isManager")

                    if (isManager) {
                        buttonAddAppointments.visibility = View.VISIBLE
                        textUserName.text = "${textUserName.text} (Manager)"
                    }

                    showUpcomingAppointment()
                    buttonToggleUpcoming.isEnabled = false
                    buttonToggleHistory.isEnabled = true
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileActivity", "Failed to load user document", e)
                }
        }

        buttonAddAppointments.setOnClickListener {
            startActivity(Intent(this, AddAvailableAppointmentsActivity::class.java))
        }

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
        Log.d("ProfileActivity", "Loading upcoming appointments")
        appointmentContainer.removeAllViews()
        val view = inflater.inflate(R.layout.layout_upcoming_appointment, appointmentContainer, false)
        appointmentContainer.addView(view)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerAppointments)
        recycler.layoutManager = LinearLayoutManager(this)

        val appointments = mutableListOf<Appointment>()

        val adapter = AppointmentAdapter(
            appointments,
            isManager,
            onCancel = { appt -> cancelAppointment(appt) },
            onComplete = { appt -> completeAppointment(appt) }
        )

        recycler.adapter = adapter

        if (isManager) {
            getScheduledAppointmentsForNextWeek { appts ->
                Log.d("ProfileActivity", "Manager upcoming appointments loaded: ${appts.size}")
                appointments.clear()
                // כאן עידכון: ודא ש־fullName נטען מהמסמכים
                appointments.addAll(appts)
                adapter.notifyDataSetChanged()
            }
        } else {
            val userDocId = "${textUserName.text}_${textUserPhone.text}".replace(" ", "_")
            Log.d("ProfileActivity", "Customer userDocId for appointments: $userDocId")

            db.collection("appointments")
                .document(userDocId)
                .collection("userAppointments")
                .whereEqualTo("status", "scheduled")
                .get()
                .addOnSuccessListener { result ->
                    Log.d("ProfileActivity", "Customer upcoming appointments loaded: ${result.size()}")
                    appointments.clear()
                    for (doc in result.documents) {
                        Log.d("ProfileActivity", "Appointment doc: ${doc.id} data: ${doc.data}")
                        val appt = doc.toObject(Appointment::class.java)?.copy(
                            id = doc.id,
                            userId = userDocId,
                            fullName = doc.getString("fullName") ?: "" // טוען את השם המלא
                        )
                        if (appt != null) appointments.add(appt)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileActivity", "Failed to load upcoming appointments", e)
                }
        }
    }

    private fun showAppointmentHistory() {
        Log.d("ProfileActivity", "Loading appointment history")
        appointmentContainer.removeAllViews()
        val view = inflater.inflate(R.layout.layout_appointment_history, appointmentContainer, false)
        appointmentContainer.addView(view)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerAppointments)
        recycler.layoutManager = LinearLayoutManager(this)

        val appointments = mutableListOf<Appointment>()

        val adapter = AppointmentAdapter(
            appointments,
            isManager,
            onCancel = { appt -> cancelAppointment(appt) },
            onComplete = { appt -> completeAppointment(appt) }
        )

        recycler.adapter = adapter

        if (isManager) {
            getCompletedAppointmentsLastWeek { appts ->
                Log.d("ProfileActivity", "Manager completed appointments loaded: ${appts.size}")
                appointments.clear()
                appointments.addAll(appts)
                adapter.notifyDataSetChanged()
            }
        } else {
            val userDocId = "${textUserName.text}_${textUserPhone.text}".replace(" ", "_")
            Log.d("ProfileActivity", "Customer userDocId for completed appointments: $userDocId")

            db.collection("appointments")
                .document(userDocId)
                .collection("userAppointments")
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener { result ->
                    Log.d("ProfileActivity", "Customer completed appointments loaded: ${result.size()}")
                    appointments.clear()
                    for (doc in result.documents) {
                        Log.d("ProfileActivity", "Appointment doc: ${doc.id} data: ${doc.data}")
                        val appt = doc.toObject(Appointment::class.java)?.copy(
                            id = doc.id,
                            userId = userDocId,
                            fullName = doc.getString("fullName") ?: "" // טוען את השם המלא
                        )
                        if (appt != null) appointments.add(appt)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileActivity", "Failed to load completed appointments", e)
                }
        }
    }

    private fun cancelAppointment(appt: Appointment) {
        val userId = appt.userId ?: run {
            Log.e("ProfileActivity", "No userId in appointment to cancel")
            return
        }
        db.collection("appointments")
            .document(userId)
            .collection("userAppointments")
            .document(appt.id)
            .update("status", "cancelled")
            .addOnSuccessListener {
                Log.d("ProfileActivity", "Appointment cancelled: ${appt.id}")
                showUpcomingAppointment()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Failed to cancel appointment", e)
            }
    }

    private fun completeAppointment(appt: Appointment) {
        val userId = appt.userId ?: run {
            Log.e("ProfileActivity", "No userId in appointment to complete")
            return
        }
        db.collection("appointments")
            .document(userId)
            .collection("userAppointments")
            .document(appt.id)
            .update("status", "completed")
            .addOnSuccessListener {
                Log.d("ProfileActivity", "Appointment completed: ${appt.id}")
                showUpcomingAppointment()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Failed to complete appointment", e)
            }
    }

    // השאר פונקציות ההבאה ללא שינוי כי הן כבר מחזירות Appointment עם fullName

    private fun getScheduledAppointmentsForNextWeek(onResult: (List<Appointment>) -> Unit) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val today = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val nextWeek = sdf.format(calendar.time)

        Log.d("ProfileActivity", "Fetching scheduled appointments from $today to $nextWeek")

        db.collectionGroup("userAppointments")
            .whereEqualTo("status", "scheduled")
            .whereGreaterThanOrEqualTo("date", today)
            .whereLessThanOrEqualTo("date", nextWeek)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("ProfileActivity", "Scheduled appointments fetched: ${querySnapshot.size()}")
                val appointments = mutableListOf<Appointment>()
                for (doc in querySnapshot.documents) {
                    val userId = doc.reference.parent.parent?.id ?: ""
                    val appt = doc.toObject(Appointment::class.java)?.copy(
                        id = doc.id,
                        userId = userId
                        // fullName נטען ישירות דרך toObject
                    )
                    if (appt != null) {
                        appointments.add(appt)
                    }
                }
                onResult(appointments)
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Failed to fetch scheduled appointments", e)
                onResult(emptyList())
            }
    }

    private fun getCompletedAppointmentsLastWeek(onResult: (List<Appointment>) -> Unit) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val lastWeek = sdf.format(calendar.time)
        val today = sdf.format(Calendar.getInstance().time)

        Log.d("ProfileActivity", "Fetching completed appointments from $lastWeek to $today")

        db.collectionGroup("userAppointments")
            .whereEqualTo("status", "completed")
            .whereGreaterThanOrEqualTo("date", lastWeek)
            .whereLessThanOrEqualTo("date", today)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("ProfileActivity", "Completed appointments fetched: ${querySnapshot.size()}")
                val appointments = mutableListOf<Appointment>()
                for (doc in querySnapshot.documents) {
                    val userId = doc.reference.parent.parent?.id ?: ""
                    val appt = doc.toObject(Appointment::class.java)?.copy(
                        id = doc.id,
                        userId = userId
                        // fullName נטען ישירות דרך toObject
                    )
                    if (appt != null) {
                        appointments.add(appt)
                    }
                }
                onResult(appointments)
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Failed to fetch completed appointments", e)
                onResult(emptyList())
            }
    }
}
