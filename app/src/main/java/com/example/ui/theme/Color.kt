package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Frosted Glass Aesthetic Palette
val GlassDarkBg = Color(0xFF0A0A0B)
val GlassSurface = Color(0x0FFFFFFF) // white/6%
val GlassSurfaceElevated = Color(0x1AFFFFFF) // white/10%
val GlassSurfaceHighlight = Color(0x26FFFFFF) // white/15%
val GlassBorder = Color(0x1FFFFFFF) // white/12%
val GlassBorderBright = Color(0x33FFFFFF) // white/20%
val GlassBorderSubtle = Color(0x0DFFFFFF) // white/5%

// Vibrant Gradient & Accent Colors from Design HTML
val GlassRed = Color(0xFFEF4444)
val GlassRedDark = Color(0xFFDC2626)
val GlassOrange = Color(0xFFF97316)
val GlassOrangeDark = Color(0xFFEA580C)
val GlassEmerald = Color(0xFF22C55E)
val GlassEmeraldBright = Color(0xFF4ADE80)
val GlassCyan = Color(0xFF38BDF8)
val GlassAmber = Color(0xFFF59E0B)
val GlassPurple = Color(0xFFA855F7)

// Backward compatible aliases
val DemonBlack = GlassDarkBg
val DemonDarkBg = GlassDarkBg
val DemonSurface = Color(0x12FFFFFF)
val DemonSurfaceElevated = Color(0x1CFFFFFF)
val DemonSurfaceBorder = GlassBorder
val DemonCrimson = GlassRed
val DemonCrimsonBright = Color(0xFFFF6B6B)
val DemonCrimsonDark = GlassRedDark
val DemonCrimsonGlow = Color(0x33EF4444)
val DemonCyan = GlassCyan
val DemonCyanGlow = Color(0x3338BDF8)
val DemonEmerald = GlassEmerald
val DemonEmeraldDark = Color(0xFF15803D)
val DemonEmeraldGlow = Color(0x3322C55E)
val DemonAmber = GlassAmber
val DemonPurple = GlassPurple

// Text hierarchy from Design
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val TextSubtle = Color(0xFF475569)

// Hero Frosted Card Gradient
val FrostedHeroGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x33450A0A), // red-950/20
        Color(0x260F172A), // slate-900/15
        Color(0x1A1E1B4B)  // indigo-950/10
    )
)

val FrostedButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFDC2626),
        Color(0xFFEA580C)
    )
)

val FrostedGreenGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF16A34A),
        Color(0xFF22C55E)
    )
)

val FrostedLogoGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFDC2626),
        Color(0xFFF97316)
    )
)

val BarrierUpper = Color(0xFFEF4444)
val BarrierLower = Color(0xFFEF4444)
val ChartGridLine = Color(0x14FFFFFF)

