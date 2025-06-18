package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start from WelcomeActivity directly
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}