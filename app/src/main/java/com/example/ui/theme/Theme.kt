package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RedBrand,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1C1C1C),
    onPrimaryContainer = Color.White,
    secondary = WhiteSecondary,
    onSecondary = BlackPrimary,
    tertiary = OffWhiteThird,
    onTertiary = BlackPrimary,
    background = BlackPrimary,
    onBackground = WhiteSecondary,
    surface = GreyOffFourth,
    onSurface = WhiteSecondary,
    surfaceVariant = Color(0xFF151515),
    onSurfaceVariant = OffWhiteThird,
    outline = SlateBorder,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
