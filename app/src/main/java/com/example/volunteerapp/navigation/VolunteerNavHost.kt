package com.example.volunteerapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.volunteerapp.myevents.MyEventsScreen
import com.example.volunteerapp.opportunities.OpportunitiesScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun VolunteerNavHost() {
    val navController = rememberNavController()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        bottomBar = { VolunteerBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "opportunities",
            modifier = Modifier.padding(padding)
        ) {
            composable("opportunities") {
                OpportunitiesScreen(
                    userId = userId,
                    onNavigateToProfile = { navController.navigate("my_events") }
                )
            }
            composable("my_events") {
                MyEventsScreen()
            }
        }
    }
}

@Composable
fun VolunteerBottomBar(navController: NavHostController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "opportunities",
            onClick = {
                navController.navigate("opportunities") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Opportunities") },
            label = { Text("Opportunities") }
        )
        NavigationBarItem(
            selected = currentRoute == "my_events",
            onClick = {
                navController.navigate("my_events") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Event, contentDescription = "My Events") },
            label = { Text("My Events") }
        )
    }
}
