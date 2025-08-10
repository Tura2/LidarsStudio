package com.ot.lidarsstudio.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

object StorageImageLoader {

    // Load image from gs:// path into ImageView
    fun loadImage(gsPath: String, imageView: ImageView) {
        val storageRef = Firebase.storage.getReferenceFromUrl(gsPath)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(imageView.context)
                .load(uri)
                .into(imageView)
        }.addOnFailureListener {
            // can add error handling here if needed
        }
    }

    // Load image from gs:// path and set as background
    fun loadBackground(gsPath: String, view: View, context: Context) {
        val storageRef = Firebase.storage.getReferenceFromUrl(gsPath)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(context)
                .load(uri)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        view.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }.addOnFailureListener {
            // can add error handling here if needed
        }
    }
}
