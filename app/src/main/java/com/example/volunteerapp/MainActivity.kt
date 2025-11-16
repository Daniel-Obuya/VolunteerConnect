package com.example.volunteerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.volunteerapp.admin.AddEditEventScreen
import com.example.volunteerapp.auth.AdminDashboardScreen
import com.example.volunteerapp.auth.LoginScreen
import com.example.volunteerapp.auth.ProfileScreen
import com.example.volunteerapp.auth.RegisterScreen
import com.example.volunteerapp.navigation.AuthState
import com.example.volunteerapp.navigation.NavigationViewModel
import com.example.volunteerapp.opportunities.OpportunitiesScreen
import com.example.volunteerapp.opportunities.RegisteredOpportunitiesScreen
import com.example.volunteerapp.ui.theme.VolunteerAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VolunteerAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navigationViewModel: NavigationViewModel = viewModel()
                    val authState by navigationViewModel.authState
                    // Pass the viewModel instance to AppNavigation
                    AppNavigation(authState, navigationViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    authState: AuthState,
    navigationViewModel: NavigationViewModel // Already receiving the instance here
) {
    val navController = rememberNavController()

    val startDestination = when (authState) {
        AuthState.LOADING -> "loading"
        AuthState.UNAUTHENTICATED -> "login"
        AuthState.AUTHENTICATED_ADMIN -> "adminDashboard"
        AuthState.AUTHENTICATED_VOLUNTEER -> "profile"
    }

    if (authState == AuthState.LOADING) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // Loading
        composable("loading") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Login
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { _, _ -> navigationViewModel.checkAuthState() }
            )
        }

        // Register
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        // Admin Dashboard
        composable("adminDashboard") {
            AdminDashboardScreen(
                navController = navController,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    // *** FIX: Call checkAuthState() on the viewModel instance ***
                    navigationViewModel.checkAuthState()
                }
            )
        }

        // Add/Edit Event
        composable("addEvent") {
            AddEditEventScreen(
                onSaved = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }

        // Profile
        composable("profile") {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            ProfileScreen(
                userId = userId,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navigationViewModel.checkAuthState()
                },
                onNavigateToOpportunities = { navController.navigate("opportunities") },
                onNavigateToMyEvents = { navController.navigate("registeredOpportunities") }
            )
        }

        // Opportunities
        composable("opportunities") {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            OpportunitiesScreen(
                userId = userId,
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToMyEvents = { navController.navigate("registeredOpportunities") }
            )
        }

        // Registered Opportunities
        composable("registeredOpportunities") {
            RegisteredOpportunitiesScreen(
                onBackToProfile = { navController.navigate("profile") }
            )
        }
    }
}
