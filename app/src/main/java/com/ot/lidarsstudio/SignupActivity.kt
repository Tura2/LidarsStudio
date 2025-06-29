// app/src/main/java/com/ot/lidarsstudio/SignupActivity.kt
package com.ot.lidarsstudio

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.ot.lidarsstudio.R
import android.view.View                      // ← added
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        val fullNameEditText = findViewById<EditText>(R.id.editTextFullName)
        val emailEditText    = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val phoneEditText    = findViewById<EditText>(R.id.editTextPhone)
        val dobEditText      = findViewById<EditText>(R.id.editTextDob)
        val signupButton     = findViewById<MaterialButton>(R.id.buttonSignUp)
        val loginText        = findViewById<TextView>(R.id.textLogin)
        val backButton       = findViewById<ImageButton>(R.id.backButton)

        // Date Picker
        dobEditText.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this,
                { _, y, m, d ->
                    dobEditText.setText("%02d/%02d/%04d".format(d, m+1, y))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Sign-up logic
        signupButton.setOnClickListener {
            val name = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val pass  = passwordEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val dob   = dobEditText.text.toString().trim()

            // Validation
            when {
                name.isEmpty() -> fullNameEditText.error = "Required"
                email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    emailEditText.error = "Valid email required"
                pass.length < 6 -> passwordEditText.error = "Min 6 characters"
                phone.isEmpty() -> phoneEditText.error = "Required"
                dob.isEmpty()   -> dobEditText.error = "Required"
                else -> {
                    // Create Auth user
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnSuccessListener { result ->
                            val uid = result.user!!.uid
                            val user = mapOf(
                                "fullName"  to name,
                                "email"     to email,
                                "phone"     to phone,
                                "dob"       to dob,
                                "role"      to "client",
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                            // Write to Firestore
                            db.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener {
                                    // Next: Welcome screen
                                    startActivity(Intent(this, WelcomeActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error saving user: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Signup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        // Back arrow
        backButton.setOnClickListener { finish() }

        // Clickable "Log in"
        val prompt = "Already have an account? Log in"
        val ss = SpannableString(prompt)
        val start = prompt.indexOf("Log in")
        ss.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, prompt.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                finish()
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(this@SignupActivity, R.color.primaryColor)
            }
        }, start, prompt.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        loginText.text = ss
        loginText.movementMethod = LinkMovementMethod.getInstance()
    }
}
