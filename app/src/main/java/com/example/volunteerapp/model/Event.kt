// In file: app/src/main/java/com/example/volunteerapp/model/Event.kt
package com.example.volunteerapp.model

import com.google.firebase.firestore.DocumentId

data class Event(
    @DocumentId val id: String = "", // This annotation maps the document ID to this field
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = ""
)
