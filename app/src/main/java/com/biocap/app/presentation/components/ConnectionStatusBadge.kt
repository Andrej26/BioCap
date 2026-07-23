package com.biocap.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.biocap.app.data.model.ConnectionState
import com.biocap.app.ui.theme.CriticalRed
import com.biocap.app.ui.theme.NeutralGray
import com.biocap.app.ui.theme.StatusGreen
import com.biocap.app.ui.theme.WarningAmber

/** Dot/label colour for a connection state — gray (disconnected), amber (connecting/reconnecting),
 *  green (connected), red (error). Uses the shared semantic palette so sensors, the device link,
 *  etc. all match. */
fun connectionStatusColor(state: ConnectionState): Color = when (state) {
    ConnectionState.DISCONNECTED -> NeutralGray
    ConnectionState.CONNECTING -> WarningAmber
    ConnectionState.RECONNECTING -> WarningAmber
    ConnectionState.CONNECTED -> StatusGreen
    ConnectionState.ERROR -> CriticalRed
}

/**
 * Connection status rendered as the shared white [StatusPill] (a colored dot / spinner + label),
 * with an optional gold Retry in the error state.
 */
@Composable
fun ConnectionStatusBadge(
    state: ConnectionState,
    label: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = connectionStatusColor(state),
        animationSpec = tween(300),
        label = "statusColor"
    )

    val statusText = label ?: when (state) {
        ConnectionState.DISCONNECTED -> "Disconnected"
        ConnectionState.CONNECTING -> "Connecting"
        ConnectionState.RECONNECTING -> "Reconnecting"
        ConnectionState.CONNECTED -> "Connected"
        ConnectionState.ERROR -> "Error"
    }

    val spinning = state == ConnectionState.CONNECTING || state == ConnectionState.RECONNECTING

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        StatusPill(
            label = statusText,
            dotColor = color,
            spinning = spinning
        )

        // Show retry button only in ERROR state when callback is provided
        if (state == ConnectionState.ERROR && onRetry != null) {
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = onRetry,
                modifier = Modifier.padding(0.dp)
            ) {
                Text(
                    text = "Retry",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
