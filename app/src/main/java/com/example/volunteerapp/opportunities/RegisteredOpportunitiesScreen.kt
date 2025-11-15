package com.example.volunteerapp.opportunities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.volunteerapp.models.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisteredOpportunitiesScreen(
    onBackToProfile: () -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()
    val registeredEvents = remember { mutableStateListOf<Event>() }

    LaunchedEffect(Unit) {
        db.collection("users")
            .document(userId)
            .collection("registeredOpportunities")
            .addSnapshotListener { snapshot, _ ->
                registeredEvents.clear()
                snapshot?.documents?.forEach { signupDoc ->
                    val eventId = signupDoc.getString("eventId")
                    if (eventId != null) {
                        db.collection("opportunities")
                            .document(eventId)
                            .get()
                            .addOnSuccessListener { eventDoc ->
                                val event = eventDoc.toObject(Event::class.java)
                                event?.id = eventDoc.id
                                event?.let { registeredEvents.add(it) }
                            }
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Registered Opportunities", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(registeredEvents) { event ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(event.title, fontWeight = FontWeight.Bold)
                        Text(event.description)
                        Text("Date: ${event.date}")
                        Text("Location: ${event.location}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackToProfile) {
            Text("Back to Profile")
        }
    }
}
