package com.example.volunteerapp.auth

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.volunteerapp.R
import com.example.volunteerapp.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

data class ProgressStat(
    val icon: ImageVector,
    val value: Int,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onLogout: () -> Unit,
    onNavigateToMyEvents: () -> Unit,
    onNavigateToOpportunities: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    var userName by remember { mutableStateOf("Loading...") }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }
    var motivation by remember { mutableStateOf("Tell us about your volunteering motivation and skills...") }
    var registeredOpportunities by remember { mutableStateOf<List<Event>>(emptyList()) }
    var stats by remember { mutableStateOf<List<ProgressStat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isEditMode by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true

        // --- Fetch user data
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                userName = document.getString("name") ?: "No Name"
                userPhotoUrl = document.getString("photoUrl")
                motivation = document.getString("motivation") ?: motivation

                stats = listOf(
                    ProgressStat(Icons.Default.Star, (document.getLong("completedCount") ?: 0).toInt(), "Completed"),
                    ProgressStat(Icons.Default.Timer, (document.getLong("hoursCount") ?: 0).toInt(), "Hours"),
                    ProgressStat(Icons.Default.CalendarToday, (document.getLong("joinedCount") ?: 0).toInt(), "Joined"),
                    ProgressStat(Icons.Default.Build, (document.getLong("skillsCount") ?: 0).toInt(), "Skills")
                )
            }.addOnFailureListener {
                userName = "Error loading profile"
            }

        // --- Fetch registered events from user's subcollection ---
        db.collection("users")
            .document(userId)
            .collection("registeredOpportunities")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    registeredOpportunities = emptyList()
                    isLoading = false
                    return@addSnapshotListener
                }
                val events = mutableListOf<Event>()
                snapshot?.documents?.forEach { doc ->
                    val event = doc.toObject(Event::class.java)?.copy(id = doc.id)
                    event?.let { events.add(it) }
                }
                registeredOpportunities = events
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Profile" else "Profile") },
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
    ) { paddingValues ->
        if (isLoading && userName == "Loading...") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { UserHeaderCard(userName, userPhotoUrl, isEditMode) }
                item { ProgressStatsRow(stats) }
                item { AchievementsSection(listOf("Volunteer of the Month", "100 Hours Milestone", "Community Hero")) }
                item {
                    Button(
                        onClick = { isEditMode = !isEditMode },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(if (isEditMode) Icons.Default.Save else Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isEditMode) "Save Changes" else "Edit Profile Details")
                    }
                }
                item { AboutMeSection(motivation, isEditMode) { motivation = it } }

                item {
                    Text("My Events", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp))
                }

                item {
                    MyRegisteredEventsItem(registeredOpportunities.size, onNavigateToMyEvents)
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun UserHeaderCard(userName: String, userPhotoUrl: String?, isEditMode: Boolean) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = userPhotoUrl ?: R.drawable.vconnect_logo,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProgressStatsRow(stats: List<ProgressStat>) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround) {
        stats.forEach { stat ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(stat.icon, contentDescription = stat.label, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                Text("${stat.value}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(stat.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AchievementsSection(achievements: List<String>) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Achievements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(achievements) { achievement ->
                Card(modifier = Modifier.size(100.dp, 80.dp), elevation = CardDefaults.cardElevation(2.dp),
                    shape = MaterialTheme.shapes.medium) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(achievement, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AboutMeSection(motivation: String, isEditMode: Boolean, onMotivationChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("About Me", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isEditMode) {
                    OutlinedTextField(value = motivation, onValueChange = onMotivationChange,
                        label = { Text("Your Volunteer Motivation") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
                } else {
                    Text(motivation, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun MyRegisteredEventsItem(count: Int, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(16.dp))
                Text("My Registered Events ($count)", style = MaterialTheme.typography.bodyLarge)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
        }
    }
}
