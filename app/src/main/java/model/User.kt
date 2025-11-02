package com.example.volunteerapp.model

data class User(
    val name: String = "",
    val email: String = "",
    val role: String = "Volunteer",
    val profileImage: String? = null
)
