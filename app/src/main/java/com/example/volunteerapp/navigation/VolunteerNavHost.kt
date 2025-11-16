package com.example.volunteerapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.volunteerapp.auth.ProfileScreen
import com.example.volunteerapp.opportunities.OpportunitiesScreen
import com.example.volunteerapp.opportunities.RegisteredOpportunitiesScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun VolunteerNavHost() {
    val navController = rememberNavController()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        bottomBar = {
            if (userId.isNotBlank()) {
                VolunteerBottomBar(navController, userId)
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "opportunities",
            modifier = Modifier.padding(padding)
        ) {

            // ---- Opportunities Screen ----
            composable("opportunities") {
                OpportunitiesScreen(
                    userId = userId,
                    onNavigateToProfile = { navController.navigate("profile/$userId") },
                    onNavigateToMyEvents = { navController.navigate("myEvents") }
                )
            }

            // ---- Profile Screen ----
            composable(
                route = "profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val currentUserId = backStackEntry.arguments?.getString("userId") ?: ""
                ProfileScreen(
                    userId = currentUserId,
                    onNavigateToOpportunities = { navController.navigate("opportunities") },
                    onNavigateToMyEvents = { navController.navigate("myEvents") },
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("opportunities") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                )
            }

            // ---- Registered Events Screen ----
            composable("myEvents") {
                RegisteredOpportunitiesScreen(
                    onBackToProfile = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun VolunteerBottomBar(navController: NavHostController, userId: String) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (currentRoute != "myEvents") {
        NavigationBar {
            NavigationBarItem(
                selected = currentRoute == "opportunities",
                onClick = { navController.navigate("opportunities") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }},
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Find Opportunities") },
                label = { Text("Find Opportunities") }
            )

            NavigationBarItem(
                selected = currentRoute?.startsWith("profile") == true,
                onClick = { navController.navigate("profile/$userId") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }},
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                label = { Text("Profile") }
            )
        }
    }
}
