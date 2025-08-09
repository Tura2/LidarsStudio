package com.ot.lidarsstudio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

class SubmissionStatusActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_IS_SUCCESS = "EXTRA_IS_SUCCESS"
        private const val STATE_IS_SUCCESS = "STATE_IS_SUCCESS"

        fun startLoading(context: Context) {
            val i = Intent(context, SubmissionStatusActivity::class.java)
                .putExtra(EXTRA_IS_SUCCESS, false)
            context.startActivity(i)
        }

        fun showSuccess(context: Context) {
            val i = Intent(context, SubmissionStatusActivity::class.java)
                .putExtra(EXTRA_IS_SUCCESS, true)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(i)
        }
    }

    private lateinit var titleText: TextView
    private lateinit var lottieView: LottieAnimationView
    private lateinit var backHomeBtn: Button

    private var isSuccessNow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submission_status)

        titleText = findViewById(R.id.titleText)
        lottieView = findViewById(R.id.lottieView)
        backHomeBtn = findViewById(R.id.backHomeBtn)

        backHomeBtn.setOnClickListener {
            startActivity(
                Intent(this, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        }

        isSuccessNow = savedInstanceState?.getBoolean(STATE_IS_SUCCESS)
            ?: intent.getBooleanExtra(EXTRA_IS_SUCCESS, false)

        render(isSuccessNow)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        isSuccessNow = intent.getBooleanExtra(EXTRA_IS_SUCCESS, false)
        render(isSuccessNow)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(STATE_IS_SUCCESS, isSuccessNow)
        super.onSaveInstanceState(outState)
    }

    private fun render(isSuccess: Boolean) {
        if (isSuccess) showSuccess() else showLoading()
    }

    private fun showLoading() {
        isSuccessNow = false
        titleText.text = "Submitting your request..."
        lottieView.setAnimation(R.raw.loading)
        lottieView.repeatCount = LottieDrawable.INFINITE
        lottieView.playAnimation()
        backHomeBtn.visibility = View.GONE
    }

    private fun showSuccess() {
        isSuccessNow = true
        titleText.text = "Your request was sent!"
        lottieView.setAnimation(R.raw.success)
        lottieView.repeatCount = 0
        lottieView.playAnimation()
        backHomeBtn.visibility = View.VISIBLE
    }
}
