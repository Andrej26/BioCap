package com.biocap.app.presentation.screens.sensors.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biocap.app.presentation.log.BleLogEntry
import com.biocap.app.ui.theme.ConsoleMuted
import com.biocap.app.ui.theme.ConsoleNavy
import com.biocap.app.ui.theme.ConsoleText
import com.biocap.app.ui.theme.CriticalRed
import com.biocap.app.ui.theme.Gold

/**
 * Composable that displays a scrollable debug log of BLE events.
 */
@Composable
fun BleDebugLog(
    logEntries: List<BleLogEntry>,
    onClearLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to top when new entries are added
    LaunchedEffect(logEntries.firstOrNull()) {
        if (logEntries.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Navy console: diagnostics visually separate from the operator UI.
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = ConsoleNavy
    ) {
        Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp)) {
            // Header: gold eyebrow + Clear action.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEBUG LOG (${logEntries.size}) · UTC",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )

                IconButton(
                    onClick = onClearLog,
                    enabled = logEntries.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear log",
                        tint = if (logEntries.isNotEmpty()) ConsoleMuted
                            else ConsoleMuted.copy(alpha = 0.38f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (logEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No log entries yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ConsoleMuted
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(
                        items = logEntries,
                        key = { index, entry -> "$index-${entry.timestamp}" }
                    ) { _, entry ->
                        LogEntryItem(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(
    entry: BleLogEntry,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (entry.isError) CriticalRed.copy(alpha = 0.14f)
                else ConsoleNavy.copy(alpha = 0f)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timestamp (muted).
        Text(
            text = entry.timestamp,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = ConsoleMuted
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Message: red for errors, green-tinted for success-ish, default console text otherwise.
        Text(
            text = entry.message,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = if (entry.isError) CriticalRed else ConsoleText,
            modifier = Modifier.weight(1f)
        )
    }
}
