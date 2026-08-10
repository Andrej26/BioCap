package com.biocap.app.presentation.screens.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.biocap.app.data.db.ScenarioCode
import com.biocap.app.presentation.components.AppCard
import com.biocap.app.presentation.components.BioCapTopBar
import com.biocap.app.presentation.screens.sessions.components.EndSessionWatchDialog
import com.biocap.app.ui.theme.GoldInk
import com.biocap.app.ui.theme.GoldSoft
import com.biocap.app.ui.theme.Navy
import com.biocap.app.ui.theme.StatusGreen
import com.biocap.app.ui.theme.StatusGreenInk

/**
 * Scenario picker that doubles as the session's home/hub: one vertically-centered button per
 * [ScenarioCode] (labelled with its `displayName`, e.g. "Scenario A – Baseline Calibration") opens the
 * session control screen for [sessionId] with the chosen scenario number (1-based, in declaration
 * order), and an **End Session & Save** action at the bottom finalizes the whole session
 * (with the watch-transfer handshake) and leaves for review. This is the screen operators return to
 * most often, so end/save lives here rather than inside each scenario run.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioSelectionScreen(
    sessionId: Long,
    onScenarioSelected: (scenarioNumber: Int) -> Unit,
    onSessionEnded: (sessionId: Long) -> Unit,
    viewModel: SessionControlViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsState()
    val scenarios by viewModel.scenarios.collectAsState()
    val isEndingSession by viewModel.isEndingSession.collectAsState()
    val endSessionPhase by viewModel.endSessionPhase.collectAsState()
    val watchReconciliation by viewModel.watchReconciliation.collectAsState()

    // A scenario counts as "recorded" once it has an endedAt (a finished run in this session).
    val recordedCodes = scenarios.filter { it.endedAt != null }.map { it.scenarioCode }.toSet()

    var showEndSessionConfirmation by remember { mutableStateOf(false) }

    // End-Session watch handshake (wake → transfer → green check → finalize), shared with the
    // session control screen via the same ViewModel.
    EndSessionWatchDialog(
        phase = endSessionPhase,
        onEndWithoutWatchData = { viewModel.endWithoutWatchData() },
        onRetry = { viewModel.retryWatchTransfer() },
        onComplete = { sid -> onSessionEnded(sid) },
        reconciliation = watchReconciliation
    )

    if (showEndSessionConfirmation) {
        AlertDialog(
            onDismissRequest = { showEndSessionConfirmation = false },
            title = { Text("End session?") },
            text = { Text("Are you sure you want to end this session?") },
            confirmButton = {
                TextButton(onClick = {
                    showEndSessionConfirmation = false
                    viewModel.requestEndSession()
                }) {
                    Text("End Session")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndSessionConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            BioCapTopBar(
                title = session?.sessionCode ?: "Select Scenario",
                subtitle = "Pick a scenario to record",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Scrollable scenario cards.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                ScenarioCode.entries.forEachIndexed { index, code ->
                    ScenarioCard(
                        code = code,
                        recorded = code in recordedCodes,
                        onClick = { onScenarioSelected(index + 1) }
                    )
                }
                Spacer(Modifier.size(4.dp))
            }

            // End Session & Save, pinned at the bottom as the navy action.
            Button(
                onClick = { showEndSessionConfirmation = true },
                enabled = !isEndingSession,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Navy,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 528.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp)
            ) {
                if (isEndingSession) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("End Session & Save", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * One scenario as a white card: a gold letter badge (A–E), the scenario title (with its "Scenario X –"
 * prefix stripped so the letter carries the code), and a RECORDED chip once it has a finished run.
 */
@Composable
private fun ScenarioCard(
    code: ScenarioCode,
    recorded: Boolean,
    onClick: () -> Unit
) {
    val shortTitle = code.displayName.substringAfter("– ", code.displayName).trim()
    AppCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Letter badge: the official code A–E in the gold badge.
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GoldSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = code.officialCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldInk
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shortTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Scenario ${code.officialCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (recorded) {
                Surface(
                    color = StatusGreen.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "RECORDED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusGreenInk,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
