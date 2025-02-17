package com.example.ezsale

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(navController: NavHostController) {
    val user = Firebase.auth.currentUser
    val userId = user?.uid
    val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("listings")

    var userListings by remember { mutableStateOf<List<Listing>>(emptyList()) }

    LaunchedEffect(userId) {
        database.get().addOnSuccessListener { snapshot ->
            val allListings = snapshot.children.mapNotNull { it.getValue<Listing>() }
            userListings = allListings.filter { it.userId == userId }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("My Listings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("ProfileScreen") }) {
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
                if (userListings.isEmpty()) {
                    // Message when the user has no listings
                    Text(
                        text = "You have no listings. Would you like to create one?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    // Button to navigate to CreateListing screen
                    Button(onClick = { navController.navigate("CreateListing") }) {
                        Text("Create a Listing")
                    }
                } else {
                    // Show listings if available
                    LazyColumn {
                        items(userListings) { listing ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { /* Navigate to listing details if needed */ },
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = listing.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Price: $${listing.price}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = listing.description,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}