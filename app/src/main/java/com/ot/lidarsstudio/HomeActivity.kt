package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.android.material.button.MaterialButton
import com.ot.lidarsstudio.adapters.PhotoAdapter
import com.ot.lidarsstudio.utils.ImageFade

class HomeActivity : AppCompatActivity() {

    private lateinit var imageSlider: ImageView
    private lateinit var textWelcome: TextView
    private lateinit var recyclerViewPhotos: RecyclerView

    private val sliderImages = listOf(
        R.drawable.slider_1,
        R.drawable.slider_2,
        R.drawable.slider_3
    )

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        imageSlider      = findViewById(R.id.imageSlider)
        textWelcome      = findViewById(R.id.textWelcome)
        recyclerViewPhotos = findViewById(R.id.recyclerViewPhotos)

        // 1) Fetch current user’s name from Firestore
        val user = auth.currentUser
        if (user != null) {
            db.collection("users")
                .document(user.uid)
                .get(Source.SERVER)
                .addOnSuccessListener { doc ->
                    val fullName = doc.getString("fullName") ?: "there"
                    textWelcome.text = "Hey $fullName, happy to have you back"
                }
                .addOnFailureListener { e ->
                    // fallback text if something goes wrong
                    textWelcome.text = "Hey there, happy to have you back"
                    Toast.makeText(this, "Couldn’t load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // not signed in for some reason
            textWelcome.text = "Hey there, happy to have you back"
        }

        // 2) “Make appointment” button
        findViewById<MaterialButton>(R.id.buttonMakeAppointment)
            .setOnClickListener {
                startActivity(Intent(this, BookAppointmentActivity::class.java))
            }

        // 3) Start fading images
        ImageFade.start(imageSlider, sliderImages, 3000)

        // 4) Catalog RecyclerView
        val photoImages = listOf(
            R.drawable.catalog_1,
            R.drawable.catalog_2,
            R.drawable.catalog_3,
            R.drawable.catalog_4
        )
        recyclerViewPhotos.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewPhotos.adapter = PhotoAdapter(photoImages)

        // 5) Social & accessibility buttons (placeholders)
        findViewById<ImageButton>(R.id.btnInstagram)
            .setOnClickListener { /* TODO: open Instagram */ }
        findViewById<ImageButton>(R.id.btnFacebook)
            .setOnClickListener { /* TODO: open Facebook */ }
        findViewById<ImageButton>(R.id.btnWhatsapp)
            .setOnClickListener { /* TODO: open WhatsApp */ }
        findViewById<ImageButton>(R.id.btnTiktok)
            .setOnClickListener { /* TODO: open TikTok */ }
        findViewById<ImageButton>(R.id.btnAccessibility)
            .setOnClickListener { /* TODO: handle accessibility */ }
    }
}
