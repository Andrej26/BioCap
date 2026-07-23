package com.biocap.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.biocap.app.data.model.ConnectionState

/**
 * Sensor list row: the shared [AppListCard] with a gold icon badge, the sensor name + description,
 * and the connection status pill tucked to the right.
 */
@Composable
fun SensorTypeCard(
    name: String,
    description: String,
    icon: ImageVector,
    connectionState: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppListCard(
        title = name,
        subtitle = description,
        icon = icon,
        onClick = onClick,
        modifier = modifier,
        trailing = {
            ConnectionStatusBadge(state = connectionState, label = null)
        }
    )
}
