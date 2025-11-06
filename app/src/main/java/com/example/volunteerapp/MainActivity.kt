package com.example.volunteerapp // Make sure this package name matches your project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.volunteerapp.auth.LoginScreen
import com.example.volunteerapp.auth.ProfileScreen
import com.example.volunteerapp.auth.RegisterScreen
import com.example.volunteerapp.ui.theme.VolunteerAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
                            currentScreen = if (role == "Admin") "admin_dashboard" else "profile"
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
                                    currentScreen = "opportunities_list"
                                }
                            )
                        }
                    }
                    "opportunities_list" -> {
                        com.example.volunteerapp.opportunities.OpportunitiesScreen(
                            onBackToProfile = { currentScreen = "profile" }
                        )
                    }
                    "admin_dashboard" -> {
                        // This is a placeeholder for your AdminDashboardScreen
                        // You would put your AdminDashboardScreen composable call here
                    }
                }
            }
        }
    }
}
