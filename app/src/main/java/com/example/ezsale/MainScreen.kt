package com.example.ezsale

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun MainScreen(navController: NavHostController) {
    val sharedPreferences = LocalContext.current.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userToken = sharedPreferences.getString("userToken", null)
    val user = Firebase.auth.currentUser

    // Top Section with centered content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp)) // Space at the top

        Image(
            painter = painterResource(id = R.drawable.ezsalelogo),  // Your app logo or image
            contentDescription = "Main Screen image",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Welcome to EZ Sale!",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Check if the user is logged in or not
        if (user != null || userToken != null) {
            Text(
                text = "You are already logged in!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Please log in or continue as a guest",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Bottom Section - Buttons for Login or Continue as Guest
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Login Button (Now correctly labeled)
            Button(
                onClick = { navController.navigate("LoginScreen") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Login")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Continue as Guest Button
            TextButton(
                onClick = {
                    // Handle guest navigation (you may want to navigate to a different screen)
                    navController.navigate("GuestHomeScreen")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Continue as Guest", color = Color.Gray)
            }
        }
    }
}