package com.ot.lidarsstudio

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"
    private val SPLASH_DELAY = 2_000L
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val httpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        cleanOldSlots {
            Log.d(TAG, "cleanOldSlots completed – delaying navigation by $SPLASH_DELAY ms")
            Handler(Looper.getMainLooper()).postDelayed({
                proceedNextScreen()
            }, SPLASH_DELAY)
        }
    }

    private fun proceedNextScreen() {
        val next = if (auth.currentUser == null) {
            WelcomeActivity::class.java
        } else {
            HomeActivity::class.java
        }
        startActivity(Intent(this, next))
        finish()
    }
// Fetch server time via HTTP request
    private fun getServerTime(onResult: (Long) -> Unit) {
        val url = "https://getservertime-fhvdk4ir7a-uc.a.run.app"
        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch server time via HTTP")
                onResult(System.currentTimeMillis())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e(TAG, "Unexpected response code from server time HTTP")
                        onResult(System.currentTimeMillis())
                        return
                    }
                    val bodyStr = it.body?.string()
                    if (bodyStr == null) {
                        Log.e(TAG, "Empty response body from server time HTTP")
                        onResult(System.currentTimeMillis())
                        return
                    }
                    try {
                        val json = JSONObject(bodyStr)
                        val serverTimestamp = json.optLong("serverTimestamp", -1L)
                        if (serverTimestamp == -1L) {
                            Log.e(TAG, "Invalid serverTimestamp in response JSON")
                            onResult(System.currentTimeMillis())
                        } else {
                            onResult(serverTimestamp)
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Failed to parse server time JSON")
                        onResult(System.currentTimeMillis())
                    }
                }
            }
        })
    }
// Clean up old appointment slots
    // This function removes slots that are older than today or past slots for today
    // It uses the server time to ensure consistency across different devices
    // It also removes entire documents if they have no slots left after cleanup
    // This is run on app startup to keep the database clean
    private fun cleanOldSlots(onComplete: () -> Unit) {
        Log.d(TAG, "Starting cleanOldSlots")
        getServerTime { serverNowMs ->
            val msInDay = 24L * 60 * 60 * 1000
            val tzOffset = TimeZone.getDefault().getOffset(serverNowMs)
            val localTimeMs = serverNowMs + tzOffset
            val msSinceMidnight = localTimeMs % msInDay
            val todayMidnightMs = serverNowMs - msSinceMidnight

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            db.collection("availableAppointments")
                .get()
                .addOnSuccessListener { qry ->
                    val batch = db.batch()
                    var done = 0
                    val total = qry.size()

                    if (total == 0) {
                        Log.d(TAG, "No appointment documents to process")
                        onComplete()
                        return@addOnSuccessListener
                    }

                    for (doc in qry.documents) {
                        val dateStr = doc.id
                        val date = try {
                            sdf.parse(dateStr)
                        } catch (e: Exception) {
                            null
                        }

                        if (date == null) {
                            batch.delete(doc.reference)
                        } else {
                            val dateMs = date.time
                            when {
                                dateMs < todayMidnightMs -> batch.delete(doc.reference)
                                dateMs == todayMidnightMs -> {
                                    val slots = doc.get("slots") as? Map<String, Map<String, Any>> ?: emptyMap()
                                    val updates = mutableMapOf<String, Any?>()
                                    for ((timeStr, _) in slots) {
                                        val parts = timeStr.split(":")
                                        if (parts.size == 2) {
                                            val h = parts[0].toIntOrNull() ?: 0
                                            val m = parts[1].toIntOrNull() ?: 0
                                            val slotMs = todayMidnightMs + h * 3_600_000 + m * 60_000
                                            if (slotMs < serverNowMs) {
                                                updates["slots.$timeStr"] = null
                                            }
                                        }
                                    }
                                    if (updates.isNotEmpty()) batch.update(doc.reference, updates)
                                }
                            }
                        }

                        done++
                        if (done == total) {
                            batch.commit()
                                .addOnSuccessListener { onComplete() }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Batch commit failed", e)
                                    onComplete()
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to fetch appointments", e)
                    onComplete()
                }
        }
    }
}
