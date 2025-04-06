package com.example.ezsale

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavHostController, listingId: String, title: String, sellerId: String, price: String) {
    Log.d("ChatScreen", "Received listingId: $listingId, title: $title, sellerId: $sellerId, price: $price")

    val currentUser = Firebase.auth.currentUser ?: return
    val database = Firebase.database.reference

    val buyerId = currentUser.uid
    val chatId = listOf(buyerId, sellerId).sorted().joinToString("_with_")

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }

    var listingTitle by remember { mutableStateOf(title) }

    // This will trigger when the `listingId` or `title` changes.
    LaunchedEffect(listingId) {
        // If the title is passed as "Chat" or is blank, fetch it from Firebase
        if (title == "Chat" || title.isBlank()) {
            Firebase.database.reference.child("listings").child(listingId).child("title")
                .get().addOnSuccessListener {
                    listingTitle = it.getValue(String::class.java) ?: "Chat"
                }
        } else {
            listingTitle = title // Otherwise, use the passed title
        }
    }

    DisposableEffect(listingId, chatId) {
        Log.d("ChatScreen", "Setting up listener for chatId: $chatId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    message?.let { messageList.add(it) }
                }
                messages = messageList
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        val chatRef = database.child("messages").child(listingId).child(chatId).orderByChild("timestamp")
        chatRef.addValueEventListener(listener)

        // Cleanup when the Composable leaves the Composition
        onDispose {
            Log.d("ChatScreen", "Removing listener for chatId: $chatId")
            chatRef.ref.removeEventListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        // Title and price display in the format "Title - $Price"
                        Text(text = "$listingTitle - $$price", style = MaterialTheme.typography.titleMedium)
                        // Seller ID below the title and price
                        Text(text = "Seller ID: $sellerId", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { message ->
                    MessageBubble(message, isMe = message.senderId == buyerId)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                Button(onClick = {
                    if (messageText.isNotEmpty()) {
                        sendMessage(database, listingId, chatId, buyerId, messageText)
                        messageText = ""
                    }
                }) {
                    Text("Send")
                }
            }
        }
    }
}

// Send message to Firebase
fun sendMessage(database: DatabaseReference, listingId: String, chatId: String, senderId: String, text: String) {
    val messageId = UUID.randomUUID().toString()
    val message = Message(senderId, text, System.currentTimeMillis())

    database.child("messages").child(listingId).child(chatId).child(messageId).setValue(message)
}

// Message data class
data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0
)

// Display message bubble
@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.align(alignment)) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
                color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}