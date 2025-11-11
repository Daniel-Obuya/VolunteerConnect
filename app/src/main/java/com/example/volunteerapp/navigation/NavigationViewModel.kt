package com.example.volunteerapp.navigation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// This enum defines all possible states for our app's navigation
enum class AuthState {
    LOADING,           // The app is checking the user's status
    UNAUTHENTICATED,   // The user is not logged in
    AUTHENTICATED_ADMIN, // The user is logged in as an Admin
    AUTHENTICATED_VOLUNTEER // The user is logged in as a Volunteer
}

class NavigationViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // _authState is the internal mutable state
    private val _authState = mutableStateOf(AuthState.LOADING)
    // authState is the public, unchangeable state the UI will observe
    val authState: State<AuthState> = _authState

    init {
        // Check the state when the app starts
        checkAuthState()
    }

    // --- THIS IS THE FIX ---
    // 1. Function is now public (no 'private' keyword).
    // 2. Renamed to 'checkAuthState' to match the call in MainActivity.
    // 3. Improved with modern '.await()' for cleaner async code.
    fun checkAuthState() {
        // Set to loading immediately to show feedback
        _authState.value = AuthState.LOADING

        viewModelScope.launch {
            // A small delay can prevent a jarring screen flash on app open.
            delay(300)

            val user = auth.currentUser
            if (user == null) {
                // If no user is logged in, they are unauthenticated.
                _authState.value = AuthState.UNAUTHENTICATED
            } else {
                // If a user is found, fetch their role from Firestore.
                try {
                    val document = db.collection("users").document(user.uid).get().await()
                    val role = document.getString("role")
                    if (role == "Admin") {
                        _authState.value = AuthState.AUTHENTICATED_ADMIN
                    } else {
                        _authState.value = AuthState.AUTHENTICATED_VOLUNTEER
                    }
                } catch (e: Exception) {
                    // If we can't get the role (e.g., network error), log them out to be safe.
                    auth.signOut()
                    _authState.value = AuthState.UNAUTHENTICATED
                }
            }
        }
    }
}
