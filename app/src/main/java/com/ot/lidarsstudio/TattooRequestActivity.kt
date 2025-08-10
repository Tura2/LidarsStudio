package com.ot.lidarsstudio

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class TattooRequestActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PICK_IMAGES = 100
        private const val MAX_IMAGES = 3
    }

    private lateinit var backButton: ImageButton
    private lateinit var spinnerSize: Spinner
    private lateinit var etConcept: EditText
    private lateinit var etDescription: EditText
    private lateinit var imageContainer: LinearLayout
    private lateinit var btnAddImage: ImageButton
    private lateinit var btnSubmit: Button

    private val selectedImageUris = mutableListOf<Uri>()
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child("tattoo_requests")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tattoo_request)

        backButton     = findViewById(R.id.backButton)
        spinnerSize    = findViewById(R.id.spinnerSize)
        etConcept      = findViewById(R.id.etConcept)
        etDescription  = findViewById(R.id.etDescription)
        imageContainer = findViewById(R.id.imageContainer)
        btnAddImage    = findViewById(R.id.btnAddImage)
        btnSubmit      = findViewById(R.id.btnSubmitTattoo)

        backButton.setOnClickListener { finish() }

        btnAddImage.setOnClickListener {
            if (selectedImageUris.size >= MAX_IMAGES) {
                Toast.makeText(this, "You can attach up to $MAX_IMAGES images.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val pickIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(Intent.createChooser(pickIntent, "Select images"), REQUEST_CODE_PICK_IMAGES)
        }

        btnSubmit.setOnClickListener {
            val size = spinnerSize.selectedItem as String
            val concept = etConcept.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (concept.isEmpty()) {
                etConcept.error = "Please enter a concept title"
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                etDescription.error = "Please enter a detailed description"
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val userId = user.uid

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val userName = document.getString("fullName") ?: "Unknown User"
                    val userEmail = user.email ?: "unknown@example.com"
                    val userPhone = document.getString("phone") ?: "0500000000"

                    SubmissionStatusActivity.startLoading(this@TattooRequestActivity)

                    if (selectedImageUris.isEmpty()) {
                        sendTattooRequest(size, concept, description, emptyList(), userName, userEmail, userPhone)
                    } else {
                        uploadAllImagesAndSend(size, concept, description, userName, userEmail, userPhone)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show()
                }
        }
    }
// Handle the result of image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == Activity.RESULT_OK) {
            data?.let { intent ->
                val clipData = intent.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        if (selectedImageUris.size >= MAX_IMAGES) break
                        val uri = clipData.getItemAt(i).uri
                        addImageUri(uri)
                    }
                } else {
                    intent.data?.let { uri ->
                        addImageUri(uri)
                    }
                }
            }
        }
    }
// Add the selected image URI to the container and update the list
    private fun addImageUri(uri: Uri) {
        if (selectedImageUris.contains(uri)) return
        selectedImageUris += uri

        val imageView = ImageView(this).apply {
            layoutParams = ViewGroup.MarginLayoutParams(200, 200).apply {
                marginEnd = 16
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(4)
            setImageURI(uri)
        }
        imageContainer.addView(imageView, imageContainer.childCount - 1)
    }

    private fun uploadImage(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val imageRef = storageRef.child(fileName)

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }.addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }
// Upload all selected images and send the request once all uploads are complete
    private fun uploadAllImagesAndSend(
        size: String,
        concept: String,
        description: String,
        userName: String,
        userEmail: String,
        userPhone: String
    ) {
        val uploadedUrls = mutableListOf<String>()
        var uploadErrors = false
        var uploadedCount = 0

        selectedImageUris.forEach { uri ->
            uploadImage(uri,
                onSuccess = { url ->
                    uploadedUrls.add(url)
                    uploadedCount++
                    if (uploadedCount == selectedImageUris.size && !uploadErrors) {
                        sendTattooRequest(size, concept, description, uploadedUrls, userName, userEmail, userPhone)
                    }
                },
                onFailure = { e ->
                    uploadErrors = true
                    runOnUiThread {
                        Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
// Send the tattoo request to the server with the provided details
    private fun sendTattooRequest(
        size: String,
        concept: String,
        description: String,
        imageUrls: List<String>,
        userName: String,
        userEmail: String,
        userPhone: String
    ) {
        val url = "https://sendtattoorequest-fhvdk4ir7a-uc.a.run.app/"

        val json = JSONObject().apply {
            put("size", size)
            put("concept", concept)
            put("description", description)
            put("imageUrls", JSONArray(imageUrls))
            put("userName", userName)
            put("userEmail", userEmail)
            put("userPhone", userPhone)
        }

        val client = OkHttpClient()
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TattooRequestActivity, "Failed to send request: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@TattooRequestActivity, "Tattoo request sent successfully!", Toast.LENGTH_LONG).show()
                        SubmissionStatusActivity.showSuccess(this@TattooRequestActivity)
                        clearForm()
                    } else {
                        Toast.makeText(this@TattooRequestActivity, "Server error: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun clearForm() {
        etConcept.text.clear()
        etDescription.text.clear()
        selectedImageUris.clear()
        imageContainer.removeViews(0, imageContainer.childCount - 1)
    }
}
