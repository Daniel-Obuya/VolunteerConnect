package com.example.volunteerapp.myevents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.volunteerapp.model.Event
import com.example.volunteerapp.viewmodel.MyEventsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEventsScreen(
    viewModel: MyEventsViewModel = viewModel()
) {
    val events by viewModel.myEvents.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Registered Events") }
            )
        }
    ) { padding ->

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("You haven't registered for any events yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        onUnregister = { viewModel.unregisterFromEvent(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onUnregister: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(event.description)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Date: ${event.date}")
            Text("Location: ${event.location}")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onUnregister,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Unregister")
            }
        }
    }
}
