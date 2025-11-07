package com.example.volunteerapp // Make sure this package name matches your project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.volunteerapp.auth.LoginScreen
import com.example.volunteerapp.auth.ProfileScreen
import com.example.volunteerapp.auth.RegisterScreen
import com.example.volunteerapp.ui.theme.VolunteerAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.ExperimentalMaterial3Api // <-- 1. Add this import
import com.example.volunteerapp.opportunities.OpportunitiesScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VolunteerAppTheme {
                // --- State Management for Navigation ---
                var currentScreen by remember { mutableStateOf("login") }
                var currentUserId by remember { mutableStateOf<String?>(null) }
                var currentUserRole by remember { mutableStateOf<String?>(null) }

                // --- Check for logged-in user on app start ---
                LaunchedEffect(Unit) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        currentUserId = user.uid
                        // Fetch role on startup to handle app resume
                        FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                val role = document.getString("role") ?: "Volunteer"
                                currentUserRole = role
                                // Decide where to go based on role
                                currentScreen = if (role == "Admin") "admin_dashboard" else "profile"
                            }.addOnFailureListener {
                                // If role fetch fails, default to profile screen
                                currentScreen = "profile"
                            }
                    }
                }

                // --- Navigation Logic ---
                when (currentScreen) {
                    "login" -> LoginScreen(
                        onLoginSuccess = { uid, role ->
                            currentUserId = uid
                            currentUserRole = role
                            currentScreen = if (role == "Admin") "admin_dashboard" else "opportunities"
                        },
                        onNavigateToRegister = {
                            currentScreen = "register"
                        }
                    )
                    "register" -> RegisterScreen(
                        onRegisterSuccess = {
                            currentScreen = "login"
                        },
                        onNavigateToLogin = {
                            currentScreen = "login"
                        }
                    )
                    "profile" -> {
                        // Ensure userId is not null before showing ProfileScreen
                        currentUserId?.let { uid ->
                            ProfileScreen(
                                userId = uid,
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    currentUserId = null
                                    currentUserRole = null
                                    currentScreen = "login"
                                },
                                onNavigateToOpportunities = {
                                    currentScreen = "opportunities"
                                }
                            )
                        }
                    }
                    "opportunities" -> {
                        currentUserId?.let { uid ->
                            OpportunitiesScreen(
                                userId = uid,
                                // The back button can take you to the profile screen
                                onNavigateToProfile = { currentScreen = "profile" }
                            )
                        }
                    }
                    "admin_dashboard" -> {
                        // This is a placeholder for your AdminDashboardScreen
                        // You would put your AdminDashboardScreen composable call here
                        // For now, let's add a simple placeholder with a header
                        @OptIn(ExperimentalMaterial3Api::class)
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { AppHeader() },
                                    actions = {
                                        IconButton(onClick = {
                                            FirebaseAuth.getInstance().signOut()
                                            currentUserId = null
                                            currentUserRole = null
                                            currentScreen = "login"
                                        }) {
                                            Icon(Icons.Default.ExitToApp, "Logout")
                                        }
                                    }
                                )
                            }
                        ) { padding ->
                            // Your Admin content would go here
                            Row(modifier = Modifier.padding(padding).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                Text("Admin Dashboard Content", modifier = Modifier.padding(top = 32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A reusable composable for the app's header, displaying the logo and name.
 * You would have already added this to your other screen files (`LoginScreen.kt`, etc.)
 * as previously instructed.
 */
@Composable
fun AppHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.vconnect_logo), // Make sure this resource name is correct
            contentDescription = "App Logo",
            modifier = Modifier.size(40.dp) // Adjust size as needed
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "VConnect", // Your App Name
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
