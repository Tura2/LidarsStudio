package com.ot.lidarsstudio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class PriceListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_price_list)

        // Instagram
        findViewById<ImageButton>(R.id.btnInstagram).setOnClickListener {
            openLink("https://www.instagram.com/lidartura_nails")
        }

        // WhatsApp
        findViewById<ImageButton>(R.id.btnWhatsapp).setOnClickListener {
            openLink("https://wa.link/mjh65g")
        }

        // Facebook
        findViewById<ImageButton>(R.id.btnFacebook).setOnClickListener {
            openLink("https://www.facebook.com/lidartura")
        }

        // TikTok
        findViewById<ImageButton>(R.id.btnTiktok).setOnClickListener {
            openLink("https://www.tiktok.com/@lidartura")
        }

    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
