package com.ot.lidarsstudio

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editTextFullName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var editTextDob: EditText
    private lateinit var buttonUpdateProfile: MaterialButton
    private lateinit var backButton: ImageButton

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile) // שם הקובץ שלך

        editTextFullName = findViewById(R.id.editTextFullName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextDob = findViewById(R.id.editTextDob)
        buttonUpdateProfile = findViewById(R.id.buttonUpdateProfile)
        backButton = findViewById(R.id.backButton)

        // טען נתוני משתמש
        loadUserData()

        // תאריך לידה - פתיחת DatePicker
        editTextDob.setOnClickListener {
            showDatePicker()
        }

        // כפתור עדכון פרופיל
        buttonUpdateProfile.setOnClickListener {
            updateUserProfile()
        }

        // כפתור חזרה לסגירת המסך
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    editTextFullName.setText(doc.getString("fullName") ?: "")
                    editTextEmail.setText(doc.getString("email") ?: "")
                    editTextPhone.setText(doc.getString("phone") ?: "")
                    editTextDob.setText(doc.getString("dob") ?: "")
                    editTextEmail.isEnabled = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileActivity", "Failed to load user data", e)
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val currentText = editTextDob.text.toString()
        if (currentText.isNotEmpty()) {
            try {
                val date = sdf.parse(currentText)
                date?.let {
                    calendar.time = it
                }
            } catch (_: Exception) {
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, y, m, d ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(y, m, d)
            editTextDob.setText(sdf.format(selectedDate.time))
        }, year, month, day)

        dpd.show()
    }

    private fun updateUserProfile() {
        val fullName = editTextFullName.text.toString().trim()
        val phone = editTextPhone.text.toString().trim()
        val dob = editTextDob.text.toString().trim()

        if (fullName.isEmpty()) {
            editTextFullName.error = "Full name is required"
            return
        }
        if (phone.isEmpty()) {
            editTextPhone.error = "Phone is required"
            return
        }
        if (dob.isEmpty()) {
            editTextDob.error = "Date of birth is required"
            return
        }

        val user = auth.currentUser ?: return
        val userDoc = db.collection("users").document(user.uid)

        // קודם נקבל את ה־userDocId הישן (מבוסס על שם וטלפון ישנים)
        userDoc.get().addOnSuccessListener { doc ->
            if (doc != null && doc.exists()) {
                val oldFullName = doc.getString("fullName") ?: ""
                val oldPhone = doc.getString("phone") ?: ""
                val oldUserDocId = "${oldFullName}_${oldPhone}".replace(" ", "_")
                val newUserDocId = "${fullName}_${phone}".replace(" ", "_")

                val updates = hashMapOf<String, Any>(
                    "fullName" to fullName,
                    "phone" to phone,
                    "dob" to dob
                )

                // מעדכנים את הפרופיל
                userDoc.update(updates).addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

                    if (oldUserDocId != newUserDocId) {
                        val oldAppointmentsRef = db.collection("appointments").document(oldUserDocId).collection("userAppointments")
                        val newAppointmentsRef = db.collection("appointments").document(newUserDocId).collection("userAppointments")

                        oldAppointmentsRef.get().addOnSuccessListener { snapshot ->
                            val batch = db.batch()

                            for (docAppointment in snapshot.documents) {
                                val newDocRef = newAppointmentsRef.document(docAppointment.id)
                                val data = docAppointment.data ?: continue
                                data["fullName"] = fullName // עדכון השם החדש בתור
                                batch.set(newDocRef, data) // יוצרים עותק חדש בנתיב החדש
                                batch.delete(docAppointment.reference) // מוחקים את הישן
                            }

                            batch.commit().addOnSuccessListener {
                                finish()
                            }.addOnFailureListener { e ->
                                Log.e("EditProfileActivity", "Failed to move appointments to new userDocId", e)
                                finish()
                            }
                        }.addOnFailureListener { e ->
                            Log.e("EditProfileActivity", "Failed to get old appointments", e)
                            finish()
                        }
                    } else {
                        finish()
                    }
                }.addOnFailureListener { e ->
                    Log.e("EditProfileActivity", "Failed to update profile", e)
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { e ->
            Log.e("EditProfileActivity", "Failed to load old user data", e)
            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
        }
    }



}
