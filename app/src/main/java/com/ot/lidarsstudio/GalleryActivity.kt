// app/src/main/java/com/ot/lidarsstudio/GalleryActivity.kt
package com.ot.lidarsstudio

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ot.lidarsstudio.adapters.GalleryAdapter
import com.ot.lidarsstudio.BaseActivity

class GalleryActivity : BaseActivity() {

    private lateinit var recyclerViewGallery: RecyclerView
    private lateinit var popupImageContainer: FrameLayout
    private lateinit var popupImageView: ImageView
    private lateinit var closePopupButton: ImageButton
    private lateinit var toggleNailsButton: Button
    private lateinit var toggleTattoosButton: Button 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_gallery)
        setupDrawer()

        // find views
        recyclerViewGallery       = findViewById(R.id.galleryRecyclerView)
        popupImageContainer       = findViewById(R.id.popupImageContainer)
        popupImageView            = findViewById(R.id.popupImageView)
        closePopupButton          = findViewById(R.id.btnClosePopup)
        toggleNailsButton         = findViewById(R.id.buttonToggleNails)
        toggleTattoosButton       = findViewById(R.id.buttonToggleTattoos)

        // grid of 3 columns
        recyclerViewGallery.layoutManager = GridLayoutManager(this, 3)
        // initial load
        loadImagesByCategory("nails")

        toggleNailsButton.setOnClickListener {
            loadImagesByCategory("nails")
        }
        toggleTattoosButton.setOnClickListener {
            loadImagesByCategory("tattoos")
        }

        // popup close
        closePopupButton.setOnClickListener {
            popupImageContainer.visibility = View.GONE
        }
    }

    private fun loadImagesByCategory(category: String) {
        // clear current adapter so we don't show stale images
        recyclerViewGallery.adapter = GalleryAdapter(emptyList(), this)

        val firestore = FirebaseFirestore.getInstance()
        val storage   = FirebaseStorage.getInstance()

        firestore.collection("galleryImages")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) return@addOnSuccessListener

                val tasks = snapshot.documents.mapNotNull { doc ->
                    doc.getString("imagePath")?.trim()?.takeIf { it.isNotEmpty() }
                        ?.let { path ->
                            storage.getReference(path).downloadUrl
                        }
                }

                Tasks.whenAllSuccess<android.net.Uri>(tasks)
                    .addOnSuccessListener { uriList ->
                        // map to String and swap into adapter
                        val urls = uriList.map { it.toString() }
                        recyclerViewGallery.adapter = GalleryAdapter(urls, this)
                    }
                    .addOnFailureListener { e ->
                        // log / toast as desired
                        e.printStackTrace()
                    }
            }
            .addOnFailureListener { e ->
                // handle Firestore error
                e.printStackTrace()
            }
    }
}
