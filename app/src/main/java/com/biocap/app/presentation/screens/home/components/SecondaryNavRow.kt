package com.biocap.app.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * The utility row rendered as gold-accented tiles (per the Parchment mock): a bordered white tile
 * per destination, a gold icon, and a caption. Sensors/Tutorial belong to the operator (client)
 * home; in Server mode the row carries only Settings.
 */
@Composable
fun SecondaryNavRow(
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onSensors: (() -> Unit)? = null,
    onTutorial: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (onSensors != null) {
            NavTile(icon = Icons.Default.Sensors, label = "Sensors", onClick = onSensors, modifier = Modifier.weight(1f))
        }
        if (onTutorial != null) {
            NavTile(icon = Icons.Default.School, label = "Tutorial", onClick = onTutorial, modifier = Modifier.weight(1f))
        }
        NavTile(icon = Icons.Default.Settings, label = "Settings", onClick = onSettings, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NavTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
