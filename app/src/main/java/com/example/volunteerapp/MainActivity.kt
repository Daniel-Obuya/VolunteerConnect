package com.example.volunteerapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.volunteerapp.admin.AddEditEventActivity
import com.example.volunteerapp.auth.AdminDashboardScreen
import com.example.volunteerapp.opportunities.OpportunitiesScreen
import com.example.volunteerapp.auth.LoginScreen
import com.example.volunteerapp.auth.ProfileScreen
import com.example.volunteerapp.auth.RegisterScreen
import com.example.volunteerapp.navigation.AuthState
import com.example.volunteerapp.navigation.NavigationViewModel
import com.example.volunteerapp.ui.theme.VolunteerAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    // Removed the direct viewModel instance here, we will get it in the Composable
    // private val navigationViewModel: NavigationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VolunteerAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the ViewModel down from the top level
                    val navigationViewModel: NavigationViewModel = viewModel()
                    val authState by navigationViewModel.authState
                    AppNavigation(
                        authState = authState,
                        navigationViewModel = navigationViewModel // Pass the ViewModel instance
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    authState: AuthState,
    navigationViewModel: NavigationViewModel // Receive the ViewModel as a parameter
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

        composable("loading") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                // --- THIS IS THE FIX ---
                onLoginSuccess = { _, _ ->
                    // After a successful login, tell the ViewModel to re-check the auth state.
                    // This will trigger the state change and automatically navigate to the correct screen.
                    navigationViewModel.checkAuthState()
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") { popUpTo("register") { inclusive = true } }
                },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        composable("adminDashboard") {
            val context = LocalContext.current
            AdminDashboardScreen(
                onAddOpportunityClicked = {
                    val intent = Intent(context, AddEditEventActivity::class.java)
                    context.startActivity(intent)
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    // Also explicitly tell the ViewModel to update its state
                    navigationViewModel.checkAuthState()
                }
            )
        }

        composable("profile") {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            ProfileScreen(
                userId = userId,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    // Also explicitly tell the ViewModel to update its state
                    navigationViewModel.checkAuthState()
                },
                onNavigateToOpportunities = {
                    navController.navigate("allOpportunities")
                }
            )
        }

        composable("allOpportunities") {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            OpportunitiesScreen(
                userId = userId,
                onNavigateToProfile = {
                    navController.popBackStack()
                }
            )
        }
    }
}
