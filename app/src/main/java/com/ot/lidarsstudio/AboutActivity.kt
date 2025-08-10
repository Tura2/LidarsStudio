package com.ot.lidarsstudio

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.core.view.GravityCompat
import com.ot.lidarsstudio.BaseActivity

class AboutActivity : BaseActivity() {

    private lateinit var buttonGoogleMaps: Button
    private lateinit var buttonWaze: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Wire up the drawer
        setupDrawer()

        buttonGoogleMaps = findViewById(R.id.buttonGoogleMaps)
        buttonWaze       = findViewById(R.id.buttonWaze)

        buttonGoogleMaps.setOnClickListener {


            val address = Uri.encode("שמורת נחל בניאס 7 נתניה")
            val gmmUri = Uri.parse("geo:0,0?q=$address")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            try {
                startActivity(mapIntent)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://maps.google.com/?q=$address")
                    )
                )
            }
        }

        buttonWaze.setOnClickListener {
            val address = Uri.encode("שמורת נחל בניאס 7 נתניה")

            // Waze URL
            val wazeUri = Uri.parse("https://waze.com/ul?q=$address")
            val wazeIntent = Intent(Intent.ACTION_VIEW, wazeUri).apply {
                setPackage("com.waze")
            }
            try {
                startActivity(wazeIntent)
            } catch (e: ActivityNotFoundException) {
                // fallback to web Waze if the app isn’t present
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.waze.com/ul?q=$address")
                    )
                )
            }
        }
    }
}
