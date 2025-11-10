package com.example.volunteerapp.utils

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {
    val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val eventsCollection
        get() = firestoreInstance.collection("events")
}
