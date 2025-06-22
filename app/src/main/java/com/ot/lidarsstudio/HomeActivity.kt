package com.ot.lidarsstudio

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        imageSlider = findViewById(R.id.imageSlider)
        textWelcome = findViewById(R.id.textWelcome)
        recyclerViewPhotos = findViewById(R.id.recyclerViewPhotos)

        val name = intent.getStringExtra("userName") ?: "there"
        textWelcome.text = "Hey $name, happy to have you back"

        // Start fading images
        ImageFade.start(imageSlider, sliderImages, 3000)

        val photoImages = listOf(
            R.drawable.catalog_1,
            R.drawable.catalog_2,
            R.drawable.catalog_3,
            R.drawable.catalog_4
        )
        recyclerViewPhotos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewPhotos.adapter = PhotoAdapter(photoImages)

        findViewById<ImageButton>(R.id.btnInstagram).setOnClickListener {
            // TODO: open Instagram
        }
        findViewById<ImageButton>(R.id.btnFacebook).setOnClickListener {
            // TODO: open Facebook
        }
        findViewById<ImageButton>(R.id.btnWhatsapp).setOnClickListener {
            // TODO: open WhatsApp
        }
        findViewById<ImageButton>(R.id.btnTiktok).setOnClickListener {
            // TODO: open TikTok
        }
        findViewById<ImageButton>(R.id.btnAccessibility).setOnClickListener {
            // TODO: handle accessibility
        }
    }
}
