// Updated AdminDashboardScreen.kt with color scheme matching the provided profile UI

package com.example.volunteerapp.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.volunteerapp.model.Event
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// COLOR SCHEME MATCHING THE PROFILE SCREEN UI
private val LightBackground = Color(0xFFF2F2F7)
private val CardWhite = Color(0xFFFFFFFF)
private val PrimaryBlue = Color(0xFF3E4DB5)
private val TextGray = Color(0xFF7A7A7A)
private val SoftIconGray = Color(0xFFD1D1D6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    onLogout: () -> Unit
) {
    val db = Firebase.firestore
    var allEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val filteredEvents = remember(allEvents, searchQuery) {
        if (searchQuery.isBlank()) allEvents else allEvents.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        db.collection("opportunities").addSnapshotListener { snapshot, error ->
            isLoading = false
            if (error != null) return@addSnapshotListener
            if (snapshot != null) {
                allEvents = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", color = Color.Black) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = PrimaryBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Event", color = Color.White) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White) },
                containerColor = PrimaryBlue,
                onClick = { navController.navigate("addEvent") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(LightBackground)
        ) {

            KpiCards(events = allEvents)

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Search by title or location") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryBlue)
                },
                singleLine = true
            )

            when {
                isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
                filteredEvents.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (allEvents.isEmpty()) "No opportunities posted yet." else "No results found.")
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onEditClick = { navController.navigate("addEvent?eventId=${event.id}") },
                            onDeleteClick = { db.collection("opportunities").document(event.id).delete() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCards(events: List<Event>) {
    val totalSignups = events.sumOf { it.signups.size }
    val totalSpots = events.sumOf { (it.totalVolunteers ?: 0).toInt() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KpiCard("Total Events", events.size.toString(), Modifier.weight(1f))
        KpiCard("Total Sign-ups", "$totalSignups / $totalSpots", Modifier.weight(1f))
    }
}

@Composable
fun KpiCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = TextGray, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryBlue)
        }
    }
}

@Composable
fun EventCard(event: Event, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(event.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                StatusBadge(event.status)
            }

            Spacer(Modifier.height(6.dp))
            Text(event.description, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
            Divider(Modifier.padding(vertical = 12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("📅 ${event.date} at ${event.time}", style = MaterialTheme.typography.bodySmall)
                    Text("📍 ${event.location}", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "${event.signups.size} / ${event.totalVolunteers}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "active" -> Color(0xFF4CAF50)
        "full" -> Color(0xFFF44336)
        "ended" -> Color(0xFF607D8B)
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}