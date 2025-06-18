package com.ot.lidarsstudio

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.editTextUsername)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val signupTextView = findViewById<TextView>(R.id.textSignup)

        // עיצוב הטקסט "Don't have an account? Register now"
        val fullText = "Don't have an account? Register now"
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf("Register now")
        val end = start + "Register now".length


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
                startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        signupTextView.text = spannable
        signupTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}
