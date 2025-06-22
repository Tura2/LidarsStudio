package com.ot.lidarsstudio

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

        // Sign up button action
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

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Make "Log in" in textLogin look like a link
        val fullText = "Already have an account? Log in"
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf("Log in")
        val end = start + "Log in".length

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, android.R.color.black)),
            start, end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            start, end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false // No underline
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        loginText.text = spannable
        loginText.movementMethod = LinkMovementMethod.getInstance()
    }
}
