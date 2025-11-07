// In file: app/src/main/java/com/example/volunteerapp/opportunities/OpportunitiesScreen.kt

package com.example.volunteerapp.opportunities

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle // <-- Use a profile icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.volunteerapp.model.Event
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunitiesScreen(
    userId: String,
    onNavigateToProfile: () -> Unit // <-- CHANGED: More descriptive name
) {
    val db = FirebaseFirestore.getInstance()
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("events")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    events = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    }
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volunteer Opportunities") },
                // --- THIS IS THE KEY CHANGE ---
                actions = {
                    IconButton(onClick = onNavigateToProfile) { // Use the new navigation lambda
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Go to Profile",
                            modifier = Modifier.size(28.dp) // Make icon a bit larger
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No volunteer events available.")
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(events) { event ->
                    EventItem(event = event, userId = userId)
                }
            }
        }
    }
}

// The EventItem composable remains the same as before
@Composable
fun EventItem(event: Event, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var isRegistered by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }
    var registrationChecked by remember { mutableStateOf(false) }

    LaunchedEffect(event.id, userId) {
        val signupRef = db.collection("events").document(event.id)
            .collection("signups").document(userId)
        signupRef.get().addOnSuccessListener { document ->
            isRegistered = document.exists()
            registrationChecked = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Date: ${event.date}")
            Text(text = "Time: ${event.time}")
            Text(text = "Location: ${event.location}")
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isRegistering = true
                    val signupRef = db.collection("events").document(event.id)
                        .collection("signups").document(userId)
                    val signupData = hashMapOf(
                        "userId" to userId,
                        "signupTime" to FieldValue.serverTimestamp()
                    )
                    signupRef.set(signupData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Successfully registered!", Toast.LENGTH_SHORT).show()
                            isRegistered = true
                            isRegistering = false
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            isRegistering = false
                        }
                },
                enabled = !isRegistered && !isRegistering && registrationChecked
            ) {
                when {
                    isRegistering -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                    isRegistered -> Text("Registered")
                    !registrationChecked -> Text("Checking...")
                    else -> Text("Register")
                }
            }
        }
    }
}
