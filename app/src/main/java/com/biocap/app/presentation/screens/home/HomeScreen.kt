package com.biocap.app.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.biocap.app.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import android.Manifest
import com.biocap.app.data.link.PeerRole
import com.biocap.app.data.model.ConnectionState
import com.biocap.app.data.system.SessionPrerequisite
import com.biocap.app.data.system.SystemReadinessChecker
import com.biocap.app.presentation.components.ReadinessWarningCard
import com.biocap.app.presentation.components.connectionStatusColor
import com.biocap.app.presentation.components.WatchBatteryWarningCard
import com.biocap.app.presentation.components.onPermissionDenied
import com.biocap.app.service.BatteryOptimizationHelper
import com.biocap.app.presentation.screens.home.components.PrimaryActionButton
import com.biocap.app.presentation.screens.home.components.SecondaryNavRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTutorial: () -> Unit,
    onNavigateToSensors: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSessions: () -> Unit,
    onNavigateToParticipantEntry: () -> Unit,
    onNavigateToSessionActive: (Long) -> Unit,
    onNavigateToLinkServer: () -> Unit,
    onNavigateToLinkClient: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val isStarting by viewModel.isStarting.collectAsState()
    val shouldAutoShowTutorial by viewModel.shouldAutoShowTutorial.collectAsState()
    val missingPrerequisites by viewModel.missingPrerequisites.collectAsState()
    val canStartSession by viewModel.canStartSession.collectAsState()
    val watchBatteryAlert by viewModel.watchBatteryAlert.collectAsState()
    val watchBatteryLevel by viewModel.watchBatteryLevel.collectAsState()
    val linkConnectionState by viewModel.linkConnectionState.collectAsState()
    val linkActiveRole by viewModel.linkActiveRole.collectAsState()
    val deviceMode by viewModel.deviceMode.collectAsState()
    val connectedSensorCount by viewModel.connectedSensorCount.collectAsState()
    val devicePrefix by viewModel.devicePrefix.collectAsState()

    val context = LocalContext.current

    // Re-derive readiness whenever the screen resumes — catches a silently revoked permission or a
    // battery-optimization setting that an update/OEM flipped, and clears the card after a Fix.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) onPermissionDenied(context, Manifest.permission.POST_NOTIFICATIONS)
        viewModel.refresh()
    }
    val microphoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) onPermissionDenied(context, Manifest.permission.RECORD_AUDIO)
        viewModel.refresh()
    }
    val bluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { !it }) {
            // Surface settings if any BLE permission is permanently denied (use the first as probe).
            onPermissionDenied(context, SystemReadinessChecker.requiredBluetoothPermissions().first())
        }
        viewModel.refresh()
    }

    val onFix: (SessionPrerequisite) -> Unit = { prerequisite ->
        when (prerequisite) {
            SessionPrerequisite.NOTIFICATIONS ->
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            SessionPrerequisite.MICROPHONE ->
                microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO)
            SessionPrerequisite.BLUETOOTH ->
                bluetoothLauncher.launch(SystemReadinessChecker.requiredBluetoothPermissions())
            SessionPrerequisite.BATTERY_OPTIMIZATION ->
                BatteryOptimizationHelper.openExemptionSettings(context)
        }
    }

    LaunchedEffect(shouldAutoShowTutorial) {
        if (shouldAutoShowTutorial) {
            viewModel.onTutorialAutoShown()
            onNavigateToTutorial()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val currentActive = activeSession
        val isServer = deviceMode == PeerRole.SERVER

        // While a session is active, tick once a second so the button shows live elapsed time.
        var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
        LaunchedEffect(currentActive?.id) {
            if (currentActive != null) {
                while (true) {
                    nowMs = System.currentTimeMillis()
                    delay(1000L)
                }
            }
        }
        val elapsedLabel = currentActive?.let { formatElapsed(nowMs - it.startedAt) }

        // A link runs in one role at a time: show the live status dot on that role's button,
        // gray on the other. Same gray/green indicator the sensors use.
        val serverDotColor = connectionStatusColor(
            if (linkActiveRole == PeerRole.SERVER) linkConnectionState else ConnectionState.DISCONNECTED
        )
        val clientDotColor = connectionStatusColor(
            if (linkActiveRole == PeerRole.CLIENT) linkConnectionState else ConnectionState.DISCONNECTED
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Scrollable content zone (warnings + action buttons + nav) ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                // ── Centered logo header (replaces the old TopAppBar). The logo carries the
                //    identity; a small uppercase caption states the mode + readiness. ──
                LogoHeader(
                    caption = if (isServer) "Monitoring Station" else "Operator Console",
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                )

                ReadinessWarningCard(
                    missing = missingPrerequisites,
                    onFix = onFix
                )

                WatchBatteryWarningCard(
                    alert = watchBatteryAlert,
                    level = watchBatteryLevel
                )

                // Server mode is intentionally bare: the host device only ever needs to start
                // hosting, so we show just "Connect as Server" (+ Settings, which hosts the
                // device-mode switch).
                if (deviceMode == PeerRole.SERVER) {
                    // Server's single action uses the navy primary card so it reads as the focus.
                    PrimaryActionButton(
                        title = "Connect as Server",
                        subtitle = "Host the device link",
                        onClick = onNavigateToLinkServer,
                        icon = Icons.Default.Wifi,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        subtitleColor = OnNavyMuted,
                        iconBadgeColor = MaterialTheme.colorScheme.tertiary,
                        iconColor = MaterialTheme.colorScheme.primary,
                        trailingDotColor = serverDotColor
                    )
                } else {
                    // Client mode (and the unpicked state) shows the full operator home, minus the
                    // "Connect as Server" button. The primary action is the navy card; an active
                    // session flips it to the orange "Resume" state.
                    val sessionActive = currentActive != null
                    PrimaryActionButton(
                        title = if (sessionActive) "Resume Active Session" else "Start New Session",
                        subtitle = when {
                            sessionActive -> elapsedLabel ?: "New participant · scenarios A–E"
                            !canStartSession -> "Fix the warnings above to start"
                            else -> "New participant · scenarios A–E"
                        },
                        enabled = !isStarting && canStartSession,
                        containerColor = if (sessionActive) ActiveSessionOrange
                            else MaterialTheme.colorScheme.primary,
                        contentColor = if (sessionActive) Color.White
                            else MaterialTheme.colorScheme.onPrimary,
                        subtitleColor = if (sessionActive) Color.White.copy(alpha = 0.85f) else OnNavyMuted,
                        iconBadgeColor = MaterialTheme.colorScheme.tertiary,
                        iconColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            if (currentActive != null) {
                                onNavigateToSessionActive(currentActive.id)
                            } else {
                                viewModel.beginSession(
                                    onResumeActive = onNavigateToSessionActive,
                                    onStartNewParticipantFlow = onNavigateToParticipantEntry
                                )
                            }
                        }
                    )

                    if (deviceMode == PeerRole.CLIENT) {
                        // Secondary cards: white on the ivory ground, gold-tinted icon badge.
                        PrimaryActionButton(
                            title = "Connect as Client",
                            subtitle = "Find and connect to a hosting device",
                            onClick = onNavigateToLinkClient,
                            icon = Icons.Default.WifiFind,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            trailingDotColor = clientDotColor
                        )
                    }

                    PrimaryActionButton(
                        title = "Completed Sessions",
                        subtitle = "Browse and export past sessions",
                        onClick = onNavigateToSessions,
                        icon = Icons.Default.Folder,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Sensors/Tutorial are operator-only; Settings is shown in both modes — it carries
                // the device prefix (A–D) that scopes the link to one pair, and the device-mode
                // (Server/Client) switch.
                if (deviceMode == PeerRole.SERVER) {
                    SecondaryNavRow(
                        onSettings = onNavigateToSettings
                    )
                } else {
                    SecondaryNavRow(
                        onSettings = onNavigateToSettings,
                        onSensors = onNavigateToSensors,
                        onTutorial = onNavigateToTutorial
                    )
                }
            }
            }

            // ── Footer: status card anchored to the bottom edge ──
            // Status at a glance: readiness / link state on the left, device mode + prefix on the
            // right (the prefix matters when several prefixed tablets test in parallel).
            val statusText: String
            val statusDot: Color
            when {
                isServer -> {
                    val serverState = if (linkActiveRole == PeerRole.SERVER) linkConnectionState
                        else ConnectionState.DISCONNECTED
                    when (serverState) {
                        ConnectionState.CONNECTED -> {
                            statusText = "Monitored device connected"
                            statusDot = StatusGreen
                        }
                        ConnectionState.CONNECTING, ConnectionState.RECONNECTING -> {
                            statusText = "Connecting…"
                            statusDot = StatusAmber
                        }
                        else -> {
                            statusText = "Waiting for monitored device"
                            statusDot = serverDotColor
                        }
                    }
                }
                currentActive != null -> {
                    statusText = "Session in progress — ${currentActive.sessionCode}"
                    statusDot = ActiveSessionOrange
                }
                missingPrerequisites.isNotEmpty() -> {
                    statusText = "Setup needed — see warnings above"
                    statusDot = StatusAmber
                }
                connectedSensorCount == 0 -> {
                    statusText = "No sensors connected"
                    statusDot = MaterialTheme.colorScheme.outline
                }
                else -> {
                    statusText = if (connectedSensorCount == 1) "1 sensor connected"
                        else "$connectedSensorCount sensors connected"
                    statusDot = StatusGreen
                }
            }
            HomeStatusCard(
                text = statusText,
                dotColor = statusDot,
                modeLabel = "${if (isServer) "Server" else "Client"} · $devicePrefix",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

/**
 * Bottom status card (the footer): a status dot + text on the left, and the device mode + prefix
 * chip on the right. Kept deliberately plain — a bordered rounded card with standard typography.
 */
@Composable
private fun HomeStatusCard(
    text: String,
    dotColor: Color,
    modeLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = modeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * The centered brand header: the BioCap logo above a small uppercase caption. Replaces the old
 * "BioCap Operator" TopAppBar — the logo now carries the identity, per the Parchment design.
 */
@Composable
private fun LogoHeader(
    caption: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.biocap_logo),
            contentDescription = "BioCap",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 148.dp)
        )
        Text(
            text = caption.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/** Muted text tone for subtitles on the navy primary card. */
private val OnNavyMuted = Color(0xFFB7BDCF)

/** In-progress session accent + status-dot colors come from the shared palette (Color.kt):
 *  [com.biocap.app.ui.theme.ActiveOrange], [com.biocap.app.ui.theme.StatusGreen],
 *  [com.biocap.app.ui.theme.WarningAmber]. Aliased locally to keep the call sites terse. */
private val ActiveSessionOrange = com.biocap.app.ui.theme.ActiveOrange
private val StatusGreen = com.biocap.app.ui.theme.StatusGreen
private val StatusAmber = com.biocap.app.ui.theme.WarningAmber

/** Formats an elapsed duration as H:MM:SS (or M:SS under an hour). */
private fun formatElapsed(elapsedMs: Long): String {
    val totalSeconds = (elapsedMs.coerceAtLeast(0L)) / 1000L
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
