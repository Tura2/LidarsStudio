package com.ot.lidarsstudio.utils

import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.view.animation.AlphaAnimation

object ImageFade {
    fun start(imageView: ImageView, images: List<Int>, delayMillis: Long) {
        var index = 0
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                // אפקט fade
                val fadeOut = AlphaAnimation(1f, 0f)
                fadeOut.duration = 500
                val fadeIn = AlphaAnimation(0f, 1f)
                fadeIn.duration = 500

                imageView.startAnimation(fadeOut)
                fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                    override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                        imageView.setImageResource(images[index])
                        imageView.startAnimation(fadeIn)
                    }

                    override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                    override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                })

                index = (index + 1) % images.size
                handler.postDelayed(this, delayMillis)
            }
        }

        handler.post(runnable)
    }
}
