package com.example.volunteerapp.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

data class Opportunity(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onLogout: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var opportunities by remember { mutableStateOf<List<Opportunity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch all opportunities
    LaunchedEffect(Unit) {
        db.collection("opportunities").get()
            .addOnSuccessListener { snapshot ->
                opportunities = snapshot.toObjects(Opportunity::class.java)
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Opportunity") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = {
                    // TODO: navigate to AddOpportunityScreen if you create it later
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (opportunities.isEmpty()) {
                Text("No opportunities available.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(opportunities) { opp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(opp.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("Date: ${opp.date}")
                                Text("Location: ${opp.location}")
                                Spacer(Modifier.height(6.dp))
                                Text(opp.description, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
