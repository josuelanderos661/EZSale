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
fun ChatScreen(navController: NavHostController, listingId: String, title: String) {
    Log.d("ChatScreen", "Received listingId: $listingId, title: $title") // Log the received parameters

    val currentUser = Firebase.auth.currentUser ?: return
    val database = Firebase.database.reference

    val buyerId = currentUser.uid
    var sellerId by remember { mutableStateOf("") } // To store sellerId from the listing
    val chatId = "${buyerId}_to_${sellerId}"

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var isSellerFetched by remember { mutableStateOf(false) } // Track if sellerId is fetched

    // Fetch the listing data and sellerId
    LaunchedEffect(listingId) {
        Log.d("ChatScreen", "Fetching listing data for listingId: $listingId") // Log the listingId
        database.child("listings").child(listingId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listing = snapshot.getValue(Listing::class.java)
                    sellerId = listing?.userId ?: ""
                    isSellerFetched = true // Seller ID is now fetched
                    Log.d("ChatScreen", "Fetched sellerId: $sellerId for listingId: $listingId") // Log the fetched sellerId
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Fetch messages from Firebase once sellerId is fetched
    LaunchedEffect(listingId, chatId, isSellerFetched) {
        if (isSellerFetched && sellerId.isNotEmpty()) {  // Only proceed once sellerId is available
            Log.d("ChatScreen", "Fetching messages for chatId: $chatId")
            database.child("messages").child(listingId).child(chatId)
                .orderByChild("timestamp")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messageList = mutableListOf<Message>()
                        for (child in snapshot.children) {
                            val message = child.getValue(Message::class.java)
                            message?.let { messageList.add(it) }
                        }
                        messages = messageList
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$title - ${if (isSellerFetched) sellerId else "Loading..."}") }, // Display title and sellerId or "Loading"
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