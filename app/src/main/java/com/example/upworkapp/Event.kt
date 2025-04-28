package com.example.upworkapp

// Event.kt


import java.util.Date

data class Event(
    val id: Long,
    val title: String,
    val dateTime: Date,
    val location: String = "",
    val notes: String = ""
)
