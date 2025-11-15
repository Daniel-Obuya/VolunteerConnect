package com.example.volunteerapp.auth

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.volunteerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// Data class for holding opportunity detailsss
data class VolunteerOpportunity(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val location: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onLogout: () -> Unit,
    onNavigateToOpportunities: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("Loading...") }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }
    var registeredOpportunities by remember { mutableStateOf<List<VolunteerOpportunity>>(emptyList()) }
    var isLoadingEvents by remember { mutableStateOf(true) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("My Details", "My Events")

    // --- Fetch user details and registered events ---
    LaunchedEffect(userId) {
        // Fetch user details
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    userName = doc.getString("name") ?: "No Name"
                    userEmail = doc.getString("email") ?: "No Email"
                    userPhotoUrl = doc.getString("photoUrl")
                }
            }

        // Fetch registered opportunities
        isLoadingEvents = true
        db.collection("users")
            .document(userId)
            .collection("registeredOpportunities")
            .get()
            .addOnSuccessListener { snapshot ->
                val eventIds = snapshot.documents.mapNotNull { it.getString("eventId") }

                if (eventIds.isNotEmpty()) {
                    val events = mutableListOf<VolunteerOpportunity>()
                    var remaining = eventIds.size

                    for (eventId in eventIds) {
                        db.collection("opportunities").document(eventId).get()
                            .addOnSuccessListener { eventDoc ->
                                eventDoc.toObject(VolunteerOpportunity::class.java)?.let {
                                    events.add(it.copy(id = eventDoc.id))
                                }
                            }
                            .addOnCompleteListener {
                                remaining--
                                if (remaining == 0) {
                                    registeredOpportunities = events
                                    isLoadingEvents = false
                                }
                            }
                    }
                } else {
                    registeredOpportunities = emptyList()
                    isLoadingEvents = false
                }
            }
            .addOnFailureListener {
                registeredOpportunities = emptyList()
                isLoadingEvents = false
                Toast.makeText(context, "Failed to load events", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Find Opportunities") },
                icon = { Icon(Icons.Default.Search, contentDescription = null) },
                onClick = onNavigateToOpportunities
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Tabs
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab content
            when (selectedTabIndex) {
                0 -> MyDetailsTab(userId, userName, userEmail, userPhotoUrl)
                1 -> MyEventsTab(registeredOpportunities, isLoadingEvents)
            }
        }
    }
}

@Composable
fun MyDetailsTab(userId: String, name: String, email: String, photoUrl: String?) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var isEditMode by remember { mutableStateOf(false) }
    var editableName by remember(name) { mutableStateOf(name) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val currentName by remember(editableName, isEditMode) { derivedStateOf { if (isEditMode) editableName else name } }
    val currentPhoto by remember(photoUrl, selectedImageUri) { derivedStateOf { selectedImageUri ?: photoUrl } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSaving) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = currentPhoto ?: R.drawable.vconnect_logo,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = isEditMode) { },
                contentScale = ContentScale.Crop
            )
            if (isEditMode) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Photo",
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isEditMode) {
            OutlinedTextField(
                value = editableName,
                onValueChange = { editableName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(currentName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (isEditMode) {
                    isSaving = true
                    Toast.makeText(context, "Saving...", Toast.LENGTH_SHORT).show()
                    isSaving = false
                    isEditMode = false
                } else {
                    isEditMode = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEditMode) "Save Changes" else "Edit Details")
        }
    }
}

@Composable
fun MyEventsTab(opportunities: List<VolunteerOpportunity>, isLoading: Boolean) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (opportunities.isEmpty()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center) {
            Text("You haven't signed up for any events yet.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(opportunities) { opportunity ->
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(opportunity.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Date: ${opportunity.date}", style = MaterialTheme.typography.bodyMedium)
                        Text("Location: ${opportunity.location}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
