package com.biocap.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light (Parchment): warm ivory ground, navy primary action, gold accent — matches the logo.
// Dynamic color is intentionally OFF: the brand palette is the identity here, and letting the OEM
// wallpaper recolor the surface would drown the navy/gold logo (the very issue this design fixes).
private val LightColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Navy,
    onPrimaryContainer = Color.White,

    secondary = GoldDeep,
    onSecondary = Color.White,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = Navy,

    tertiary = GoldDeep,
    onTertiary = Color.White,
    tertiaryContainer = GoldSoft,
    onTertiaryContainer = Navy,

    background = Ivory,
    onBackground = Navy,
    surface = Ivory,
    onSurface = Navy,
    surfaceVariant = CardWhite,
    onSurfaceVariant = InkSubtle,
    outline = CardBorder,
    outlineVariant = CardBorder,
)

@Composable
fun BioCapTheme(
    content: @Composable () -> Unit
) {
    // The BioCap identity is the Parchment (light) design; the app is intentionally light-only, so
    // the operator UI is identical on every device regardless of the system dark-mode setting.
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
