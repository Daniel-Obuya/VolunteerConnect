package com.example.volunteerapp.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome, ${user?.email}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                auth.signOut()
                onLogout()
            }) {
                Text("Logout")
            }
        }
    }
}
