package com.ot.lidarsstudio.utils

data class Appointment(
    val id: String = "",
    val service: String = "",
    val date: String = "",       // בפורמט dd.MM.yyyy
    val startHour: String = "",  // בפורמט HH:mm
    val durationMinutes: Int = 60,
    val status: String = "scheduled"  // או "pending"
)

