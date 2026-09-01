package com.lamamics.audioboooks.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bordeaux = Color(0xFF7A1F2B)
val BordeauxDark = Color(0xFF5E1620)
val Beige = Color(0xFFF7F0E2)
val BeigeSurface = Color(0xFFFDF8EC)
val BeigeVariant = Color(0xFFEFE4D0)
val BrownText = Color(0xFF3A2A26)
val BrownMuted = Color(0xFF6B5A50)

private val LightColors = lightColorScheme(
    primary = Bordeaux,
    onPrimary = Color(0xFFFFF8EE),
    primaryContainer = Color(0xFFF2DCD4),
    onPrimaryContainer = BordeauxDark,
    secondary = Color(0xFF8A7563),
    onSecondary = Color(0xFFFFF8EE),
    background = Beige,
    onBackground = BrownText,
    surface = BeigeSurface,
    onSurface = BrownText,
    surfaceVariant = BeigeVariant,
    onSurfaceVariant = BrownMuted,
    outline = Color(0xFFB9A891),
)

@Composable
fun AudioboooksTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
