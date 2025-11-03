package com.example.volunteerapp

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VolunteerAppTheme {

                var currentScreen by remember { mutableStateOf<String>("login") }
                // 🔹 Store the current User's ID
                var currentUserId by remember { mutableStateOf<String?>(null) }

                // Check if user is already logged in on app start
                LaunchedEffect(Unit) {
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                        currentScreen = "profile"
                    }
                }

                when (currentScreen) {
                    "login" -> LoginScreen(
                        // 🔹 Pass the UID and role on success
                        onLoginSuccess = { uid, role ->
                            currentUserId = uid
                            currentScreen = "profile"
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
                        // 🔹 Guard against null ID and pass it to the ProfileScreen
                        currentUserId?.let { uid ->
                            ProfileScreen(
                                userId = uid, // Pass the UID
                                onLogout = {
                                    currentUserId = null
                                    currentScreen = "login"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
