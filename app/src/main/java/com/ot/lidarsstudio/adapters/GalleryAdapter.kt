package com.ot.lidarsstudio.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ot.lidarsstudio.R

class GalleryAdapter(
    private val images: List<String>,
    private val activity: Activity
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val galleryImageView: ImageView = itemView.findViewById(R.id.imageViewGallery)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val url = images[position]

        // load into grid cell with placeholder & error fallback
        Glide.with(holder.itemView.context)
            .load(url)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_placeholder)
            .into(holder.galleryImageView)

        holder.galleryImageView.setOnClickListener {
            // show full-screen popup
            val popupContainer = activity.findViewById<FrameLayout>(R.id.popupImageContainer)
            val popupImageView = activity.findViewById<ImageView>(R.id.popupImageView)
            val closeButton    = activity.findViewById<ImageButton>(R.id.btnClosePopup)

            popupContainer.visibility = VISIBLE

            // load same URL into popup
            Glide.with(activity)
                .load(url)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_placeholder)
                .into(popupImageView)

            closeButton.setOnClickListener {
                popupContainer.visibility = GONE
            }
        }
    }

    override fun getItemCount(): Int = images.size
}
