package com.ot.lidarsstudio.utils

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val phone: String = "",
    val fullName: String = "",
    val service: String = "",
    val date: String = "",
    val startHour: String = "",
    val durationMinutes: Int = 60,
    val status: String = "scheduled"
)

