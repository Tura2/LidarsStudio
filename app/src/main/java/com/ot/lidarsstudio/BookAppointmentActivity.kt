
package com.ot.lidarsstudio

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat

class BookAppointmentActivity : AppCompatActivity() {

    private var selectedServiceName: String = ""
    private var pendingAppointment: Triple<String, String, String>? = null
    private var confirmedAppointment: Triple<String, String, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        val buttonTattoo = findViewById<MaterialButton>(R.id.buttonTattoo)
        val buttonGel = findViewById<MaterialButton>(R.id.buttonGelNails)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        val textConfirmedTitle = findViewById<TextView>(R.id.textActiveAppointment)
        val textConfirmedDetails = findViewById<TextView>(R.id.textActiveAppointmentDetails)
        val textPendingTitle = findViewById<TextView>(R.id.textPendingAppointment)
        val textPendingDetails = findViewById<TextView>(R.id.textPendingAppointmentDetails)
        val btnConfirmNow = findViewById<MaterialButton>(R.id.buttonConfirmPendingAppointment)
        val btnCancel = findViewById<MaterialButton>(R.id.buttonCancelAppointment)

        btnClose.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonTattoo.setOnClickListener {
            Toast.makeText(this, "Tattoo form will open here", Toast.LENGTH_SHORT).show()
        }

        buttonGel.setOnClickListener {
            pendingAppointment = null
            updateAppointmentViews()
            showGelNailOptions()
        }

        btnConfirmNow.setOnClickListener {
            pendingAppointment?.let {
                confirmedAppointment = it
                pendingAppointment = null
                Toast.makeText(this, "Appointment confirmed!", Toast.LENGTH_SHORT).show()
                updateAppointmentViews()
            }
        }

        btnCancel.setOnClickListener {
            confirmedAppointment?.let { (service, date, hour) ->
                showCancelConfirmationDialog(service, date, hour)
            }
        }


        updateAppointmentViews()
    }

    @SuppressLint("WrongViewCast")
    private fun updateAppointmentViews() {
        val textConfirmed = findViewById<TextView>(R.id.textActiveAppointmentDetails)
        val textPending = findViewById<TextView>(R.id.textPendingAppointmentDetails)
        val textConfirmedTitle = findViewById<TextView>(R.id.textActiveAppointment)
        val textPendingTitle = findViewById<TextView>(R.id.textPendingAppointment)
        val btnConfirmNow = findViewById<MaterialButton>(R.id.buttonConfirmPendingAppointment)
        val layoutActiveAppointmentBlock = findViewById<LinearLayout>(R.id.layoutActiveAppointmentBlock)

        if (confirmedAppointment != null) {
            val (service, date, hour) = confirmedAppointment!!
            textConfirmed.text = "Service: $service\nDate: $date\nHour: $hour"
            textConfirmedTitle.visibility = View.VISIBLE
            layoutActiveAppointmentBlock.visibility = View.VISIBLE
        } else {
            textConfirmedTitle.visibility = View.GONE
            layoutActiveAppointmentBlock.visibility = View.GONE
        }

        if (pendingAppointment != null) {
            val (service, date, hour) = pendingAppointment!!
            textPending.text = "Service: $service\nDate: $date\nHour: $hour"
            textPending.visibility = View.VISIBLE
            textPendingTitle.visibility = View.VISIBLE
            btnConfirmNow.visibility = View.VISIBLE
        } else {
            textPending.visibility = View.GONE
            textPendingTitle.visibility = View.GONE
            btnConfirmNow.visibility = View.GONE
        }
    }


    private fun showGelNailOptions() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_gel_options, null)

        view.findViewById<MaterialButton>(R.id.btnRegularGel).setOnClickListener {
            selectedServiceName = "Regular Gel"
            dialog.dismiss()
            showDateTimePicker()
        }

        view.findViewById<MaterialButton>(R.id.btnAnatomyGel).setOnClickListener {
            selectedServiceName = "Gel with Anatomy"
            dialog.dismiss()
            showDateTimePicker()
        }

        view.findViewById<MaterialButton>(R.id.btnPaintedGel).setOnClickListener {
            selectedServiceName = "Painted Gel"
            dialog.dismiss()
            showDateTimePicker()
        }

        view.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showDateTimePicker() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_select_date_time, null)
        dialog.setContentView(view)

        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = (600 * resources.displayMetrics.density).toInt()
        bottomSheet?.requestLayout()

        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseDateTime)
        val layoutDates = view.findViewById<LinearLayout>(R.id.layoutDates)
        val layoutHours = view.findViewById<GridLayout>(R.id.layoutHours)
        val scrollHours = view.findViewById<View>(R.id.scrollHours)
        val textChooseHour = view.findViewById<TextView>(R.id.textChooseHour)

        val sampleDates = listOf("24.6", "25.6", "26.6", "27.6", "28.6", "29.6", "30.6")
        var selectedDateBtn: MaterialButton? = null

        for (date in sampleDates) {
            val btn = MaterialButton(this).apply {
                text = date
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 0, 16, 0)
                }
                minimumWidth = 0
                minWidth = 0
                setPadding(36, 12, 36, 12)
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.primaryColor)
                setTextColor(ContextCompat.getColor(context, R.color.white))
            }

            btn.setOnClickListener {
                selectedDateBtn?.apply {
                    strokeColor = ContextCompat.getColorStateList(context, R.color.primaryColor)
                    setTextColor(ContextCompat.getColor(context, R.color.primaryColor))
                    backgroundTintList = null
                }

                selectedDateBtn = btn
                btn.setTextColor(ContextCompat.getColor(this@BookAppointmentActivity, R.color.white))
                btn.setBackgroundTintList(ContextCompat.getColorStateList(this@BookAppointmentActivity, R.color.primaryColor))

                if (layoutHours.childCount == 0) {
                    val hourSlots = generateTimeSlots("08:30", "19:30", 30)
                    for (time in hourSlots) {
                        val timeBtn = MaterialButton(this@BookAppointmentActivity).apply {
                            text = time
                            layoutParams = GridLayout.LayoutParams().apply {
                                width = GridLayout.LayoutParams.WRAP_CONTENT
                                height = GridLayout.LayoutParams.WRAP_CONTENT
                                setMargins(16, 16, 16, 16)
                            }
                            minimumWidth = 0
                            minWidth = 0
                            setPadding(36, 12, 36, 12)
                            backgroundTintList = ContextCompat.getColorStateList(context, R.color.primaryColor)
                            setTextColor(ContextCompat.getColor(context, R.color.white))
                        }

                        timeBtn.setOnClickListener {
                            dialog.dismiss()
                            showConfirmationBottomSheet(selectedServiceName, date, time)
                        }

                        layoutHours.addView(timeBtn)
                    }

                    textChooseHour.visibility = View.VISIBLE
                    scrollHours.visibility = View.VISIBLE
                }
            }

            layoutDates.addView(btn)
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showConfirmationBottomSheet(service: String, date: String, time: String) {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_confirm_appointment, null)

        val textDetails = view.findViewById<TextView>(R.id.textAppointmentDetails)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirmAppointment)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseConfirmation)

        textDetails.text = "Service: $service\nDate: $date\nHour: $time"

        btnConfirm.setOnClickListener {
            confirmedAppointment = Triple(service, date, time)
            pendingAppointment = null
            dialog.dismiss()
            updateAppointmentViews()
            Toast.makeText(this, "Appointment confirmed!", Toast.LENGTH_SHORT).show()
        }

        btnClose.setOnClickListener {
            pendingAppointment = Triple(service, date, time)
            dialog.dismiss()
            updateAppointmentViews()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun generateTimeSlots(start: String, end: String, intervalMinutes: Int): List<String> {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTime = format.parse(start) ?: return emptyList()
        val endTime = format.parse(end) ?: return emptyList()

        val times = mutableListOf<String>()
        val calendar = Calendar.getInstance().apply { time = startTime }

        while (calendar.time <= endTime) {
            times.add(format.format(calendar.time))
            calendar.add(Calendar.MINUTE, intervalMinutes)
        }
        return times
    }
    private fun showCancelConfirmationDialog(service: String, date: String, hour: String) {
        val message = "Are you sure you want to cancel this appointment?\n\nService: $service\nDate: $date\nHour: $hour"

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Cancel Appointment")
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                confirmedAppointment = null
                updateAppointmentViews()
                Toast.makeText(this, "Appointment cancelled.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()
    }

}
