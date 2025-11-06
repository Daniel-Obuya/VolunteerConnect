package com.example.volunteerapp.auth

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.volunteerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// A simple data class for holding opportunity details
data class VolunteerOpportunity(
    val id: String = "",
    val title: String = "",
    val date: String = "", // Keep it simple for now
    val location: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onLogout: () -> Unit,
    onNavigateToOpportunities: () -> Unit // Navigation callback
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // --- State Management ---
    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("Loading...") }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }
    var registeredOpportunities by remember { mutableStateOf<List<VolunteerOpportunity>>(emptyList()) }
    var isLoadingEvents by remember { mutableStateOf(true) }

    // --- Tab State ---
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("My Details", "My Events")

    // --- Data Fetchingg ---
    LaunchedEffect(userId) {
        // Fetch User Details
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userName = document.getString("name") ?: "No Name"
                    userEmail = document.getString("email") ?: "No Email"
                    userPhotoUrl = document.getString("photoUrl")
                }
            }

        // Fetch Registered Opportunities
        isLoadingEvents = true
        db.collectionGroup("signups").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { snapshot ->
                val opportunityIds = snapshot.documents.map { it.reference.parent.parent?.id }
                if (opportunityIds.isNotEmpty()) {
                    db.collection("opportunities").whereIn("id", opportunityIds).get()
                        .addOnSuccessListener { opportunityDocs ->
                            registeredOpportunities = opportunityDocs.toObjects(VolunteerOpportunity::class.java)
                            isLoadingEvents = false
                        }.addOnFailureListener { isLoadingEvents = false }
                } else {
                    isLoadingEvents = false
                }
            }.addOnFailureListener { isLoadingEvents = false }
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
                onClick = onNavigateToOpportunities // Navigate to the main list
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // --- Tab Layout ---
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // --- Content based on selected tab ---
            when (selectedTabIndex) {
                0 -> MyDetailsTab(userId, userName, userEmail, userPhotoUrl)
                1 -> MyEventsTab(registeredOpportunities, isLoadingEvents)
            }
        }
    }
}

@Composable
fun MyDetailsTab(
    userId: String,
    initialName: String,
    email: String,
    photoUrl: String?
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    // Edit mode states
    var isEditMode by remember { mutableStateOf(false) }
    var editableName by remember(initialName) { mutableStateOf(initialName) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Derived states for display
    val currentName by remember(initialName, editableName, isEditMode) { derivedStateOf { if (isEditMode) editableName else initialName } }
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

        // --- Profile Picture Section ---
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = currentPhoto ?: R.drawable.vconnect_logo,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = isEditMode) {
                        // Image picker logic here
                    },
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

        // --- User Details Section ---
        if (isEditMode) {
            OutlinedTextField(
                value = editableName,
                onValueChange = { editableName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(text = currentName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.weight(1f)) // Pushes button to bottom

        // --- Edit/Save Button ---
        Button(
            onClick = {
                if (isEditMode) {
                    // Save logic
                    isSaving = true
                    // (Add the full save logic from the previous answer here)
                    Toast.makeText(context, "Saving...", Toast.LENGTH_SHORT).show()
                    // Simulate save and exit edit mode
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
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
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
                        Text(text = opportunity.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Date: ${opportunity.date}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Location: ${opportunity.location}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
