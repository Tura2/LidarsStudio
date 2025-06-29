package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // TODO: wire up your buttons or side‐sheet menu here.
        // Here’s how you would fire an intent – uncomment and replace with your actual targets:

        // findViewById<Button>(R.id.buttonToggleAppointments).setOnClickListener {
        //     startActivity(Intent(this, AppointmentsActivity::class.java))
        // }

        // findViewById<Button>(R.id.buttonToggleAvailability).setOnClickListener {
        //     startActivity(Intent(this, AvailabilityActivity::class.java))
        // }
    }
}
