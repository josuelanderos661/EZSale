package com.example.ezsale

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.firebase.database.database

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val userId = currentUser?.uid

    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    val isLoggedIn = currentUser != null

    val dbRef = if (isLoggedIn && userId != null) {
        Firebase.database.reference.child("users").child(userId)
    } else null

    val profileOptions = listOf("profilegrey", "profilered", "profilepurple", "profilepink", "profileblue")
    val colorLabels = mapOf(
        "profilegrey" to "Grey",
        "profilered" to "Red",
        "profilepurple" to "Purple",
        "profilepink" to "Pink",
        "profileblue" to "Green"
    )

    var selectedProfile by remember { mutableStateOf("profilegrey") }

    LaunchedEffect(userId) {
        if (isLoggedIn) {
            dbRef?.child("userProfile")?.get()?.addOnSuccessListener {
                selectedProfile = it.getValue(String::class.java) ?: "profilegrey"
            }
        }
    }

    var isEditing by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(displayName) }

    val profileDrawable = when (selectedProfile) {
        "profilered" -> R.drawable.profilered
        "profilepurple" -> R.drawable.profilepurple
        "profilepink" -> R.drawable.profilepink
        "profileblue" -> R.drawable.profileblue
        else -> R.drawable.profilegrey
    }

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
                Image(
                    painter = painterResource(id = profileDrawable),
                    contentDescription = "User Profile",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 16.dp)
                )

                if (!isLoggedIn) {
                    Text("Hello Guest!", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { navController.navigate("LoginScreen") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign In")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = { navController.navigate("LoginScreen") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("New user? Create an account")
                    }
                } else {
                    Text("Choose your profile color:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(10.dp))

                    var dropdownExpanded by remember { mutableStateOf(false) }

                    Box {
                        Button(onClick = { dropdownExpanded = true }) {
                            Text("Selected: ${colorLabels[selectedProfile] ?: "Grey"}")
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            profileOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(colorLabels[option] ?: option) },
                                    onClick = {
                                        selectedProfile = option
                                        dbRef?.child("userProfile")?.setValue(option)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isEditing) {
                        Text("Enter a new display name:")
                        Spacer(modifier = Modifier.height(10.dp))
                        TextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Display Name") }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row {
                            Button(onClick = {
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(newName)
                                    .build()

                                Firebase.auth.currentUser?.updateProfile(profileUpdates)
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            displayName = newName
                                            isEditing = false
                                        }
                                    }
                            }) {
                                Text("Save")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { isEditing = false }) {
                                Text("Cancel")
                            }
                        }
                    } else {
                        Text("Hello, ${displayName.ifBlank { "User" }}!", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { isEditing = true }) {
                            Text("Edit Name")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(onClick = { navController.navigate("CreateListing") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Create a Listing")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { navController.navigate("MyListingsScreen") }, modifier = Modifier.fillMaxWidth()) {
                        Text("My Listings")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { navController.navigate("SavedListingsScreen") }, modifier = Modifier.fillMaxWidth()) {
                        Text("Saved Listings")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { navController.navigate("MyMessageScreen") }, modifier = Modifier.fillMaxWidth()) {
                        Text("My Messages")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            Firebase.auth.signOut()
                            navController.navigate("MainScreen") {
                                popUpTo("ProfileScreen") { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log Out")
                    }
                }
            }
        }
    )
}