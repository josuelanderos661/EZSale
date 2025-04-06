package com.example.ezsale

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val user = Firebase.auth.currentUser
    var displayName by remember { mutableStateOf(user?.displayName ?: "Guest") }
    var isEditing by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(displayName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("ListingsScreen") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App logo above the greeting text
                Image(
                    painter = painterResource(id = R.drawable.ezsalelogo1),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(300.dp)
                        .padding(bottom = 16.dp)
                        .let {
                            if (displayName == "Guest") {
                                it.padding(top = 32.dp) // Push logo higher for guest users
                            } else {
                                it
                            }
                        }
                )

                // Check if the user is logged in as a guest
                if (displayName == "Guest") {
                    // Guest User - Only Sign In option
                    Text("Hello Guest!", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { navController.navigate("LoginScreen") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Sign In")
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    TextButton(
                        onClick = { navController.navigate("LoginScreen") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "New user? Create an account", color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    // Logged-in user - Greeting and options to edit name
                    if (isEditing) {
                        // Editing mode: User can change display name
                        Text("Enter a new display name:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Display Name") }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row {
                            Button(
                                onClick = {
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(newName)
                                        .build()

                                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            displayName = newName
                                            isEditing = false
                                        }
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { isEditing = false }) {
                                Text("Cancel")
                            }
                        }
                    } else {
                        // Normal mode: Show greeting and edit option
                        Text(
                            text = "Hello, $displayName!",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { isEditing = true }) {
                            Text("Edit Name")
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        // Create Listing Button
                        Button(
                            onClick = { navController.navigate("CreateListing") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Create a Listing")
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        // My Listings Button
                        Button(
                            onClick = { navController.navigate("MyListingsScreen") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "My Listings")
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { navController.navigate("MyMessageScreen") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("My Messages")
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        // Log Out Button
                        Button(
                            onClick = {
                                Firebase.auth.signOut()
                                navController.navigate("MainScreen") {
                                    popUpTo("ProfileScreen") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Log Out")
                        }
                    }
                }
            }
        }
    )
}