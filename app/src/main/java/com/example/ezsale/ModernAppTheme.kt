package com.example.ezsale

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material3.Typography
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Define your color scheme
private val DarkColorPalette = darkColorScheme(
    primary = Color(0xFFB0BEC5), // light grey-blue
    secondary = Color(0xFF78909C), // medium grey-blue
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorPalette = lightColorScheme(
    primary = Color(0xFF424242),       // dark grey for buttons/titles
    onPrimary = Color.White,

    secondary = Color(0xFF9E9E9E),     // lighter grey for accents
    onSecondary = Color.Black,

    background = Color(0xFFF5F5F5),     // soft grey background
    onBackground = Color.Black,

    surface = Color(0xFFFFFFFF),       // white cards/sheets
    onSurface = Color.Black
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)

@Composable
fun ModernAppTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}