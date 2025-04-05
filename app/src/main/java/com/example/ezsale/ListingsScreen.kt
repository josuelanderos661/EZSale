package com.example.ezsale

import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
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
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    val database = Firebase.database.reference.child("listings")
    var listings by remember { mutableStateOf(emptyList<Listing>()) }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newListings = snapshot.children.mapNotNull { dataSnapshot ->
                    val listing = dataSnapshot.getValue(Listing::class.java)?.apply {
                        // Ensure 'id' is fetched from Firebase as the snapshot key
                        id = dataSnapshot.key ?: ""
                    }

                    if (listing != null) {
                        Log.d("ListingsScreen", "Fetched Listing ID: ${listing.id}, User ID: ${listing.userId}, Title: ${listing.title}")
                    } else {
                        Log.e("ListingsScreen", "Failed to fetch listing from snapshot: ${dataSnapshot}")
                    }
                    listing // Return the listing after logging
                }

                listings = newListings // Assign the listings to the state variable
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
                        ListingItem(listing = listing, currentUserId = currentUserId, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun ListingItem(
    listing: Listing,
    currentUserId: String?,
    navController: NavHostController
) {
    Log.d("ListingsScreen", "Displaying listing: $listing")
    var isImageClicked by remember { mutableStateOf(false) }
    val imageHeight = if (isImageClicked) 400.dp else 200.dp
    val context = LocalContext.current // For Toast messages

    // Debugging: Log listing details
    Log.d("ListingItem", "Listing ID: ${listing.id}, User ID: ${listing.userId}, Title: ${listing.title}")

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
            if (listing.imageUrl.isNotEmpty()) {
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            Glide.with(context)
                                .load(listing.imageUrl)
                                .into(this)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .clickable {
                            isImageClicked = !isImageClicked
                        }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(text = listing.title, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Price: $${listing.price}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Category: ${listing.category}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Condition: ${listing.condition}", style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = listing.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                // Log to check listing.id and userId
                Log.d("ListingItem", "Listing ID: ${listing.id}, User ID: ${listing.userId}, Title: ${listing.title}")

                // Show button only if the listing is not owned by the current user
                if (currentUserId == null || listing.userId != currentUserId) {
                    IconButton(onClick = {
                        if (currentUserId == null) {
                            Toast.makeText(context, "You must be signed in to use this feature", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("ListingsScreen", "Navigating to ChatScreen with listingId: ${listing.id}, title: ${listing.title}, userId: ${listing.userId}")
                            // Passing listingId, title, and userId to the ChatScreen
                            navController.navigate("ChatScreen/${listing.id}/${listing.title}/${listing.userId}")
                            // Passing listingId and title
                        }
                    }) {
                        Image(
                            painter = rememberAsyncImagePainter(R.drawable.ic_message), // Make sure this drawable exists
                            contentDescription = "Message Seller"
                        )
                    }
                }
            }
        }
    }
}