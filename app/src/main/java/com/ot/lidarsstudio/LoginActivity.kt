package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        val emailEt  = findViewById<EditText>(R.id.editTextUsername)
        val passEt   = findViewById<EditText>(R.id.editTextPassword)
        val loginBtn = findViewById<Button>(R.id.buttonLogin)
        val signupTv = findViewById<TextView>(R.id.textSignup)
        // Clickable "Forgot Password?"
        findViewById<TextView>(R.id.textForgotPassword).setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        // Clickable "Register now"
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
                        Intent(
                            this@LoginActivity,
                            SignupActivity::class.java
                        )
                    )
                }
                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = ContextCompat.getColor(
                        this@LoginActivity,
                        R.color.primaryColor
                    )
                }
            }, start, prompt.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        signupTv.text = ss
        signupTv.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        // Login button logic
        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pass  = passEt.text.toString().trim()

            when {
                email.isEmpty() -> {
                    emailEt.error = "Email required"
                    return@setOnClickListener
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailEt.error = "Invalid email"
                    return@setOnClickListener
                }
                pass.length < 6 -> {
                    passEt.error = "Password too short"
                    return@setOnClickListener
                }
                else -> {
                    auth.signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener { result ->
                            val uid = result.user!!.uid
                            db.collection("users")
                                .document(uid)
                                .get(Source.SERVER)
                                .addOnSuccessListener { doc ->
                                    val role = doc.getString("role")
                                    val nextCls = if (role == "admin")
                                        HomeActivity::class.java
                                    else
                                        HomeActivity::class.java

                                    startActivity(Intent(this, nextCls))
                                    finish()
                                }
                                .addOnFailureListener {
                                    // fallback to Home on error
                                    startActivity(
                                        Intent(
                                            this,
                                            HomeActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Login failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }
    }
}
