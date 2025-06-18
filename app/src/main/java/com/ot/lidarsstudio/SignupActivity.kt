package com.ot.lidarsstudio

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.*

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val fullNameEditText = findViewById<EditText>(R.id.editTextFullName)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val phoneEditText = findViewById<EditText>(R.id.editTextPhone)
        val dobEditText = findViewById<EditText>(R.id.editTextDob)
        val signupButton = findViewById<MaterialButton>(R.id.buttonSignUp)
        val loginText = findViewById<TextView>(R.id.textLogin)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Date Picker for DOB
        dobEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                dobEditText.setText(date)
            }, year, month, day)

            datePicker.show()
        }

        // Sign up button
        signupButton.setOnClickListener {
            val name = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val dob = dobEditText.text.toString().trim()

            // Here you'd add validation and Firebase logic if needed
            Toast.makeText(this, "Signed up as $name", Toast.LENGTH_SHORT).show()
                // startActivity(Intent(this, ClientHomeActivity::class.java))
        }

        // Navigate to login
        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Go back
        backButton.setOnClickListener {
            finish()
        }
    }
}