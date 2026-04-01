package com.securekey.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Navy = Color(0xFF1A237E)
private val NavyLight = Color(0xFF534BAE)
private val Gold = Color(0xFFFFB300)
private val GoldLight = Color(0xFFFFE54C)
private val Background = Color(0xFFF5F5F5)
private val Surface = Color(0xFFFFFFFF)
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)

private val LightColorScheme = lightColorScheme(
    primary = Navy,
    primaryContainer = NavyLight,
    secondary = Gold,
    secondaryContainer = GoldLight,
    background = Background,
    surface = Surface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = NavyLight,
    primaryContainer = Navy,
    secondary = Gold,
    secondaryContainer = GoldLight,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun SecureKeyBankTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
