package com.biocap.app.presentation.screens.sessions.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Battery1Bar
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biocap.app.data.model.ConnectionState
import com.biocap.app.presentation.components.ConnectionStatusBadge
import com.biocap.app.ui.theme.CardBorder
import com.biocap.app.ui.theme.CriticalRed
import com.biocap.app.ui.theme.StatusGreen
import com.biocap.app.ui.theme.WarningAmber

@Composable
fun DeviceSensorGroup(
    deviceName: String,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
    batteryLevel: Int? = null,
    statusLabel: String? = null,
    onClick: (() -> Unit)? = null,
    clickHint: String? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    // Tappable while not connected (mirrors the eSense Pulse card): a tap drives the
    // connect/enable-Bluetooth action supplied by the caller. A live or buffering watch maps to
    // CONNECTED, so the tap target lines up with "not actually connected".
    val isClickable = onClick != null &&
            (connectionState == ConnectionState.DISCONNECTED || connectionState == ConnectionState.ERROR)
    // Slim left rail encodes the connection state (green/amber/red/gray) on the white AppCard.
    val railColor by animateColorAsState(
        targetValue = when (connectionState) {
            ConnectionState.CONNECTED -> StatusGreen
            ConnectionState.CONNECTING -> WarningAmber
            ConnectionState.RECONNECTING -> WarningAmber
            ConnectionState.ERROR -> CriticalRed
            ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(300),
        label = "device_group_rail"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .then(
                if (isClickable) Modifier.clickable { onClick?.invoke() }
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Colored state rail.
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(railColor)
            )
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (connectionState == ConnectionState.CONNECTED) {
                    batteryLevel?.let { level ->
                        Icon(
                            imageVector = batteryIcon(level),
                            contentDescription = "Battery $level%",
                            modifier = Modifier.size(14.dp),
                            tint = if (level < 20) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "$level%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (level < 20) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
                ConnectionStatusBadge(state = connectionState, label = statusLabel)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
            footer?.invoke(this)

            if (isClickable && clickHint != null) {
                Text(
                    text = clickHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
            }
        }
    }
}

private fun batteryIcon(level: Int): ImageVector = when {
    level >= 95 -> Icons.Default.BatteryFull
    level >= 80 -> Icons.Default.Battery6Bar
    level >= 65 -> Icons.Default.Battery5Bar
    level >= 50 -> Icons.Default.Battery4Bar
    level >= 35 -> Icons.Default.Battery3Bar
    level >= 20 -> Icons.Default.Battery2Bar
    level >= 10 -> Icons.Default.Battery1Bar
    else        -> Icons.Default.Battery0Bar
}
