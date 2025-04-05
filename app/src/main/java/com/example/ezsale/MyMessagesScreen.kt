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
        database.child("messages").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val summaryList = mutableSetOf<ChatSummary>()

                snapshot.children.forEach { listingSnapshot ->
                    val listingId = listingSnapshot.key ?: return@forEach

                    listingSnapshot.children.forEach { chatSnapshot ->
                        val chatId = chatSnapshot.key ?: return@forEach

                        val (userA, _, userB) = chatId.split("_")

                        val isInvolved = userA == userId || userB == userId
                        if (!isInvolved) return@forEach

                        val otherUserId = if (userA == userId) userB else userA

                        // Grab latest message title for UI (optional — needs title stored in DB ideally)
                        val lastMessage = chatSnapshot.children.lastOrNull()?.getValue(Message::class.java)
                        val preview = lastMessage?.text ?: "No messages yet"

                        summaryList.add(ChatSummary(listingId, otherUserId, preview))
                    }
                }

                chatSummaries = summaryList.toList()
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
                                navController.navigate("ChatScreen/${summary.listingId}/Chat/${summary.otherUserId}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Listing: ${summary.listingId}")
                                Text("Chat with: ${summary.otherUserId}")
                                Text("Last message: ${summary.preview}")
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ChatSummary(
    val listingId: String,
    val otherUserId: String,
    val preview: String
)