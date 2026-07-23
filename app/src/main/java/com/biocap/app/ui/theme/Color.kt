package com.biocap.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── BioCap brand palette ──
// Derived from the logo: mustard gold arch, near-black navy ECG line, warm-ivory ground.
// Navy carries weight in type and the primary action; gold is the accent; ivory is the surface.

val Navy = Color(0xFF131B2E)          // logo ECG line / primary action / body ink
val NavyDeep = Color(0xFF0F1626)      // slightly deeper navy for pressed/dark surfaces
val Gold = Color(0xFFD2AE3B)          // logo arch / accent
val GoldDeep = Color(0xFFB8952B)      // gold that stays legible on light grounds (icons, links)
val GoldSoft = Color(0xFFF1E8CC)      // gold tint for icon badges on light cards

// Ivory (Parchment) ground and its companions for the light scheme.
val Ivory = Color(0xFFFAF7EE)         // page background (top of the gradient)
val IvoryDeep = Color(0xFFEFE8D5)     // page background (bottom of the gradient)
val CardWhite = Color(0xFFFFFFFF)     // secondary cards / status footer
val CardBorder = Color(0xFFE6DFC9)    // warm border on white cards
val InkMuted = Color(0xFF6F7488)      // subtitle / secondary text
val InkSubtle = Color(0xFF3D4459)     // status / nav labels
val EyebrowGold = Color(0xFFA09367)   // muted gold for uppercase eyebrow labels
val GoldInk = Color(0xFF8A6F1C)       // legible gold-dark for text/icons on gold tints
val OnNavyMutedPublic = Color(0xFFB7BDCF) // muted subtitle tone on the navy primary card

// ── Semantic alert palette ──
// Deliberately shifted away from the brand gold so warnings never blend into the accent:
// warning amber is more orange than the gold; critical is a muted red (not stock Material red).
val StatusGreen = Color(0xFF4CAF50)     // success / connected
val StatusGreenInk = Color(0xFF2E7D32)  // legible green for text on light chips
val WarningAmber = Color(0xFFE8922E)    // warnings, connecting states
val WarningAmberInk = Color(0xFF9A5D14) // legible amber for text on amber tints
val CriticalRed = Color(0xFFC6473E)     // critical alerts, errors
val CriticalRedInk = Color(0xFFA33830)  // legible red for text on red tints
val NeutralGray = Color(0xFF9AA0AE)     // disconnected / inactive dots
val ActiveOrange = Color(0xFFCC8A52)    // in-progress session highlight

// Warm warning-container tones (readiness card: amber-tinted card with white fix rows).
val WarnContainer = Color(0xFFF7ECD2)
val WarnBorder = Color(0xFFE5CF94)
val WarnInk = Color(0xFF4A3F18)

// Soft error tint (tappable error cards like Bluetooth-disabled).
val ErrorTint = Color(0xFFF6DFD2)
val ErrorTintBorder = Color(0xFFE7BFA4)
val ErrorTintInk = Color(0xFF6E3A1C)

// Console (navy diagnostics surface — debug log, connection log).
val ConsoleNavy = Color(0xFF131B2E)
val ConsoleText = Color(0xFFC6CCDB)
val ConsoleMuted = Color(0xFF8D96AD)
val ConsoleGreen = Color(0xFF7EE0A3)

// Tutorial section colors — brand-harmonized wayfinding across the 12 onboarding steps.
val SectionHeartRate = Color(0xFFC0653F)   // terracotta — heart rate
val SectionHeartRateInk = Color(0xFFB05A37)
val SectionBreathing = Color(0xFF4E8577)   // sage — respiration
val SectionBreathingInk = Color(0xFF44755F)
val SectionWatch = Color(0xFF4A6489)       // slate — Galaxy Watch
val SectionWatchInk = Color(0xFF435A7D)
