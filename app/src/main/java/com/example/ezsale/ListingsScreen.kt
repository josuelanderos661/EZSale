package com.example.ezsale

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


@Composable
fun ListingsScreen(navController: NavHostController) {
    val database = Firebase.database.reference.child("listings")
    var listings by remember { mutableStateOf(emptyList<Listing>()) }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newListings = snapshot.children.mapNotNull { it.getValue(Listing::class.java) }
                listings = newListings
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ListingsScreen", "Database error: ${error.message}")
            }
        })
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Listings", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listings) { listing ->
                ListingItem(listing)
            }
        }
    }
}

@Composable
fun ListingItem(listing: Listing) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = listing.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Price: ${listing.price}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Category: ${listing.category}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Condition: ${listing.condition}", style = MaterialTheme.typography.bodySmall)
            Text(text = listing.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
