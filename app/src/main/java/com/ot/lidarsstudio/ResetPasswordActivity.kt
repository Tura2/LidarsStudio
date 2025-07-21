package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.ot.lidarsstudio.R

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        auth = FirebaseAuth.getInstance()

        val emailEt  = findViewById<EditText>(R.id.editTextEmail)
        val resetBtn = findViewById<MaterialButton>(R.id.buttonReset)
        val signupTv = findViewById<TextView>(R.id.textSignup)

        // Set up clickable "Register now"
        val prompt = "Don't have an account? Register now"
        val start = prompt.indexOf("Register now")
        val ss = android.text.SpannableString(prompt).apply {
            setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                start, prompt.length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(object : android.text.style.ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(
                        Intent(this@ResetPasswordActivity, SignupActivity::class.java)
                    )
                }
                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = ContextCompat.getColor(
                        this@ResetPasswordActivity,
                        R.color.primaryColor
                    )
                }
            }, start, prompt.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        signupTv.text = ss
        signupTv.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        // Reset button logic
        resetBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()

            when {
                email.isEmpty() -> {
                    emailEt.error = "Email required"
                    return@setOnClickListener
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailEt.error = "Invalid email"
                    return@setOnClickListener
                }
                else -> {
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Password reset email sent.",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Navigate back to Login screen
                            startActivity(Intent(this, WelcomeActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to send reset email: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
        }
    }
}
