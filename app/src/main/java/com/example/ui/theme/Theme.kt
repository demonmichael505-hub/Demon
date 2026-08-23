package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FrostedGlassColorScheme = darkColorScheme(
    primary = GlassRed,
    onPrimary = Color.White,
    primaryContainer = Color(0x33EF4444),
    onPrimaryContainer = Color.White,
    secondary = GlassOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0x26F97316),
    onSecondaryContainer = GlassOrange,
    tertiary = GlassEmerald,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0x2622C55E),
    onTertiaryContainer = Color.White,
    background = GlassDarkBg,
    onBackground = TextPrimary,
    surface = Color(0x12FFFFFF),
    onSurface = TextPrimary,
    surfaceVariant = Color(0x1CFFFFFF),
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = GlassRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FrostedGlassColorScheme,
        typography = Typography,
        content = content
    )
}

