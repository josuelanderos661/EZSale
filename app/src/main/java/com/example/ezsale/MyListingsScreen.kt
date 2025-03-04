package com.example.ezsale

import android.util.Log
import android.widget.ImageView
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
            val allListings = snapshot.children.mapNotNull {
                val listing = it.getValue<Listing>()
                if (listing != null) {
                    listing.id = it.key ?: ""  // Now you can modify the id directly
                }
                listing
            }
            userListings = allListings.filter { it.userId == userId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
                                    .clickable {
                                        // Navigate to EditListingScreen and pass the listingId
                                        Log.d("Navigation", "Listing object: $listing")
                                        if (listing.id.isNotEmpty()) {
                                            navController.navigate("EditListingScreen/${listing.id}")
                                        } else {
                                            Log.e("Navigation", "Attempted to navigate with empty listing ID: ${listing}")
                                        }

                                    },
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Display image only if the URL is valid
                                    if (listing.imageUrl.isNotEmpty()) {
                                        Log.d("MyListingsScreen", "Using image URL: ${listing.imageUrl}") // Log the image URL

                                        // Load the image with Glide using AndroidView
                                        AndroidView(
                                            factory = { context ->
                                                ImageView(context).apply {
                                                    Glide.with(context)
                                                        .load(listing.imageUrl) // The URL is passed here
                                                        .apply(RequestOptions().centerCrop())
                                                        .into(this)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    } else {
                                        Log.d("MyListingsScreen", "No image available for this listing")
                                        Text(text = "No image available", style = MaterialTheme.typography.bodySmall)
                                    }

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
                                        text = "Category: ${listing.category}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Condition: ${listing.condition}",
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