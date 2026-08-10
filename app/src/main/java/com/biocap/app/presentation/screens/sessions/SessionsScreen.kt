package com.biocap.app.presentation.screens.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biocap.app.presentation.components.BioCapTopBar
import com.biocap.app.presentation.screens.sessions.components.ActiveSessionBanner
import com.biocap.app.presentation.screens.sessions.components.SessionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    onNavigateBack: () -> Unit,
    onOpenSession: (sessionId: Long) -> Unit,
    onOpenActiveSession: (sessionId: Long) -> Unit,
    viewModel: SessionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uploadingIds by viewModel.uploadingIds.collectAsState()
    val uploadMessage by viewModel.uploadMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uploadMessage) {
        uploadMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUploadMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            BioCapTopBar(
                title = "Completed Sessions",
                subtitle = "${uiState.sessions.size} session${if (uiState.sessions.size != 1) "s" else ""} · all times UTC",
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            uiState.activeSession?.let { activeSession ->
                ActiveSessionBanner(
                    sessionCode = activeSession.sessionCode,
                    duration = uiState.activeSessionDuration,
                    heartRate = uiState.activeSessionHeartRate,
                    isRecording = uiState.isRecording,
                    onResume = { onOpenActiveSession(activeSession.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (uiState.sessions.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No completed sessions yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start a session from the home screen to see it here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.pendingUploadCount > 0) {
                        item {
                            Text(
                                text = "${uiState.pendingUploadCount} pending upload",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = com.biocap.app.ui.theme.WarningAmberInk
                            )
                        }
                    }

                    items(uiState.sessions, key = { it.id }) { session ->
                        SessionCard(
                            session = session,
                            onClick = { onOpenSession(session.id) },
                            isUploading = session.id in uploadingIds,
                            onUpload = { viewModel.uploadSession(session.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
