package com.example.ezsale

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth

@Composable
fun ProfileScreen(navController: NavHostController) {
    val user = Firebase.auth.currentUser
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var isEditing by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(displayName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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

            // View Listings Button
            Button(
                onClick = { navController.navigate("ListingsScreen") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "View Listings")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { navController.navigate("MyListingsScreen") }, // Ensure this route is correctly defined in your NavGraph
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "My Listings")
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