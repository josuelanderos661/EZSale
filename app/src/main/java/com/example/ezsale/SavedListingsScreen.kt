package com.example.ezsale

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedListingsScreen(navController: NavHostController) {
    val userId = Firebase.auth.currentUser?.uid
    val listingsRef = Firebase.database.reference.child("listings")
    val savedRef = Firebase.database.reference.child("savedListings")

    var savedListings by remember { mutableStateOf<List<Listing>>(emptyList()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            savedRef.child(userId).get().addOnSuccessListener { snapshot ->
                val savedIds = snapshot.children.mapNotNull { it.key }

                listingsRef.get().addOnSuccessListener { listingSnapshot ->
                    val allListings = listingSnapshot.children.mapNotNull { data ->
                        val listing = data.getValue(Listing::class.java)
                        listing?.apply { id = data.key ?: "" }
                    }

                    savedListings = allListings.filter { it.id in savedIds }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Listings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (savedListings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("You haven't saved any listings yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(savedListings) { listing ->
                    ListingItem(listing = listing, currentUserId = userId, navController = navController)
                }
            }
        }
    }
}