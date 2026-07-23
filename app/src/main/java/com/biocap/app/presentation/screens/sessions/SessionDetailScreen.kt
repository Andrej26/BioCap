package com.biocap.app.presentation.screens.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biocap.app.data.db.SessionStatus
import com.biocap.app.presentation.components.AppCard
import com.biocap.app.presentation.components.BioCapTopBar
import com.biocap.app.presentation.screens.sessions.components.SessionStatusChip
import com.biocap.app.presentation.screens.sessions.components.UploadProgressDialog
import com.biocap.app.ui.theme.CriticalRed
import com.biocap.app.ui.theme.GoldInk
import com.biocap.app.ui.theme.GoldSoft
import com.biocap.app.ui.theme.Navy
import com.biocap.app.util.TimeFormats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    showCsvSaved: Boolean = false,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showReExportConfirmation by remember { mutableStateOf(false) }
    var showCsvDialog by remember { mutableStateOf(showCsvSaved) }

    LaunchedEffect(uiState.exportResult) {
        uiState.exportResult?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearExportResult()
        }
    }

    if (showCsvDialog && !uiState.isLoading) {
        val scenarioCount = uiState.scenarios.size
        AlertDialog(
            onDismissRequest = { showCsvDialog = false },
            title = { Text("Session saved") },
            text = {
                Column {
                    if (scenarioCount > 0) {
                        Text("Session completed with $scenarioCount scenario(s).")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Use Export to save data to Documents folder.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text("Session completed. No scenarios were recorded.")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCsvDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete session?") },
            text = { Text("This session will be permanently deleted. Make sure you've exported it to Documents first, or you'll lose all data.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    viewModel.deleteSession { onNavigateBack() }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showReExportConfirmation) {
        AlertDialog(
            onDismissRequest = { showReExportConfirmation = false },
            title = { Text("Re-export session?") },
            text = { Text("This will overwrite previously exported files in Documents/BioCap/.") },
            confirmButton = {
                TextButton(onClick = {
                    showReExportConfirmation = false
                    viewModel.exportSession()
                }) {
                    Text("Re-export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReExportConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    UploadProgressDialog(
        state = uiState.uploadState,
        onRetry = { viewModel.uploadSession() },
        onContinue = { viewModel.dismissUploadDialog() }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val session = uiState.session
        if (session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Session not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .apply { timeZone = TimeFormats.UTC }
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            .apply { timeZone = TimeFormats.UTC }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BioCapTopBar(
                title = session.sessionCode,
                subtitle = "${session.scenarioCount} scenario${if (session.scenarioCount != 1) "s" else ""} · all times UTC",
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                actions = { SessionStatusChip(status = session.status) }
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            AppCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SUMMARY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    SummaryRow("Participant", uiState.participantCode ?: "—")

                    SummaryRow("Date", dateFormat.format(Date(session.startedAt)))

                    val startTime = timeFormat.format(Date(session.startedAt))
                    val endTime = session.endedAt?.let { timeFormat.format(Date(it)) } ?: "N/A"
                    SummaryRow("Time", "$startTime - $endTime")

                    val durationMs = session.endedAt?.let { it - session.startedAt } ?: 0L
                    SummaryRow("Duration", formatDuration(durationMs))

                    SummaryRow("Scenarios", session.scenarioCount.toString())
                }
            }

            // Upload to the VitalWork server. Primary (navy) action; sets status UPLOADED on success.
            Button(
                onClick = { viewModel.uploadSession() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Navy,
                    contentColor = Color.White
                ),
                enabled = uiState.uploadState != UploadState.Uploading &&
                    uiState.scenarios.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (session.status == SessionStatus.UPLOADED) "Re-upload to server"
                    else "Upload to server",
                    fontWeight = FontWeight.Bold
                )
            }

            // Local offline copy to Documents (gold-ghost). Does NOT change upload status.
            Button(
                onClick = {
                    if (session.status == SessionStatus.UPLOADED) {
                        showReExportConfirmation = true
                    } else {
                        viewModel.exportSession()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldSoft,
                    contentColor = GoldInk
                ),
                enabled = !uiState.isExporting && uiState.scenarios.isNotEmpty()
            ) {
                if (uiState.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = GoldInk
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export to Documents", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CriticalRed
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CriticalRed.copy(alpha = 0.4f)),
                enabled = !uiState.isDeleting
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Session", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}
