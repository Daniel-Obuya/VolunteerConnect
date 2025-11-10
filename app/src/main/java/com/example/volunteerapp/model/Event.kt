package com.example.volunteerapp.model // Assuming the package name is 'models' as per previous code

data class Event(
    // var allows changing the ID later, val requires it to be final
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = "",
    val totalVolunteers: Int = 0
)