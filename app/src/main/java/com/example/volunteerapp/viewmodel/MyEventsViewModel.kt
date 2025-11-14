package com.example.volunteerapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.volunteerapp.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyEventsViewModel : ViewModel() {

    private val _myEvents = MutableStateFlow<List<Event>>(emptyList())
    val myEvents: StateFlow<List<Event>> = _myEvents

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var eventsListener: ListenerRegistration? = null

    init {
        fetchUserEventsRealtime()
    }

    /**
     * Fetch events the current user has registered for and listen for real-time updates.
     */
    private fun fetchUserEventsRealtime() {
        val userId = auth.currentUser?.uid ?: return

        // Listen to all events collection
        eventsListener = firestore.collection("events")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _myEvents.value = emptyList()
                    return@addSnapshotListener
                }

                val registeredEvents = mutableListOf<Event>()

                snapshot.documents.forEach { doc ->
                    val signupsRef = doc.reference.collection("signups").document(userId)
                    signupsRef.get().addOnSuccessListener { signupDoc ->
                        if (signupDoc.exists()) {
                            val event = doc.toObject(Event::class.java)?.copy(id = doc.id)
                            if (event != null) {
                                registeredEvents.add(event)
                                _myEvents.value = registeredEvents
                            }
                        } else {
                            // Remove event if user unregisters
                            _myEvents.value = _myEvents.value.filterNot { it.id == doc.id }
                        }
                    }
                }
            }
    }

    /**
     * Unregister the current user from a specific event.
     */
    fun unregisterFromEvent(eventId: String) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("events").document(eventId)
            .collection("signups").document(userId)
            .delete()
            .addOnSuccessListener {
                // Update local state immediately
                _myEvents.value = _myEvents.value.filterNot { it.id == eventId }
            }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove Firestore listener when ViewModel is cleared
        eventsListener?.remove()
    }
}
