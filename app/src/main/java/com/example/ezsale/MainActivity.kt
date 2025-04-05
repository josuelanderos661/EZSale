package com.example.ezsale

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ezsale.ui.theme.EZSaleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EZSaleTheme {
                // Set up NavController for navigation
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Apply innerPadding to the NavHost or any composable inside Scaffold
                    NavHost(
                        navController = navController,
                        startDestination = "SplashScreen",
                        modifier = Modifier.padding(innerPadding) // Apply padding here
                    ) {
                        composable("SplashScreen") {
                            SplashScreen(navController = navController)  // Your SplashScreen composable
                        }
                        composable("MainScreen") {
                            MainScreen(navController = navController)  // Your SplashScreen composable
                        }
                        composable("LoginScreen") {
                            LoginScreen(navController = navController)  // Your SplashScreen composable
                        }
                        composable("NewUser") {
                            NewUserScreen(navController = navController)  // Your SplashScreen composable
                        }
                        composable("ProfileScreen") {
                            ProfileScreen(navController = navController)  // Your SplashScreen composable
                        }
                        composable("CreateListing") {
                            CreateListing(navController = navController)  // Your SplashScreen composable
                        }
                        composable("ListingsScreen") {
                            ListingsScreen(navController = navController)  // Your SplashScreen composable
                        }
                        composable("MyListingsScreen") {
                            MyListingsScreen(navController = navController)  // Your SplashScreen composable
                        }
                        composable("MyMessageScreen") {
                            MyMessageScreen(navController = navController)
                        }

                        composable("EditListingScreen/{listingId}") { backStackEntry ->
                            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
                            Log.d("Navigation", "Received listingId: $listingId") // Debugging log
                            EditListingScreen(navController = navController, listingId = listingId)
                        }
                        composable("ChatScreen/{listingId}/{title}/{sellerId}") { backStackEntry ->
                            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
                            val title = backStackEntry.arguments?.getString("title") ?: ""
                            val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""

                            ChatScreen(
                                navController = navController,
                                listingId = listingId,
                                title = title,
                                sellerId = sellerId
                            )
                        }





                        // You can add more screens here for navigation
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EZSaleTheme {
        Greeting("Android")
    }
}
