package com.ot.lidarsstudio.models

import com.google.firebase.Timestamp

data class Appointment(
    val docId: String,
    val service: String,
    val dateTime: Timestamp,
    var status: String
)
