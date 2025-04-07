package com.example.ezsale

import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(navController: NavHostController) {
    val auth = Firebase.auth
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val database = Firebase.database.reference.child("listings")

    var listings by remember { mutableStateOf(emptyList<Listing>()) }
    var filteredListings by remember { mutableStateOf(emptyList<Listing>()) }

    var selectedCategory by remember { mutableStateOf("") }
    var selectedPrice by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("") }
    var filterHeaderExpanded by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newListings = snapshot.children.mapNotNull { dataSnapshot ->
                    val listing = dataSnapshot.getValue(Listing::class.java)?.apply {
                        id = dataSnapshot.key ?: ""
                    }
                    listing
                }
                listings = newListings
                filteredListings = filterListings(listings, selectedCategory, selectedPrice, selectedCondition, selectedLocation)
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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (filterHeaderExpanded) "Hide Filters" else "Show Filters",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .clickable { filterHeaderExpanded = !filterHeaderExpanded }
                        .padding(16.dp)
                )

                if (filterHeaderExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterDropdown("Category", selectedCategory,
                            listOf("Electronics", "Furniture", "Video Games", "Clothing & Accessories",
                                "Home & Kitchen", "Toys & Games", "Tools & Garden", "Sports & Outdoors",
                                "Books, Movies & Music", "Baby & Kids", "Miscellaneous")
                        ) {
                            selectedCategory = it
                        }

                        FilterDropdown("Price", selectedPrice,
                            listOf("Select Price", "Under $50", "$50 - $100", "$100 - $200", "Over $200")
                        ) {
                            selectedPrice = it
                        }

                        FilterDropdown("Condition", selectedCondition,
                            listOf("Select Condition", "New", "Like New", "Good", "Fair")
                        ) {
                            selectedCondition = it
                        }

                        FilterDropdown(
                            "Location",
                            selectedLocation,
                            listOf("Santa Barbara", "Ventura", "Camarillo", "Oxnard")
                        ) {
                            selectedLocation = it
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Clear Filters",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clickable {
                                    selectedCategory = ""
                                    selectedLocation = ""
                                    selectedPrice = ""
                                    selectedCondition = ""
                                    filteredListings = listings
                                }
                                .padding(16.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Add the clickable "Search" text here
                        Text(
                            text = "Search",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clickable {
                                    // Trigger search (filtering listings) when clicked
                                    filteredListings = filterListings(listings, selectedCategory, selectedPrice, selectedCondition, selectedLocation)
                                    filterHeaderExpanded = false // Collapse filter menu
                                }
                                .padding(16.dp)
                        )
                    }
                }

                // Button to return to all listings when no filtered listings
                if (filteredListings.isEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = "No listings match this criteria.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                // Reset filters to default values
                                selectedCategory = ""
                                selectedPrice = ""
                                selectedCondition = ""
                                selectedLocation = ""

                                // Reset the filtered listings to all listings
                                filteredListings = listings
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Return to Listings")
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredListings) { listing ->
                            ListingItem(listing = listing, currentUserId = currentUserId, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterDropdown(
    label: String,
    selectedOption: String,
    options: List<String>,
    onSelectOption: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(4.dp)) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Text(
            text = if (selectedOption.isEmpty()) "Select $label" else selectedOption,
            color = Color.Blue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(8.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onSelectOption(option)
                    expanded = false
                }) {
                    Text(option)
                }
            }
        }
    }
}

fun filterListings(
    listings: List<Listing>,
    selectedCategory: String,
    selectedPrice: String,
    selectedCondition: String,
    selectedLocation: String
): List<Listing> {
    return listings.filter { listing ->
        val priceInt = listing.price.toIntOrNull() ?: 0
        (selectedCategory.isEmpty() || listing.category == selectedCategory) &&
                (selectedCondition.isEmpty() || listing.condition == selectedCondition) &&
                (selectedPrice.isEmpty() || isPriceInRange(priceInt, selectedPrice)) &&
                (selectedLocation.isEmpty() || listing.location == selectedLocation)
    }
}

fun isPriceInRange(price: Int, selectedPrice: String): Boolean {
    return when (selectedPrice) {
        "Under $50" -> price < 50
        "$50 - $100" -> price in 50..100
        "$100 - $200" -> price in 100..200
        "Over $200" -> price > 200
        else -> true
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
                // Force image refresh by using the listing ID as a key
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
                        },
                    update = { view ->
                        // Using a unique key to force reload the image
                        Glide.with(view.context).load(listing.imageUrl).into(view)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Display the price with a floating dot and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Price with the dot in the middle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${listing.price}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal)
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Space between price and dot
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
                    ) // The dot
                    Spacer(modifier = Modifier.width(4.dp)) // Space between dot and title
                }

                // Title displayed to the right
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                    modifier = Modifier.weight(1f)
                )
            }

            // Show other details when the image is clicked
            if (isImageClicked) {
                Text(text = "Category: ${listing.category}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Condition: ${listing.condition}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Location: ${listing.location}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Description: ${listing.description}", style = MaterialTheme.typography.bodyMedium)
            }

            // Row for the message icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (currentUserId == null || listing.userId != currentUserId) {
                    Image(
                        painter = rememberAsyncImagePainter(R.drawable.ic_message),
                        contentDescription = "Message Seller",
                        modifier = Modifier
                            .size(45.dp)  // Customize size as needed
                            .clickable {
                                if (currentUserId == null) {
                                    Toast.makeText(context, "You must be signed in to use this feature", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.d("ListingsScreen", "Navigating to ChatScreen with listingId: ${listing.id}, title: ${listing.title}, userId: ${listing.userId}, price: ${listing.price}")
                                    // Pass listingId, title, userId, and price to the ChatScreen
                                    navController.navigate("ChatScreen/${listing.id}/${listing.title}/${listing.userId}/${listing.price}")
                                }
                            }
                    )

                }
            }
        }
    }
}