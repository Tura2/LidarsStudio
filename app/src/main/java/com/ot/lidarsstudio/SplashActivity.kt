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

    // הפונקציה החדשה לקבלת זמן שרת מה-HTTP API
    private fun getServerTime(onResult: (Long) -> Unit) {
        val url = "https://getservertime-fhvdk4ir7a-uc.a.run.app"
        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch server time via HTTP", e)
                onResult(System.currentTimeMillis())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e(TAG, "Unexpected code $response")
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
                            Log.d(TAG, "Got serverTimestamp=$serverTimestamp from HTTP API")
                            onResult(serverTimestamp)
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Failed to parse server time JSON", ex)
                        onResult(System.currentTimeMillis())
                    }
                }
            }
        })
    }

    private fun cleanOldSlots(onComplete: () -> Unit) {
        Log.d(TAG, "cleanOldSlots start")
        getServerTime { serverNowMs ->
            val msInDay = 24L * 60 * 60 * 1000
            val tzOffset = TimeZone.getDefault().getOffset(serverNowMs)
            val localTimeMs = serverNowMs + tzOffset
            val msSinceMidnight = localTimeMs % msInDay
            val todayMidnightMs = serverNowMs - msSinceMidnight
            Log.d(TAG, "todayMidnightMs = $todayMidnightMs (msSinceMidnight=$msSinceMidnight, tzOffset=$tzOffset)")

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            db.collection("availableAppointments")
                .get()
                .addOnSuccessListener { qry ->
                    Log.d(TAG, "fetched ${qry.size()} appointment documents")
                    val batch = db.batch()
                    var done = 0
                    val total = qry.size()

                    if (total == 0) {
                        Log.d(TAG, "no documents to process")
                        onComplete()
                        return@addOnSuccessListener
                    }

                    for (doc in qry.documents) {
                        val dateStr = doc.id
                        Log.d(TAG, "processing doc id='$dateStr'")
                        val date = try {
                            sdf.parse(dateStr)
                        } catch (e: Exception) {
                            null
                        }

                        if (date == null) {
                            Log.d(TAG, "  invalid date format, deleting doc")
                            batch.delete(doc.reference)
                        } else {
                            val dateMs = date.time
                            when {
                                dateMs < todayMidnightMs -> {
                                    Log.d(TAG, "  date $dateStr is before today, deleting doc")
                                    batch.delete(doc.reference)
                                }
                                dateMs == todayMidnightMs -> {
                                    Log.d(TAG, "  date $dateStr is today, checking slots")
                                    val slots = doc.get("slots")
                                            as? Map<String, Map<String, Any>> ?: emptyMap()
                                    Log.d(TAG, "    found ${slots.size} slots")
                                    val updates = mutableMapOf<String, Any?>()

                                    for ((timeStr, _) in slots) {
                                        val parts = timeStr.split(":")
                                        if (parts.size == 2) {
                                            val h = parts[0].toIntOrNull() ?: 0
                                            val m = parts[1].toIntOrNull() ?: 0
                                            val slotMs = todayMidnightMs + h * 3_600_000 + m * 60_000
                                            if (slotMs < serverNowMs) {
                                                Log.d(TAG, "      slot '$timeStr' already passed (slotMs=$slotMs), marking for deletion")
                                                updates["slots.$timeStr"] = null
                                            } else {
                                                Log.d(TAG, "      slot '$timeStr' is still valid (slotMs=$slotMs)")
                                            }
                                        }
                                    }
                                    if (updates.isNotEmpty()) {
                                        Log.d(TAG, "    applying ${updates.size} slot deletions")
                                        batch.update(doc.reference, updates)
                                    } else {
                                        Log.d(TAG, "    no slots to delete")
                                    }
                                }
                                else -> {
                                    Log.d(TAG, "  date $dateStr is in the future, skipping")
                                }
                            }
                        }

                        done++
                        if (done == total) {
                            Log.d(TAG, "all $total documents processed, committing batch")
                            batch.commit()
                                .addOnSuccessListener {
                                    Log.d(TAG, "batch commit succeeded")
                                    onComplete()
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "batch commit failed", e)
                                    onComplete()
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "failed to fetch appointments", e)
                    onComplete()
                }
        }
    }
}
