package com.example.ezsale

import android.util.Log
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(navController: NavHostController) {
    val auth = Firebase.auth
    val database = Firebase.database.reference.child("listings")
    var listings by remember { mutableStateOf(emptyList<Listing>()) }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newListings = snapshot.children.mapNotNull { dataSnapshot ->
                    dataSnapshot.getValue(Listing::class.java)
                }.toMutableList()

                val updatedListings = mutableListOf<Listing>()
                val storageRef = Firebase.storage.reference

                // For each listing, resolve image URL if needed
                newListings.forEachIndexed { index, listing ->
                    Log.d("ListingsScreen", "Processing listing: $listing")  // Log the full listing to verify the imageUrl

                    if (listing.imageUrl.startsWith("gs://")) {
                        Log.d("ListingsScreen", "Found gs:// path: ${listing.imageUrl}")

                        val storageRef = Firebase.storage.getReferenceFromUrl(listing.imageUrl)
                        storageRef.downloadUrl
                            .addOnSuccessListener { uri ->
                                Log.d("ListingsScreen", "Image URL fetched successfully: $uri")
                                // Update the listing with resolved image URL
                                updatedListings.add(listing.copy(imageUrl = uri.toString()))
                            }
                            .addOnFailureListener { exception ->
                                Log.e("ListingsScreen", "Failed to fetch image URL: ${exception.message}")
                                updatedListings.add(listing)  // Keep original listing in case of failure
                            }
                    } else {
                        updatedListings.add(listing) // No need to change if already a valid URL
                    }
                }

                // Once all URLs are fetched, update listings state
                listings = updatedListings
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ListingsScreen", "Database error: ${error.message}")
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listings") },
                actions = {
                    IconButton(onClick = { navController.navigate("ProfileScreen") }) {
                        Image(
                            painter = rememberAsyncImagePainter(model = R.drawable.userlogo1),
                            contentDescription = "User Profile",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn {
                    items(listings) { listing ->
                        ListingItem(listing)
                    }
                }
            }
        }
    }
}

@Composable
fun ListingItem(listing: Listing) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Display image only if the URL is valid
            if (listing.imageUrl.isNotEmpty()) {
                Log.d("ListingItem", "Using image URL: ${listing.imageUrl}") // Log the image URL

                // Use Glide to load the image using AndroidView
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            Glide.with(context)
                                .load(listing.imageUrl) // The URL is passed here
                                .into(this)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Log.d("ListingItem", "No image available for this listing")
                Text(text = "No image available", style = MaterialTheme.typography.bodySmall)
            }

            Text(text = listing.title, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Price: $${listing.price}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Category: ${listing.category}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Condition: ${listing.condition}", style = MaterialTheme.typography.bodyMedium)
            Text(text = listing.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}