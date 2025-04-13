package com.example.ezsale

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(navController: NavHostController) {
    val user = Firebase.auth.currentUser
    val userId = user?.uid
    val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("listings")

    var userListings by remember { mutableStateOf<List<Listing>>(emptyList()) }

    LaunchedEffect(userId) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allListings = snapshot.children.mapNotNull {
                    val listing = it.getValue(Listing::class.java)
                    listing?.apply { id = it.key ?: "" }
                }
                userListings = allListings.filter { it.userId == userId }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Listings", style = MaterialTheme.typography.titleLarge) },
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
                    Text("You have no listings. Would you like to create one?", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { navController.navigate("CreateListing") }) {
                        Text("Create a Listing")
                    }
                } else {
                    LazyColumn {
                        items(userListings) { listing ->
                            MyListingItem(listing = listing, navController = navController)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun MyListingItem(listing: Listing, navController: NavHostController) {
    val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("listings")
    var isImageClicked by remember { mutableStateOf(false) }
    val imageHeight = if (isImageClicked) 400.dp else 200.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (listing.imageUrl.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .clickable { isImageClicked = !isImageClicked }
                ) {
                    AndroidView(
                        factory = { context ->
                            ImageView(context).apply {
                                Glide.with(context).load(listing.imageUrl).into(this)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (listing.sold) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SOLD",
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    } else if (listing.pending) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(imageHeight / 2)
                                .align(Alignment.BottomCenter)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PENDING",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Text("No image available", style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$${listing.price}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("•", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    listing.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            }

            if (isImageClicked) {
                Text("Category: ${listing.category}", style = MaterialTheme.typography.bodyMedium)
                Text("Condition: ${listing.condition}", style = MaterialTheme.typography.bodyMedium)
                Text("Location: ${listing.location}", style = MaterialTheme.typography.bodyMedium)
                Text("Description: ${listing.description}", style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }

                Box {
                    Row(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                listing.sold -> "Sold"
                                listing.pending -> "Pending"
                                else -> "Available"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Expand Status",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .rotate(90f),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Available") },
                            onClick = {
                                val updates = mapOf("sold" to false, "pending" to false)
                                database.child(listing.id).updateChildren(updates)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pending") },
                            onClick = {
                                val updates = mapOf("sold" to false, "pending" to true)
                                database.child(listing.id).updateChildren(updates)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sold") },
                            onClick = {
                                val updates = mapOf("sold" to true, "pending" to false)
                                database.child(listing.id).updateChildren(updates)
                                expanded = false
                            }
                        )
                    }
                }

                IconButton(onClick = {
                    navController.navigate("EditListingScreen/${listing.id}")
                }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Listing")
                }
            }
        }
    }
}