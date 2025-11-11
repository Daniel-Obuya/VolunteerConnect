package com.example.volunteerapp.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Data class to hold opportunity information
data class Opportunity(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val location: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onAddOpportunityClicked: () -> Unit,
    onLogout: () -> Unit
) {
    val db = Firebase.firestore
    var opportunities by remember { mutableStateOf<List<Opportunity>>(emptyList()) }

    // Fetch opportunities from Firestore when the screen is first composed
    LaunchedEffect(Unit) {
        db.collection("opportunities")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error, e.g., show a toast
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val opportunityList = snapshot.documents.map { doc ->
                        doc.toObject(Opportunity::class.java)!!.copy(id = doc.id)
                    }
                    opportunities = opportunityList
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Opportunity") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                onClick = onAddOpportunityClicked
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(opportunities) { opportunity ->
                OpportunityCard(opportunity = opportunity)
            }
        }
    }
}

@Composable
fun OpportunityCard(opportunity: Opportunity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = opportunity.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = opportunity.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Date: ${opportunity.date}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Location: ${opportunity.location}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
