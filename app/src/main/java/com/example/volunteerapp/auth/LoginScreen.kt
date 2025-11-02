package com.example.volunteerapp.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.volunteerapp.R // Import to access drawable resources
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisibility by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()), // Makes screen scrollable
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 1. App Logo and Title
            Image(
                // Use the vconnect_logo.png from your drawable folder
                painter = painterResource(id = R.drawable.vconnect_logo),
                contentDescription = "Volunteer Connect Logo",
                modifier = Modifier.size(120.dp)
            )

            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Sign in to continue your journey.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 2. Input Fields with Icons
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisibility) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(image, "Toggle password visibility")
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Loading State inside the Button
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        message = "Email and password cannot be empty."
                        return@Button
                    }
                    isLoading = true
                    message = ""

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            // In a real app, you'd fetch the role from Firestore here
                            // For now, we'll just assume "Volunteer"
                            isLoading = false
                            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess("Volunteer") // Pass role to MainActivity
                        }
                        .addOnFailureListener { e ->
                            message = "Login failed: ${e.message}"
                            isLoading = false
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sign In", fontSize = 16.sp)
                }
            }

            // 4. Improved "Don't have an account" button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Don't have an account?")
                TextButton(onClick = onNavigateToRegister, enabled = !isLoading) {
                    Text("Sign Up")
                }
            }

            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
