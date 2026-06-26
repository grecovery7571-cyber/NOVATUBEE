package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// High-Contrast Cinema Palette Specs requested by user:
val BlackPrimary = Color(0xFF000000)      // Primary colour black
val WhiteSecondary = Color(0xFFFFFFFF)    // Secondary white
val OffWhiteThird = Color(0xFFE5E5E5)      // Third white off (high-contrast off-white)
val GreyOffFourth = Color(0xFF272727)      // Then grey off theme container background / cards
val GreyText = Color(0xFF8E8E8E)           // Subtexts and captions

// Accent Red
val RedBrand = Color(0xFFFF0000)

// Semantic references
val DarkBackground = BlackPrimary
val DarkSurface = GreyOffFourth
val DarkSurfaceVariant = Color(0xFF151515) // Deep contrast background
val SlateBorder = Color(0xFF333333)        // Borders for high contrast
val Slate100 = WhiteSecondary
val Slate400 = OffWhiteThird
val Slate500 = GreyText
