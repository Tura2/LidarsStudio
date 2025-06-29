package com.ot.lidarsstudio

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var textUserName: TextView
    private lateinit var textUserEmail: TextView
    private lateinit var textUserPhone: TextView
    private lateinit var textUserBirth: TextView

    private lateinit var buttonEditProfile: ImageButton
    private lateinit var buttonToggleUpcoming: Button
    private lateinit var buttonToggleHistory: Button

    private lateinit var appointmentContainer: FrameLayout

    private val inflater by lazy { LayoutInflater.from(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // קישור לרכיבי המסך
        textUserName = findViewById(R.id.textUserName)
        textUserEmail = findViewById(R.id.textUserEmail)
        textUserPhone = findViewById(R.id.textUserPhone)
        textUserBirth = findViewById(R.id.textUserBirth)

        buttonEditProfile = findViewById(R.id.buttonEditProfile)
        buttonToggleUpcoming = findViewById(R.id.buttonToggleUpcoming)
        buttonToggleHistory = findViewById(R.id.buttonToggleHistory)
        appointmentContainer = findViewById(R.id.appointmentContentContainer)

        // נתוני דמו
        textUserName.text = "Lidar Levi"
        textUserEmail.text = "lidar@example.com"
        textUserPhone.text = "+972-54-0000000"
        textUserBirth.text = "03/04/1997"

        // ברירת מחדל - תור קרוב
        showUpcomingAppointment()

        // מאזינים ללחצנים
        buttonToggleUpcoming.setOnClickListener {
            showUpcomingAppointment()
        }

        buttonToggleHistory.setOnClickListener {
            showAppointmentHistory()
        }
    }

    private fun showUpcomingAppointment() {
        appointmentContainer.removeAllViews()
        val view = inflater.inflate(R.layout.layout_upcoming_appointment, appointmentContainer, false)
        appointmentContainer.addView(view)
    }

    private fun showAppointmentHistory() {
        appointmentContainer.removeAllViews()
        val view = inflater.inflate(R.layout.layout_appointment_history, appointmentContainer, false)
        appointmentContainer.addView(view)
    }
}
