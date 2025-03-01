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
            TextField(value = title, onValueChange = { title = it }, label = { Text("Item Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            TextField(value = price, onValueChange = { price = it }, label = { Text("Item Price") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            TextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            TextField(value = condition, onValueChange = { condition = it }, label = { Text("Condition") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            TextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text("Select Image")
            }
            Spacer(modifier = Modifier.height(10.dp))

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
                        imageUri = selectedImageUri,
                        listingId = listingId ?: ""
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
        }
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
                val updatedListing = Listing(
                    id = listingId,
                    title = title,
                    price = price,
                    category = category,
                    condition = condition,
                    description = description,
                    userId = userId,
                    imageUrl = imageUrl,  // Use the new imageUrl
                    imagePath = storageRef.path  // Add the image path if needed
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
        // If no image is provided, just save the listing with an empty imageUrl
        val updatedListing = Listing(
            id = listingId,
            title = title,
            price = price,
            category = category,
            condition = condition,
            description = description,
            userId = userId,
            imageUrl = "",  // No image URL if no image is provided
            imagePath = ""  // Empty path if no image is provided
        )

        // Save the listing without an image
        database.child("listings").child(listingId).setValue(updatedListing)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.localizedMessage)
            }
    }
}
fun updateExistingListing(
    userId: String,
    title: String,
    price: String,
    category: String,
    condition: String,
    description: String,
    imageUri: Uri?,
    listingId: String, // You will pass the existing listingId
    onComplete: (Boolean, String?) -> Unit
) {
    // Call the updated uploadListing function
    uploadListing(userId, title, price, category, condition, description, imageUri, listingId, onComplete)
}

fun saveUpdatedListingToDatabase(
    listingId: String,
    updatedListing: Map<String, Any>,
    onComplete: (Boolean, String?) -> Unit
) {
    val database = Firebase.database.reference
    database.child("listings").child(listingId).updateChildren(updatedListing)
        .addOnSuccessListener { onComplete(true, null) }
        .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
}