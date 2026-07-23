package com.biocap.app.presentation.screens.sensors.watch

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biocap.app.data.model.ConnectionState
import com.biocap.app.data.sensor.watch.WatchLinkStatus
import com.biocap.app.presentation.components.AppCard
import com.biocap.app.presentation.components.AppIconBadge
import com.biocap.app.presentation.components.BioCapTopBar
import com.biocap.app.presentation.components.BluetoothDisabledCard
import com.biocap.app.presentation.components.StatusPill
import com.biocap.app.ui.theme.EyebrowGold
import com.biocap.app.ui.theme.NeutralGray
import com.biocap.app.ui.theme.StatusGreen
import com.biocap.app.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchSensorScreen(
    onNavigateBack: () -> Unit,
    viewModel: WatchSensorViewModel = hiltViewModel()
) {
    val connection by viewModel.connectionState.collectAsState()
    val linkStatus by viewModel.linkStatus.collectAsState()
    val readings by viewModel.latestByType.collectAsState()
    val trackers by viewModel.availableTrackers.collectAsState()
    val battery by viewModel.batteryLevel.collectAsState()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val context = LocalContext.current

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* adapter state is observed reactively via viewModel.bluetoothEnabled */ }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            BioCapTopBar(
                title = "Galaxy Watch",
                subtitle = "HR · IBI · EDA via Wearable link",
                onNavigateBack = onNavigateBack
            )

            // Bluetooth Disabled Warning — the watch link runs over direct Bluetooth, so without it
            // data can't arrive reliably (the cloud relay dies when the phone sleeps). Same card and
            // behaviour as the eSense Pulse screen.
            if (!bluetoothEnabled) {
                BluetoothDisabledCard(
                    onClick = {
                        enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    }
                )
            }

            // Connection status: gold watch badge + channel line, status pill on the right.
            // Prefer the finer link status so an expected screen-off/Doze gap reads as "buffering",
            // not a scary "Disconnected". Fall back to the coarse state for CONNECTING/ERROR.
            val (statusLabel, statusDot, statusSpin) = when (connection) {
                ConnectionState.CONNECTING -> Triple("Connecting…", WarningAmber, true)
                ConnectionState.ERROR -> Triple("Error", com.biocap.app.ui.theme.CriticalRed, false)
                else -> when (linkStatus) {
                    WatchLinkStatus.LIVE -> Triple("Connected", StatusGreen, false)
                    WatchLinkStatus.DOZING -> Triple("Dozing — buffering", WarningAmber, false)
                    WatchLinkStatus.DISCONNECTED -> Triple("Disconnected", NeutralGray, false)
                }
            }
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconBadge(icon = Icons.Default.Watch)
                    Spacer(Modifier.padding(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Channel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        battery?.let {
                            Text("Watch battery $it%", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    StatusPill(label = statusLabel, dotColor = statusDot, spinning = statusSpin)
                }
            }

            // Live readings per type as a stat-tile grid.
            Text(
                "LIVE READINGS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = EyebrowGold
            )
            if (readings.isEmpty()) {
                Text("Waiting for data… (start tracking on the watch)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val entries = readings.toSortedMap().entries.toList()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    entries.forEach { (type, r) ->
                        WatchStatTile(
                            label = watchSignalLabel(type),
                            value = watchValueText(type, r.value),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Available trackers (what this watch can give us)
            AppCard {
                Column(Modifier.padding(horizontal = 15.dp, vertical = 13.dp)) {
                    Text("SUPPORTED TRACKERS", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = EyebrowGold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (trackers.isEmpty()) "—" else trackers.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** A compact live-reading stat tile: eyebrow label + big tabular value. */
@Composable
private fun WatchStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = EyebrowGold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Friendly display name for a watch signal type (the wire types are terse). */
private fun watchSignalLabel(type: String): String = when (type) {
    "WATCH_HR" -> "Heart rate"
    "WATCH_IBI" -> "Inter-beat interval"
    "WATCH_EDA" -> "Skin conductance (EDA)"
    "BATTERY" -> "Watch battery"
    else -> type
}

/** Value text with the signal's unit so a bare number isn't ambiguous. */
private fun watchValueText(type: String, value: Float): String = when (type) {
    "WATCH_HR" -> "${value.toInt()} bpm"
    "WATCH_IBI" -> "${value.toInt()} ms"
    "WATCH_EDA" -> "%.2f µS".format(value)
    "BATTERY" -> "${value.toInt()} %"
    else -> value.toString()
}
