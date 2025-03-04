package com.example.ezsale

import android.net.Uri
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
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedImageUri = uri }
    )

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
                    uploadListing(user.uid, title, price, category, condition, description, selectedImageUri) { success, message ->
                        isLoading = false
                        if (success) {
                            navController.navigate("ProfileScreen")
                        } else {
                            errorMessage = message
                        }
                    }
                },
                enabled = !isLoading && title.isNotEmpty() && price.isNotEmpty() &&
                        category.isNotEmpty() && condition.isNotEmpty() && description.isNotEmpty(),
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
                saveListingToDatabase(userId, title, price, category, condition, description, task.result.toString(), listingId, onComplete)
            } else {
                onComplete(false, "Image upload failed")
            }
        }
    } else {
        saveListingToDatabase(userId, title, price, category, condition, description, "", listingId, onComplete)
    }
}

fun saveListingToDatabase(
    userId: String,
    title: String,
    price: String,
    category: String,
    condition: String,
    description: String,
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
        "imageUrl" to imageUrl
    )

    database.child("listings").child(listingId).setValue(listing)
        .addOnSuccessListener { onComplete(true, null) }
        .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
}