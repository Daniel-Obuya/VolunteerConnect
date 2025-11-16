package com.example.volunteerapp.model

// This data class defines the structure for an Event across your entire app
data class Event(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val time: String = "" ,
    val totalVolunteers: Int? = null,
    val signups: List<String> = emptyList(),
    val status: String = "Active"  // New: Status of the event
)
