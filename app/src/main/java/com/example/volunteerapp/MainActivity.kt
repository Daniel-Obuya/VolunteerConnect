package com.example.volunteerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.volunteerapp.auth.LoginScreen // 👈 **1. Add this import**
import com.example.volunteerapp.auth.ProfileScreen // 👈 **2. Add this import**
import com.example.volunteerapp.auth.RegisterScreen
import com.example.volunteerapp.ui.theme.VolunteerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VolunteerAppTheme {

                var currentScreen by remember { mutableStateOf("login") }
                var userRole by remember { mutableStateOf("") }

                when (currentScreen) {

                    // 🔹 LOGIN SCREEN
                    "login" -> LoginScreen( // 👈 **3. Corrected function call**
                        onLoginSuccess = { role ->
                            userRole = role
                            currentScreen = "profile"
                        },
                        onNavigateToRegister = {
                            currentScreen = "register"
                        }
                    )

                    // 🔹 REGISTER SCREEN (This one was already correct)
                    "register" -> RegisterScreen(
                        onRegisterSuccess = {
                            // Let's navigate to login to force the user to sign in
                            currentScreen = "login"
                        },
                        onNavigateToLogin = {
                            currentScreen = "login"
                        }
                    )

                    // 🔹 PROFILE SCREEN
                    "profile" -> ProfileScreen( // 👈 **4. Corrected function call**
                        onLogout = {
                            currentScreen = "login"
                        }
                    )
                }
            }
        }
    }
}

