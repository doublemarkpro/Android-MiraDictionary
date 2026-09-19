package com.handdict.studyassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MiraNavy = Color(0xFF0B2B55)
val MiraBlue = Color(0xFF2368D5)
val MiraSky = Color(0xFFEAF4FF)
val MiraGreen = Color(0xFF3AA477)
val MiraOrange = Color(0xFFE18432)
val MiraBackground = Color(0xFFF7F8FA)

private val MiraColors = lightColorScheme(
    primary = MiraBlue,
    onPrimary = Color.White,
    primaryContainer = MiraSky,
    onPrimaryContainer = MiraNavy,
    secondary = MiraGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F5EC),
    onSecondaryContainer = Color(0xFF174C38),
    tertiary = MiraOrange,
    background = MiraBackground,
    onBackground = MiraNavy,
    surface = Color(0xFFFFFEFC),
    onSurface = MiraNavy,
    surfaceVariant = Color(0xFFF0F3F7),
    onSurfaceVariant = Color(0xFF4E6075),
    outline = Color(0xFFD6DEE8),
)

@Composable
fun MiraTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MiraColors,
        typography = MiraTypography,
        content = content,
    )
}

