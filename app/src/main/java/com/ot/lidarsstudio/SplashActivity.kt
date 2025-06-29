package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = auth.currentUser
            if (user == null) {
                // Not signed in → go to Login
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                // Already signed in → look up role to decide destination
                db.collection("users")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val role = doc.getString("role")
                        val nextCls = when (role) {
                         //   "admin"  -> AdminActivity::class.java
                            else     -> HomeActivity::class.java
                        }
                        startActivity(Intent(this, nextCls))
                        finish()
                    }
                    .addOnFailureListener {
                        // Fallback to Home on error
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
            }
        }, 2000)
    }
}
