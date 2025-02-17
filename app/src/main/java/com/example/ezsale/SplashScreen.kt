package com.example.ezsale

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun SplashScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Image
            Image(
                painter = painterResource(id = R.drawable.ezsalelogo1),
                contentDescription = "App Logo",
                modifier = Modifier.size(300.dp)
            )

        }
    }

    // Navigate to the main screen after a delay
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        navController.navigate("MainScreen") {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
    }
}