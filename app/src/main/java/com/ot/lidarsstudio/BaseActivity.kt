package com.ot.lidarsstudio

import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.ZoneId
import java.util.*

abstract class BaseActivity : AppCompatActivity() {

    private val TAG = "BaseActivity"

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navView: NavigationView
    protected lateinit var buttonHamburger: ImageButton

    protected fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        buttonHamburger = findViewById(R.id.buttonHamburger)

        buttonHamburger.setOnClickListener {
            Log.d(TAG, "Opening drawer")
            drawerLayout.openDrawer(GravityCompat.START)
        }

        updateGreetingHeader()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home      -> if (this !is HomeActivity) startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_profile   -> if (this !is ProfileActivity) startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_gallery   -> if (this !is GalleryActivity) startActivity(Intent(this, GalleryActivity::class.java))
                R.id.nav_price_list-> if (this !is PriceListActivity) startActivity(Intent(this, PriceListActivity::class.java))
                R.id.nav_about     -> if (this !is AboutActivity) startActivity(Intent(this, AboutActivity::class.java))
                R.id.nav_logout    -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finishAffinity()
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun updateGreetingHeader() {
        val header = navView.getHeaderView(0)
        val tvGreeting = header.findViewById<TextView>(R.id.textGreeting)
        val tvName     = header.findViewById<TextView>(R.id.textFullName)

        Log.d(TAG, "Updating header…")
        getServerTime { ts ->
            Log.d(TAG, "Server time: $ts")
            val greeting = getGreetingByTime(ts)
            fetchUserFullName { name ->
                runOnUiThread {
                    tvGreeting.text = greeting
                    tvName.text     = name
                    Log.d(TAG, "Header updated")
                }
            }
        }
    }

    private fun getGreetingByTime(serverTimestamp: Long): String {
        val hour = Instant.ofEpochMilli(serverTimestamp)
            .atZone(ZoneId.systemDefault())
            .hour
        return when (hour) {
            in 5..11  -> "Good Morning,"
            in 12..17 -> "Good Afternoon,"
            else      -> "Good Evening,"
        }
    }

    private fun getServerTime(onResult: (Long) -> Unit) {
        Firebase.functions
            .getHttpsCallable("getServerTime")
            .call()
            .addOnSuccessListener { result ->
                val ts = (result.data as? Map<*, *>)?.get("serverTimestamp") as? Number
                onResult(ts?.toLong() ?: System.currentTimeMillis())
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to fetch server time", it)
                onResult(System.currentTimeMillis())
            }
    }

    private fun fetchUserFullName(onResult: (String) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.uid
            ?.let { uid ->
                FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .get()
                    .addOnSuccessListener {
                        onResult(it.getString("fullName") ?: "User")
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Failed to fetch name", it)
                        onResult("User")
                    }
            } ?: onResult("User")
    }
}
