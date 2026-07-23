package com.biocap.app.presentation.screens.mode

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biocap.app.R
import com.biocap.app.data.link.PeerRole
import com.biocap.app.presentation.screens.home.components.PrimaryActionButton
import com.biocap.app.ui.theme.OnNavyMutedPublic

/**
 * First-launch picker for the device's link role. Persists the choice (so later launches skip
 * straight to Home) and reports it so navigation can land on Home in the chosen mode. Later mode
 * changes are made in Settings.
 */
@Composable
fun ModeSelectionScreen(
    onModeSelected: () -> Unit,
    viewModel: ModeSelectionViewModel = hiltViewModel()
) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brand-first: the first screen a device ever shows opens with the logo.
                Image(
                    painter = painterResource(id = R.drawable.biocap_logo),
                    contentDescription = "BioCap",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 132.dp)
                )
                Text(
                    text = "Choose this device's mode",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Pick how this device participates in the device-to-device link. " +
                        "You can change it later in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Server is the navy (filled) card; Client is the white secondary card.
                PrimaryActionButton(
                    title = "Server",
                    subtitle = "Host the device link (other device connects)",
                    icon = Icons.Default.Wifi,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    subtitleColor = OnNavyMutedPublic,
                    iconBadgeColor = MaterialTheme.colorScheme.tertiary,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        viewModel.selectMode(PeerRole.SERVER)
                        onModeSelected()
                    }
                )

                PrimaryActionButton(
                    title = "Client",
                    subtitle = "Find and connect to a hosting device",
                    icon = Icons.Default.WifiFind,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    onClick = {
                        viewModel.selectMode(PeerRole.CLIENT)
                        onModeSelected()
                    }
                )
            }
        }
    }
}
