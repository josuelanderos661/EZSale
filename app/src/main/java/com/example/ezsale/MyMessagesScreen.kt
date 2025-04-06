package com.example.ezsale

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMessageScreen(navController: NavHostController) {
    val currentUser = Firebase.auth.currentUser ?: return
    val userId = currentUser.uid
    val database = Firebase.database.reference

    var chatSummaries by remember { mutableStateOf(listOf<ChatSummary>()) }

    LaunchedEffect(true) {
        // Start fetching the chat summaries
        database.child("messages").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val summaryList = mutableListOf<ChatSummary>()

                // Fetch listing titles and prices asynchronously
                snapshot.children.forEach { listingSnapshot ->
                    val listingId = listingSnapshot.key ?: return@forEach

                    // Fetch the listing title and price
                    database.child("listings").child(listingId).let { listingRef ->
                        listingRef.child("title").get().addOnSuccessListener { listingTitleSnapshot ->
                            var listingTitle = listingTitleSnapshot.getValue(String::class.java) ?: "Untitled"

                            // If title is "Untitled", update it and give option to delete chat
                            if (listingTitle == "Untitled") {
                                listingTitle = "Listing Was Deleted..."
                            }

                            listingRef.child("price").get().addOnSuccessListener { listingPriceSnapshot ->
                                val price = listingPriceSnapshot.getValue(String::class.java) ?: "0.00"

                                // Process the messages for each chat
                                listingSnapshot.children.forEach { chatSnapshot ->
                                    val chatId = chatSnapshot.key ?: return@forEach

                                    val (userA, _, userB) = chatId.split("_")
                                    val isInvolved = userA == userId || userB == userId
                                    if (!isInvolved) return@forEach

                                    val otherUserId = if (userA == userId) userB else userA

                                    // Grab the last message text for preview
                                    val lastMessage = chatSnapshot.children.lastOrNull()?.getValue(Message::class.java)
                                    val preview = lastMessage?.text ?: "No messages yet"

                                    // Add the chat summary with the listing title and price
                                    summaryList.add(ChatSummary(listingId, listingTitle, otherUserId, preview, price))
                                }

                                // After all titles and prices are fetched, update the state
                                chatSummaries = summaryList
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Messages") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            if (chatSummaries.isEmpty()) {
                Text("No messages yet.")
            } else {
                LazyColumn {
                    items(chatSummaries) { summary ->
                        Card(
                            onClick = {
                                navController.navigate("ChatScreen/${summary.listingId}/Chat/${summary.otherUserId}/${summary.price}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Listing: ${summary.listingTitle}")
                                Text("Price: $${summary.price}")
                                Text("Chat with: ${summary.otherUserId}")

                                // Show delete button if the listing was deleted
                                if (summary.listingTitle == "Listing Was Deleted...") {
                                    TextButton(onClick = {
                                        deleteMessagesForListing(summary.listingId)
                                        // Remove the deleted chat from the UI immediately
                                        chatSummaries = chatSummaries.filter { it.listingId != summary.listingId }
                                    }) {
                                        Text("Delete Chat")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun deleteMessagesForListing(listingId: String) {
    val database = Firebase.database.reference

    // Delete messages related to this listing
    database.child("messages").child(listingId).removeValue()
}

data class ChatSummary(
    val listingId: String,
    val listingTitle: String,
    val otherUserId: String,
    val preview: String,
    val price: String
)