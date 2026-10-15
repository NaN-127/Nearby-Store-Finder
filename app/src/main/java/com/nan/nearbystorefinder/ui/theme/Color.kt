package com.nan.nearbystorefinder.ui.theme

import androidx.compose.ui.graphics.Color

// Minimal Monochrome & High-Contrast Design System
// Eliminates "vibecoded" saturated colors in favor of a sleek, clean, minimal dark/white aesthetic.

// --- Dark Mode (Minimal Dark Screen) ---
val DarkBackground = Color(0xFF09090B)       // Deep slate-black
val DarkSurface = Color(0xFF141417)          // Crisp dark surface
val DarkSurfaceElevated = Color(0xFF1E1E22)  // Card / container elevation
val DarkOutline = Color(0xFF2E2E34)          // Subtle boundary border
val DarkOutlineVariant = Color(0xFF222226)

val DarkPrimary = Color(0xFFFFFFFF)          // High-contrast pure white for primary actions
val DarkOnPrimary = Color(0xFF09090B)        // Pitch dark label on primary
val DarkSecondary = Color(0xFFE4E4E7)

val DarkTextPrimary = Color(0xFFF4F4F6)      // Primary reading text
val DarkTextSecondary = Color(0xFFA1A1AA)    // Muted / secondary metadata
val DarkTextTertiary = Color(0xFF71717A)     // Inactive / placeholder text

// --- Light Mode (Minimal White Screen) ---
val LightBackground = Color(0xFFFFFFFF)      // Pure white
val LightSurface = Color(0xFFF8F9FA)         // Subtle off-white surface
val LightSurfaceElevated = Color(0xFFFFFFFF) // Crisp elevated white card
val LightOutline = Color(0xFFE4E4E7)         // Clean light gray border
val LightOutlineVariant = Color(0xFFEEEEF0)

val LightPrimary = Color(0xFF09090B)         // High-contrast pitch black for primary actions
val LightOnPrimary = Color(0xFFFFFFFF)       // Crisp pure white label on primary
val LightSecondary = Color(0xFF27272A)

val LightTextPrimary = Color(0xFF09090B)     // High-contrast deep black text
val LightTextSecondary = Color(0xFF71717A)   // Balanced slate gray
val LightTextTertiary = Color(0xFFA1A1AA)    // Subtle tertiary label

// --- Minimalist Status & Feedback Indicators (Subdued, Non-neon) ---
val StatusOpen = Color(0xFF22C55E)           // Subdued green
val StatusClosed = Color(0xFFEF4444)         // Subdued red
val NeutralRating = Color(0xFFE4E4E7)        // Minimal neutral for ratings in dark
val NeutralRatingLight = Color(0xFF52525B)   // Minimal neutral for ratings in light
