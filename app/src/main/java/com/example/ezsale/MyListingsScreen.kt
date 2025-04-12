package com.example.ezsale

import android.widget.ImageView
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
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
                    listing.id = it.key ?: ""
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
    var isImageClicked by remember { mutableStateOf(false) }
    val imageHeight = if (isImageClicked) 400.dp else 200.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (listing.imageUrl.isNotEmpty()) {
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            Glide.with(context).load(listing.imageUrl).into(this)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .clickable { isImageClicked = !isImageClicked }
                )
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
                Text(listing.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            }

            if (isImageClicked) {
                Text("Category: ${listing.category}", style = MaterialTheme.typography.bodyMedium)
                Text("Condition: ${listing.condition}", style = MaterialTheme.typography.bodyMedium)
                Text("Location: ${listing.location}", style = MaterialTheme.typography.bodyMedium)
                Text("Description: ${listing.description}", style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    navController.navigate("EditListingScreen/${listing.id}")
                }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Listing")
                }
            }
        }
    }
}
