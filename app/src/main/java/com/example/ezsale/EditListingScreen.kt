package com.example.ezsale

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListingScreen(navController: NavHostController, listingId: String) {
    Log.d("EditListingScreen", "Editing listing with ID: $listingId")
    val user = Firebase.auth.currentUser
    val database = Firebase.database.reference
    var listing by remember { mutableStateOf<Listing?>(null) }
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch the listing data when listingId changes
    LaunchedEffect(listingId) {
        listingId?.let {
            database.child("listings").child(it).get().addOnSuccessListener { snapshot ->
                listing = snapshot.getValue(Listing::class.java)
                listing?.let { item ->
                    title = item.title
                    price = item.price
                    category = item.category
                    condition = item.condition
                    description = item.description
                    selectedImageUri = item.imageUrl?.let { Uri.parse(it) }
                }
            }
        }
    }

    if (user == null) {
        navController.navigate("LoginScreen") {
            popUpTo("EditListingScreen") { inclusive = true }
        }
        return
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedImageUri = uri }
    )

    val categories = listOf(
        "Electronics", "Furniture", "Video Games", "Clothing & Accessories", "Home & Kitchen",
        "Toys & Games", "Tools & Garden", "Sports & Outdoors", "Books, Movies & Music", "Baby & Kids", "Miscellaneous"
    )

    val conditionOptions = listOf("New", "Like New", "Good", "Fair")
    var expandedCondition by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Listing") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("MyListingsScreen") }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Edit Listing", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(10.dp))

            // Title
            TextField(value = title, onValueChange = { title = it }, label = { Text("Item Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            // Price
            TextField(value = price, onValueChange = { price = it }, label = { Text("Item Price") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            // Condition Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCondition,
                onExpandedChange = { expandedCondition = !expandedCondition },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = condition,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Condition") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCondition)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedCondition,
                    onDismissRequest = { expandedCondition = false }
                ) {
                    conditionOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                condition = selectionOption
                                expandedCondition = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Description
            TextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Image Selection
            Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text("Select Image")
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Display the existing image or selected image
            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = "Selected Image",
                    modifier = Modifier.size(150.dp).padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null

                    uploadListing(
                        userId = user.uid,
                        title = title,
                        price = price,
                        category = category,
                        condition = condition,
                        description = description,
                        imageUri = selectedImageUri ?: Uri.parse(listing?.imageUrl),  // Use existing image if none selected
                        listingId = listingId
                    ) { success, message ->
                        isLoading = false
                        if (success) {
                            navController.navigate("MyListingsScreen")
                        } else {
                            errorMessage = message
                        }
                    }
                },
                enabled = !isLoading && title.isNotEmpty() && price.isNotEmpty() &&
                        category.isNotEmpty() && condition.isNotEmpty() && description.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isLoading) "Saving..." else "Update Listing")
            }

            var showDeleteDialog by remember { mutableStateOf(false) }
            Button(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Listing")
            }

            // Confirmation Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you sure you want to delete this post? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                deleteListing(listingId) { success, message ->
                                    if (success) {
                                        navController.navigate("MyListingsScreen")
                                    } else {
                                        errorMessage = message
                                    }
                                }
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

fun deleteListing(listingId: String, onComplete: (Boolean, String?) -> Unit) {
    val database = Firebase.database.reference
    val storage = Firebase.storage.reference

    // Get the listing data to retrieve the image URL
    database.child("listings").child(listingId).get().addOnSuccessListener { snapshot ->
        val listing = snapshot.getValue(Listing::class.java)

        // First, delete the listing from the database
        database.child("listings").child(listingId).removeValue().addOnSuccessListener {
            // If the listing had an image, delete it from storage
            listing?.imageUrl?.let { imageUrl ->
                val imageRef = storage.child("listings/$listingId.jpg")
                imageRef.delete().addOnSuccessListener {
                    onComplete(true, null) // Successfully deleted listing and image
                }.addOnFailureListener { e ->
                    onComplete(false, "Failed to delete image: ${e.localizedMessage}")
                }
            } ?: onComplete(true, null) // If no image, just complete successfully
        }.addOnFailureListener { e ->
            onComplete(false, "Failed to delete listing: ${e.localizedMessage}")
        }
    }.addOnFailureListener { e ->
        onComplete(false, "Failed to fetch listing: ${e.localizedMessage}")
    }
}

fun uploadListing(
    userId: String,
    title: String,
    price: String,
    category: String,
    condition: String,
    description: String,
    imageUri: Uri?,
    listingId: String,
    onComplete: (Boolean, String?) -> Unit
) {
    val database = Firebase.database.reference

    // Check for empty fields before proceeding
    if (title.isEmpty() || price.isEmpty() || category.isEmpty() || condition.isEmpty() || description.isEmpty()) {
        onComplete(false, "All fields must be filled out!")
        return
    }

    // Get the existing listing data to preserve the image URL if not updated
    database.child("listings").child(listingId).get().addOnSuccessListener { snapshot ->
        val existingListing = snapshot.getValue(Listing::class.java)

        // If there's an image to upload
        if (imageUri != null) {
            val storageRef = Firebase.storage.reference.child("listings/$listingId.jpg")

            storageRef.putFile(imageUri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Image upload failed")
                }
                // Get the download URL after the image is uploaded
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the image URL
                    val imageUrl = task.result.toString()

                    // Create an updated Listing object with the new imageUrl
                    val updatedListing = existingListing?.copy(
                        title = title,
                        price = price,
                        category = category,
                        condition = condition,
                        description = description,
                        userId = userId,
                        imageUrl = imageUrl  // Use the new imageUrl if image was uploaded
                    )

                    // Now update the listing in the database
                    database.child("listings").child(listingId).setValue(updatedListing)
                        .addOnSuccessListener {
                            onComplete(true, null)
                        }
                        .addOnFailureListener { e ->
                            onComplete(false, e.localizedMessage)
                        }
                } else {
                    onComplete(false, "Image upload failed")
                }
            }
        } else {
            // If no image is provided, just save the listing with the existing imageUrl
            val updatedListing = existingListing?.copy(
                title = title,
                price = price,
                category = category,
                condition = condition,
                description = description,
                userId = userId
                // No need to change the imageUrl
            )

            // Save the listing without modifying the image
            database.child("listings").child(listingId).setValue(updatedListing)
                .addOnSuccessListener {
                    onComplete(true, null)
                }
                .addOnFailureListener { e ->
                    onComplete(false, e.localizedMessage)
                }
        }
    }.addOnFailureListener { e ->
        onComplete(false, e.localizedMessage)
    }
}