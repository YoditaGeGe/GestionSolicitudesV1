package com.example.gestionsolicitudes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Abyss,
    onPrimary = Color.White,
    primaryContainer = NebulaContainer,
    onPrimaryContainer = Abyss,
    secondary = Pulse,
    onSecondary = Color.White,
    secondaryContainer = PulseContainer,
    onSecondaryContainer = Abyss,
    tertiary = Nebula,
    onTertiary = Abyss,
    background = AppBackground,
    onBackground = Ink,
    surface = AppSurface,
    onSurface = Ink,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = MutedInk,
    outline = AppOutline,
    error = Color(0xFFD64545)
)

private val DarkColorScheme = darkColorScheme(
    primary = Nebula,
    onPrimary = Abyss,
    primaryContainer = Abyss,
    secondary = Pulse,
    onSecondary = Color.White,
    tertiary = Nebula,
    background = Color(0xFF011820),
    onBackground = Color(0xFFE5F6FA),
    surface = Color(0xFF012A38),
    onSurface = Color(0xFFE5F6FA),
    surfaceVariant = Color(0xFF013E52),
    onSurfaceVariant = Color(0xFFB7DDE6)
)

@Composable
fun GestionSolicitudesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
