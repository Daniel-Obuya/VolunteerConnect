package com.example.volunteerapp.opportunities

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.volunteerapp.models.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun OpportunitiesScreen(
    userId: String,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val eventList = remember { mutableStateListOf<Event>() }
    var loadingEvents by remember { mutableStateOf(true) }
    var registeringEventId by remember { mutableStateOf<String?>(null) } // Currently registering

    // Fetch events
    LaunchedEffect(Unit) {
        db.collection("opportunities")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load events", Toast.LENGTH_SHORT).show()
                    loadingEvents = false
                    return@addSnapshotListener
                }
                eventList.clear()
                snapshot?.documents?.forEach { doc ->
                    val event = doc.toObject(Event::class.java)
                    event?.id = doc.id
                    event?.let { eventList.add(it) }
                }
                loadingEvents = false
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Volunteer Opportunities", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (loadingEvents) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(eventList) { event ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(event.title, fontWeight = FontWeight.Bold)
                            Text(event.description)
                            Text("Date: ${event.date}")
                            Text("Location: ${event.location}")

                            Spacer(modifier = Modifier.height(8.dp))

                            val isRegistering = registeringEventId == event.id

                            Button(
                                onClick = {
                                    registeringEventId = event.id

                                    val registrationData = mapOf(
                                        "eventId" to event.id,
                                        "title" to event.title,
                                        "date" to event.date,
                                        "location" to event.location,
                                        "timestamp" to System.currentTimeMillis()
                                    )

                                    val userRef = db.collection("users")
                                        .document(userId)
                                        .collection("registeredOpportunities")
                                        .document(event.id)

                                    // Check if already registered
                                    userRef.get().addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            Toast.makeText(context, "Already registered for this event", Toast.LENGTH_SHORT).show()
                                            registeringEventId = null
                                        } else {
                                            userRef.set(registrationData)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Successfully registered!", Toast.LENGTH_SHORT).show()
                                                    registeringEventId = null
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    registeringEventId = null
                                                }
                                        }
                                    }.addOnFailureListener { e ->
                                        Toast.makeText(context, "Could not check registration: ${e.message}", Toast.LENGTH_SHORT).show()
                                        registeringEventId = null
                                    }
                                },
                                enabled = !isRegistering,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isRegistering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Registering...")
                                } else {
                                    Text("Register")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToProfile,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back to Profile")
        }
    }
}
