package com.biocap.app.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biocap.app.data.link.PeerRole
import com.biocap.app.presentation.components.AppCard
import com.biocap.app.presentation.components.BioCapTopBar
import com.biocap.app.ui.theme.CardBorder
import com.biocap.app.ui.theme.EyebrowGold
import com.biocap.app.ui.theme.GoldInk
import com.biocap.app.ui.theme.GoldSoft
import com.biocap.app.ui.theme.Navy

/**
 * Per-device settings. The device prefix (A/B/C/D) tags every participant code (`A-001`) and session
 * code (`BC-A-…`) generated on this tablet, and also scopes the device-to-device link to one pair.
 * Rule: **both devices of a pair (server + client) use the same letter**; different pairs use
 * different letters, so codes don't collide and each client only links to its own server. Operators
 * must agree beforehand which letter each pair owns.
 *
 * Also hosts the device-link mode (Server/Client) — the same choice as the first-launch picker;
 * Home re-reads it on resume, so switching here re-shapes Home immediately on return.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val devicePrefix by viewModel.devicePrefix.collectAsState()
    val deviceMode by viewModel.deviceMode.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BioCapTopBar(
                title = "Settings",
                onNavigateBack = onNavigateBack
            )

            // ── Device prefix ──
            AppCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "DEVICE PREFIX",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EyebrowGold
                    )
                    Text(
                        text = "Added to every participant and session code on this device, and pairs " +
                            "it with its server/client. Both devices in a set use the same letter; a " +
                            "different set uses a different letter, so codes never collide and each " +
                            "client links only to its own server.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Circular letter chips: selected is navy with the gold letter.
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        viewModel.devicePrefixes.forEach { prefix ->
                            LetterChip(
                                letter = prefix,
                                selected = prefix == devicePrefix,
                                onClick = { viewModel.onPrefixSelected(prefix) }
                            )
                        }
                    }

                    Text(
                        text = "Codes on this device: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$devicePrefix-001 · BC-$devicePrefix-…",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = GoldInk
                    )
                }
            }

            // ── Device mode ──
            AppCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "DEVICE MODE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EyebrowGold
                    )
                    Text(
                        text = "Server hosts the link and watches the paired device; Client runs the " +
                            "full operator app and connects to its Server. The home screen changes to " +
                            "match the selected mode.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    PillSwitch<PeerRole?>(
                        options = listOf<Pair<PeerRole?, String>>(
                            PeerRole.SERVER to "Server",
                            PeerRole.CLIENT to "Client"
                        ),
                        selected = deviceMode,
                        onSelect = { it?.let(viewModel::onModeSelected) }
                    )
                }
            }
        }
    }
}

/** A circular prefix letter chip: navy with a gold letter when selected, outlined otherwise. */
@Composable
private fun LetterChip(
    letter: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (selected) Navy else Color.Transparent)
            .border(1.5.dp, if (selected) Navy else CardBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) com.biocap.app.ui.theme.Gold else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A two-or-more state pill switch (gold track, navy selected segment) — one control language with
 *  the participant-entry gender selector. */
@Composable
private fun <T> PillSwitch(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = GoldSoft
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            options.forEach { (value, label) ->
                val isSel = value == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSel) Navy else Color.Transparent)
                        .clickable { onSelect(value) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else GoldInk
                    )
                }
            }
        }
    }
}
