package com.biocap.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biocap.app.data.sensor.DeviceState
import com.biocap.app.ui.theme.GoldInk
import com.biocap.app.ui.theme.GoldSoft
import com.biocap.app.ui.theme.Navy
import com.biocap.app.ui.theme.StatusGreen
import com.biocap.app.ui.theme.StatusGreenInk

@Composable
fun BioSensorCard(
    sensorName: String,
    state: DeviceState,
    rate: Float,
    stats: String,
    /** Unit of [rate] — always passed explicitly; there is no sensible cross-sensor default. */
    unit: String,
    onToggle: () -> Unit,
    showStreamData: Boolean = false,
    onToggleStreamDisplay: () -> Unit = {}
) {
    val isStreaming = state == DeviceState.Streaming
    AppCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sensorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Soft state chip
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isStreaming) StatusGreen.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = state.name.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isStreaming) StatusGreenInk
                            else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val displayRate = if (showStreamData) rate else 0f
            val displayStats = if (showStreamData) stats else "Data Hidden"

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format("%.1f", displayRate),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (showStreamData) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = " $unit",
                    modifier = Modifier.padding(bottom = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = displayStats,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onToggle,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Text(if (state == DeviceState.Disconnected) "CONNECT" else "DISCONNECT")
                }

                if (isStreaming) {
                    Button(
                        onClick = onToggleStreamDisplay,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldSoft,
                            contentColor = GoldInk
                        )
                    ) {
                        Text(if (showStreamData) "HIDE DATA" else "SHOW DATA")
                    }
                }
            }
        }
    }
}
