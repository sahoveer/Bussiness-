package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DukaanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF282864),
    onPrimaryContainer = Color(0xFFE0E0FF),
    secondary = DukaanSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF322368),
    onSecondaryContainer = Color(0xFFEADDFF),
    tertiary = DukaanSuccess,
    onTertiary = Color.White,
    background = DukaanDarkBg,
    onBackground = DukaanTextPrimaryDark,
    surface = DukaanDarkCard,
    onSurface = DukaanTextPrimaryDark,
    surfaceVariant = DukaanDarkSurfaceVariant,
    onSurfaceVariant = DukaanTextSecondaryDark,
    error = DukaanDanger,
    onError = Color.White,
    outline = Color(0xFF343854)
)

private val LightColorScheme = lightColorScheme(
    primary = DukaanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E5FF),
    onPrimaryContainer = Color(0xFF1D1D5B),
    secondary = DukaanSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE7FF),
    onSecondaryContainer = Color(0xFF2B1664),
    tertiary = DukaanSuccess,
    onTertiary = Color.White,
    background = DukaanLightBg,
    onBackground = DukaanTextPrimaryLight,
    surface = DukaanLightCard,
    onSurface = DukaanTextPrimaryLight,
    surfaceVariant = DukaanLightSurfaceVariant,
    onSurfaceVariant = DukaanTextSecondaryLight,
    error = DukaanDanger,
    onError = Color.White,
    outline = Color(0xFFD5D9EB)
)

val DukaanShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DukaanOSTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = DukaanShapes,
        content = content
    )
}
