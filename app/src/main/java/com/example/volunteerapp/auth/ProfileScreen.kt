package com.example.volunteerapp.auth

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// Data class for Quick Stats
data class QuickStat(
    val icon: ImageVector,
    val value: String,
    val label: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onLogout: () -> Unit,
    onNavigateToOpportunities: () -> Unit,
    onNavigateToMyEvents: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    // State Variables
    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("Loading...") }
    var userBio by remember { mutableStateOf("Tell us about your volunteering motivation and skills...") }
    var userLocation by remember { mutableStateOf("Not set") }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }
    var registeredOpportunities by remember { mutableStateOf<List<VolunteerOpportunity>>(emptyList()) }
    var isLoadingEvents by remember { mutableStateOf(true) }

    var isEditMode by remember { mutableStateOf(false) }
    var editableName by remember(userName) { mutableStateOf(userName) }
    var editableBio by remember(userBio) { mutableStateOf(userBio) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val currentPhotoDisplay by remember(userPhotoUrl, selectedImageUri) {
        derivedStateOf { selectedImageUri ?: userPhotoUrl }
    }

    // Placeholder data for stats (You would fetch these from Firestore)
    val completedEvents = 5
    val hoursVolunteered = 38
    val eventsJoined = registeredOpportunities.size
    val skillsCount = 4

    val quickStats = listOf(
        QuickStat(Icons.Default.Star, "$completedEvents", "Completed", MaterialTheme.colorScheme.primary),
        QuickStat(Icons.Default.Timer, "$hoursVolunteered", "Hours", MaterialTheme.colorScheme.tertiary),
        QuickStat(Icons.Default.Event, "$eventsJoined", "Joined", MaterialTheme.colorScheme.secondary),
        QuickStat(Icons.Default.Build, "$skillsCount", "Skills", MaterialTheme.colorScheme.error)
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // --- Fetch user details and registered events ---
    LaunchedEffect(userId) {
        // Fetch user details
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    userName = doc.getString("name") ?: "No Name"
                    userEmail = doc.getString("email") ?: "No Email"
                    userBio = doc.getString("bio") ?: "Tell us about your volunteering motivation and skills..."
                    userLocation = doc.getString("location") ?: "Not set"
                    userPhotoUrl = doc.getString("photoUrl")
                    editableName = userName
                    editableBio = userBio
                }
            }

        // Fetch registered opportunities (same logic as before)
        isLoadingEvents = true
        db.collection("users")
            .document(userId)
            .collection("registeredOpportunities")
            .get()
            .addOnSuccessListener { snapshot ->
                // ... (existing logic to fetch and filter events)
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
                                    registeredOpportunities = events.sortedBy { it.date } // Sort for Upcoming
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
                isLoadingEvents = false
            }
    }

    // Function to handle saving changes
    val saveChanges: () -> Unit = {
        isSaving = true
        val userDocRef = db.collection("users").document(userId)
        val updates = mutableMapOf<String, Any>()

        if (editableName != userName) updates["name"] = editableName
        if (editableBio != userBio) updates["bio"] = editableBio

        fun finalizeSave(photoUrlUpdate: String? = null) {
            photoUrlUpdate?.let { updates["photoUrl"] = it }

            if (updates.isNotEmpty()) {
                userDocRef.update(updates)
                    .addOnSuccessListener {
                        userName = editableName
                        userBio = editableBio
                        Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        isSaving = false
                        isEditMode = false
                        selectedImageUri = null
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
                        isSaving = false
                    }
            } else {
                Toast.makeText(context, "No changes to save.", Toast.LENGTH_SHORT).show()
                isSaving = false
                isEditMode = false
            }
        }

        if (selectedImageUri != null) {
            val photoRef = storage.reference.child("profile_pictures/${userId}.jpg")
            photoRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    photoRef.downloadUrl.addOnSuccessListener { uri ->
                        finalizeSave(uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_LONG).show()
                    isSaving = false
                }
        } else {
            finalizeSave()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    if (!isEditMode) {
                        IconButton(onClick = {
                            FirebaseAuth.getInstance().signOut()
                            onLogout()
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                        }
                    } else {
                        TextButton(onClick = {
                            isEditMode = false
                            editableName = userName
                            editableBio = userBio
                            selectedImageUri = null
                        }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = saveChanges,
                            enabled = !isSaving,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isEditMode) {
                // Keep the FAB for the primary "Find Opportunities" action
                FloatingActionButton(onClick = onNavigateToOpportunities) {
                    Icon(Icons.Default.Search, contentDescription = "Find Opportunities")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. Profile Header Section
            item {
                ProfileHeader(
                    userName = userName,
                    userEmail = userEmail,
                    currentPhotoDisplay = currentPhotoDisplay,
                    isEditMode = isEditMode,
                    editableName = editableName,
                    onNameChange = { editableName = it },
                    onEditPhotoClicked = { pickImageLauncher.launch("image/*") }
                )
            }

            // 2. Quick Statistics
            item {
                QuickStats(quickStats = quickStats)
            }

            // 3. Edit Profile Button
            item {
                if (!isEditMode) {
                    Button(
                        onClick = { isEditMode = true },
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(48.dp)
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile Details", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // 4. About Me Section
            item {
                AboutMe(
                    bio = userBio,
                    location = userLocation,
                    isEditMode = isEditMode,
                    editableBio = editableBio,
                    onBioChange = { editableBio = it }
                )
            }

            // 5. User Events Section (Registered and Posted)
            item {
                ProfileSectionCard(title = "My Events") {
                    Column {
                        // User's Registered Events
                        ProfileOption(
                            icon = Icons.Default.CalendarToday,
                            title = "My Registered Events ($eventsJoined)",
                            onClick = onNavigateToMyEvents
                        )
                        Divider()
                        // User's Posted Events (if feature exists)
                        ProfileOption(
                            icon = Icons.Default.PostAdd, // Icon for posting/creating
                            title = "My Posted Events",
                            onClick = { /* TODO: Navigate to screen listing events created by this user */ }
                        )
                    }
                }
            }

            // 6. Upcoming Event Highlight
            item {
                UpcomingEvents(registeredOpportunities.firstOrNull(), isLoadingEvents)
            }

            // 7. Achievements/Badges (Placeholder)
            item {
                ProfileSectionCard(title = "Achievements & Badges") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BadgeChip("🌟 First Event", MaterialTheme.colorScheme.primary)
                        BadgeChip("🔥 Active Volunteer", MaterialTheme.colorScheme.tertiary)
                        BadgeChip("🏅 20 Hours", MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

// --- COMPOSE HELPER FUNCTIONS (No functional changes here) ---

@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    currentPhotoDisplay: Any?,
    isEditMode: Boolean,
    editableName: String,
    onNameChange: (String) -> Unit,
    onEditPhotoClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.background
                    ),
                    startY = 0f,
                    endY = 500f
                )
            )
            .padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .offset(y = 50.dp)
                .fillMaxWidth(0.95f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 16.dp)
                    .offset(y = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                if (isEditMode) {
                    OutlinedTextField(
                        value = editableName,
                        onValueChange = onNameChange,
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        singleLine = true
                    )
                } else {
                    Text(
                        userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(y = 20.dp)
                .size(120.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AsyncImage(
                model = currentPhotoDisplay ?: R.drawable.vconnect_logo,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = isEditMode, onClick = onEditPhotoClicked),
                contentScale = ContentScale.Crop
            )
            if (isEditMode) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Photo",
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(6.dp)
                        .size(24.dp)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .clickable(onClick = onEditPhotoClicked),
                    tint = Color.White
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(80.dp))
}

@Composable
fun QuickStats(quickStats: List<QuickStat>) {
    ProfileSectionCard(title = "Your Progress") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            quickStats.forEach { stat ->
                StatItem(stat)
            }
        }
    }
}

@Composable
fun StatItem(stat: QuickStat) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = stat.icon,
            contentDescription = null,
            tint = stat.color,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = stat.value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stat.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AboutMe(
    bio: String,
    location: String,
    isEditMode: Boolean,
    editableBio: String,
    onBioChange: (String) -> Unit
) {
    ProfileSectionCard(title = "About Me") {
        Column(
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            if (isEditMode) {
                OutlinedTextField(
                    value = editableBio,
                    onValueChange = onBioChange,
                    label = { Text("Your Volunteer Motivation") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            } else {
                Text(
                    bio,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Location: $location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UpcomingEvents(nextEvent: VolunteerOpportunity?, isLoading: Boolean) {
    ProfileSectionCard(title = "Upcoming Event Highlight") {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp))
            }
        } else if (nextEvent == null) {
            Text(
                "You have no upcoming events.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            nextEvent.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${nextEvent.date} @ ${nextEvent.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "View Event",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ProfileOption(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Go to", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    // This is a minimal implementation of a custom FlowRow layout to support the badges.
    // For a production app, use the official androidx.compose.foundation.layout.FlowRow
    // which may require updating your Compose dependencies.
    // For now, we'll use a nested Column/Row structure that approximates it.
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        Row(horizontalArrangement = horizontalArrangement) {
            // Simplified placeholder - for a proper flow layout you'd need custom layout logic.
            content.invoke(FlowRowScope)
        }
    }
}

object FlowRowScope

@Composable
fun BadgeChip(text: String, color: Color) {
    AssistChip(
        onClick = { /* Do nothing */ },
        label = { Text(text) },
        leadingIcon = {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.1f),
            labelColor = color,
            leadingIconContentColor = color
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp) // Add padding for flow effect
    )
}