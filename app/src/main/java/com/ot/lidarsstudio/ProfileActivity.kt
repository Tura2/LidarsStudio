package com.ot.lidarsstudio

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    // UI
    private lateinit var textUserName: TextView
    private lateinit var textUserEmail: TextView
    private lateinit var textUserPhone: TextView
    private lateinit var textUserBirth: TextView

    private lateinit var buttonEditProfile: ImageButton
    private lateinit var buttonToggleUpcoming: Button
    private lateinit var buttonToggleHistory: Button

    private lateinit var appointmentContainer: FrameLayout
    private val inflater by lazy { LayoutInflater.from(this) }

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // bind views
        textUserName    = findViewById(R.id.textUserName)
        textUserEmail   = findViewById(R.id.textUserEmail)
        textUserPhone   = findViewById(R.id.textUserPhone)
        textUserBirth   = findViewById(R.id.textUserBirth)

        buttonEditProfile      = findViewById(R.id.buttonEditProfile)
        buttonToggleUpcoming   = findViewById(R.id.buttonToggleUpcoming)
        buttonToggleHistory    = findViewById(R.id.buttonToggleHistory)
        appointmentContainer   = findViewById(R.id.appointmentContentContainer)

        // 1) Load real profile from Firestore
        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    textUserName.text  = doc.getString("fullName") ?: ""
                    textUserEmail.text = doc.getString("email")    ?: ""
                    textUserPhone.text = doc.getString("phone")    ?: ""
                    textUserBirth.text = doc.getString("dob")      ?: ""
                }
                .addOnFailureListener {
                    // you may want to show an error or fallback text here
                }
        }

        // 2) Default to upcoming
        showUpcomingAppointment()
        buttonToggleUpcoming.isEnabled = false

        // 3) Toggle listeners
        buttonToggleUpcoming.setOnClickListener {
            showUpcomingAppointment()
            buttonToggleUpcoming.isEnabled = false
            buttonToggleHistory.isEnabled  = true
        }

        buttonToggleHistory.setOnClickListener {
            showAppointmentHistory()
            buttonToggleHistory.isEnabled  = false
            buttonToggleUpcoming.isEnabled = true
        }

        // 4) Edit‐profile click (just a placeholder intent)
        buttonEditProfile.setOnClickListener {
            // TODO: launch your EditProfileActivity
            // startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun showUpcomingAppointment() {
        appointmentContainer.removeAllViews()
        val view = inflater.inflate(
            R.layout.layout_upcoming_appointment,
            appointmentContainer,
            false
        )
        appointmentContainer.addView(view)
    }

    private fun showAppointmentHistory() {
        appointmentContainer.removeAllViews()
        val view = inflater.inflate(
            R.layout.layout_appointment_history,
            appointmentContainer,
            false
        )
        appointmentContainer.addView(view)
    }
}
