package com.example.ezsale

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListing(navController: NavHostController) {
    val user = Firebase.auth.currentUser

    if (user == null) {
        navController.navigate("LoginScreen") {
            popUpTo("CreateListing") { inclusive = true }
        }
        return
    }

    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") } // Location state
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedImageUri = uri }
    )

    val categories = listOf(
        "Electronics", "Furniture", "Video Games", "Clothing & Accessories",
        "Home & Kitchen", "Toys & Games", "Tools & Garden", "Sports & Outdoors",
        "Books, Movies & Music", "Baby & Kids", "Miscellaneous"
    )

    val locations = listOf("Santa Barbara", "Ventura", "Camarillo", "Oxnard") // Location options

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create a Listing") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("ProfileScreen") }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Create a Listing", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(10.dp))

            TextField(value = title, onValueChange = { title = it }, label = { Text("Item Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            TextField(value = price, onValueChange = { price = it }, label = { Text("Item Price") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            val conditionOptions = listOf("New", "Like New", "Good", "Fair")
            var expandedCondition by remember { mutableStateOf(false) }

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

            // Location Dropdown
            var expandedLocation by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expandedLocation,
                onExpandedChange = { expandedLocation = !expandedLocation },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = location,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Location") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLocation)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedLocation,
                    onDismissRequest = { expandedLocation = false }
                ) {
                    locations.forEach { locationOption ->
                        DropdownMenuItem(
                            text = { Text(locationOption) },
                            onClick = {
                                location = locationOption
                                expandedLocation = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))

            // Category Dropdown
            val categoryOptions = categories
            var expandedCategory by remember { mutableStateOf(false) }

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
                    categoryOptions.forEach { selectionOption ->
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

            Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text("Select Image")
            }
            Spacer(modifier = Modifier.height(10.dp))

            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(8.dp)
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
                    uploadListing(user.uid, title, price, category, condition, description, location, selectedImageUri) { success, message ->
                        isLoading = false
                        if (success) {
                            navController.navigate("MyListingsScreen")
                        } else {
                            errorMessage = message
                        }
                    }
                },
                enabled = !isLoading && title.isNotEmpty() && price.isNotEmpty() &&
                        category.isNotEmpty() && condition.isNotEmpty() && description.isNotEmpty() && location.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isLoading) "Saving..." else "Create Listing")
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
    location: String, // Location parameter
    imageUri: Uri?,
    onComplete: (Boolean, String?) -> Unit
) {
    val database = Firebase.database.reference
    val listingId = UUID.randomUUID().toString()

    if (imageUri != null) {
        val storageRef = Firebase.storage.reference.child("listings/$listingId.jpg")

        storageRef.putFile(imageUri).continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: Exception("Image upload failed")
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveListingToDatabase(userId, title, price, category, condition, description, location, task.result.toString(), listingId, onComplete)
            } else {
                onComplete(false, "Image upload failed")
            }
        }
    } else {
        saveListingToDatabase(userId, title, price, category, condition, description, location, "", listingId, onComplete)
    }
}

fun saveListingToDatabase(
    userId: String,
    title: String,
    price: String,
    category: String,
    condition: String,
    description: String,
    location: String, // Add location to the database
    imageUrl: String,
    listingId: String,
    onComplete: (Boolean, String?) -> Unit
) {
    val database = Firebase.database.reference
    val listing = mapOf(
        "title" to title,
        "price" to price,
        "category" to category,
        "condition" to condition,
        "description" to description,
        "userId" to userId,
        "imageUrl" to imageUrl,
        "location" to location // Save the location in the database
    )

    database.child("listings").child(listingId).setValue(listing)
        .addOnSuccessListener { onComplete(true, null) }
        .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
}