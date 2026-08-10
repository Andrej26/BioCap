@file:OptIn(ExperimentalTextApi::class)

package com.biocap.app.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.biocap.app.R

// ── Manrope: the BioCap brand typeface ──
// A single variable-font file (res/font/manrope_variable.ttf) instanced at the weights the UI uses,
// via FontVariation. On API 26+ the axis is honored; on API 24/25 (where variable fonts aren't
// supported) Compose loads the font's default (Regular) instance — an acceptable fallback.
private fun manrope(weight: FontWeight) = Font(
    resId = R.font.manrope_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

private val Manrope = FontFamily(
    manrope(FontWeight.Normal),      // 400
    manrope(FontWeight.Medium),      // 500
    manrope(FontWeight.SemiBold),    // 600
    manrope(FontWeight.Bold),        // 700
    manrope(FontWeight.ExtraBold),   // 800
)

// Material 3 type scale, retargeted to Manrope. Sizes/line-heights follow the M3 defaults; weights
// lean a touch heavier on titles and labels to match the Parchment home mock.
private val Default = Typography()

val Typography = Typography(
    displayLarge = Default.displayLarge.copy(fontFamily = Manrope),
    displayMedium = Default.displayMedium.copy(fontFamily = Manrope),
    displaySmall = Default.displaySmall.copy(fontFamily = Manrope),

    headlineLarge = Default.headlineLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    headlineMedium = Default.headlineMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    headlineSmall = Default.headlineSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),

    titleLarge = Default.titleLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    titleMedium = Default.titleMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
    titleSmall = Default.titleSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),

    bodyLarge = Default.bodyLarge.copy(fontFamily = Manrope),
    bodyMedium = Default.bodyMedium.copy(fontFamily = Manrope),
    bodySmall = Default.bodySmall.copy(fontFamily = Manrope),

    labelLarge = Default.labelLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
    labelMedium = Default.labelMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Medium),
    labelSmall = Default.labelSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Medium),
)
