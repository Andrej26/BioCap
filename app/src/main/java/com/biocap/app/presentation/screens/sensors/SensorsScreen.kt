package com.biocap.app.presentation.screens.sensors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biocap.app.data.model.ConnectionState
import com.biocap.app.presentation.components.BioCapTopBar
import com.biocap.app.presentation.components.SensorTypeCard
import com.biocap.app.ui.theme.EyebrowGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSensorDetail: (String) -> Unit,
    viewModel: SensorsViewModel = hiltViewModel()
) {
    val bleConnectionState by viewModel.bleConnectionState.collectAsState()
    val respirationState by viewModel.respirationState.collectAsState()
    val watchConnectionState by viewModel.watchConnectionState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val connectedCount = listOf(bleConnectionState, respirationState.toConnectionState(), watchConnectionState)
            .count { it == ConnectionState.CONNECTED }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val hPad = if (maxWidth >= 600.dp) 24.dp else 16.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = hPad, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                BioCapTopBar(
                    title = "Sensors",
                    subtitle = "$connectedCount of 3 connected",
                    onNavigateBack = onNavigateBack
                )

                Text(
                    text = "AVAILABLE SENSORS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = EyebrowGold
                )

                SensorTypeCard(
                    name = "eSense Pulse",
                    description = "Heart Rate Monitor (BLE)",
                    icon = Icons.Default.Bluetooth,
                    connectionState = bleConnectionState,
                    onClick = { onNavigateToSensorDetail("esense_pulse") }
                )

                SensorTypeCard(
                    name = "eSense Respiration",
                    description = "Breathing Sensor (Audio Jack)",
                    icon = Icons.Default.Mic,
                    connectionState = respirationState.toConnectionState(),
                    onClick = { onNavigateToSensorDetail("esense_respiration") }
                )

                SensorTypeCard(
                    name = "Galaxy Watch",
                    description = "HR / IBI / EDA (Wear OS)",
                    icon = Icons.Default.Watch,
                    connectionState = watchConnectionState,
                    onClick = { onNavigateToSensorDetail("galaxy_watch") }
                )
            }
        }
    }
}
