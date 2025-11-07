package com.example.volunteerapp.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.volunteerapp.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onLogout: () -> Unit,
    onNavigateToOpportunities: () -> Unit // This comes from MainActivity
) {
    var user by remember { mutableStateOf<User?>(null) }
    var signedUpEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Fetch user data and their signed-up events
    LaunchedEffect(userId) {
        // Fetch user details
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                user = document.toObject<User>()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load profile.", Toast.LENGTH_SHORT).show()
            }

        // Fetch events the user has signed up for
        db.collectionGroup("signups").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { signupSnapshot ->
                val eventIds = signupSnapshot.documents.map { it.reference.parent.parent?.id }
                if (eventIds.isNotEmpty()) {
                    db.collection("events").whereIn("id", eventIds).get()
                        .addOnSuccessListener { eventSnapshot ->
                            signedUpEvents = eventSnapshot.toObjects(Event::class.java)
                        }
                }
            }
    }

    // --- UI with Scaffold and FAB ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        // --- THIS IS THE KEY ADDITION ---
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Find Opportunities") },
                icon = { Icon(Icons.Default.Event, contentDescription = "Find Opportunities") },
                onClick = onNavigateToOpportunities // This triggers the navigation
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            user?.let {
                Text(it.name, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(it.email, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                "My Registered Events",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (signedUpEvents.isEmpty()) {
                Text("You haven't signed up for any events yet.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(signedUpEvents) { event ->
                        EventHistoryCard(event)
                    }
                }
            }
        }
    }
}

@Composable
fun EventHistoryCard(event: Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Text("Date: ${event.date}", style = MaterialTheme.typography.bodySmall)
            Text("Location: ${event.location}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Make sure you have this User data class defined, probably in a 'model' package
data class User(
    val name: String = "",
    val email: String = "",
    val role: String = "Volunteer"
)
