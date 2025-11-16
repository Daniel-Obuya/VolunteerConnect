package com.example.volunteerapp.opportunities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.volunteerapp.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisteredOpportunitiesScreen(
    onBackToProfile: () -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()
    var registeredEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch all events where the current user has signed up
    LaunchedEffect(userId) {
        if (userId.isBlank()) return@LaunchedEffect

        db.collection("opportunities")
            .whereArrayContains("signups", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    registeredEvents = emptyList()
                    isLoading = false
                    return@addSnapshotListener
                }

                registeredEvents = snapshot?.toObjects(Event::class.java) ?: emptyList()
                isLoading = false
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Text("Registered Opportunities", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (registeredEvents.isEmpty()) {
            Text("You haven't registered for any events yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(registeredEvents) { event ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(event.title, fontWeight = FontWeight.Bold)
                            Text(event.description)
                            Text("Date: ${event.date}")
                            Text("Location: ${event.location}")
                            Text("Time: ${event.time}")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackToProfile, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Profile")
        }
    }
}
