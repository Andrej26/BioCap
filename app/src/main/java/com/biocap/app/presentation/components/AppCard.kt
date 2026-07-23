package com.biocap.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biocap.app.ui.theme.CardBorder
import com.biocap.app.ui.theme.GoldInk
import com.biocap.app.ui.theme.GoldSoft

/**
 * The BioCap base card: white surface, warm 1 dp border, 16 dp corners — the card language
 * established on Home and used across every screen. This is the plain container; for the common
 * "icon badge + title + subtitle + trailing" row use [AppListCard].
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = BorderStroke(1.dp, CardBorder),
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = color,
            contentColor = contentColor,
            border = border,
            modifier = modifier.fillMaxWidth()
        ) { content() }
    } else {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = color,
            contentColor = contentColor,
            border = border,
            modifier = modifier.fillMaxWidth()
        ) { content() }
    }
}

/**
 * A circular gold icon badge — the recurring 40 dp chip that fronts list rows and headers.
 */
@Composable
fun AppIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Int = 40,
    badgeColor: Color = GoldSoft,
    iconColor: Color = GoldInk
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(badgeColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size((size * 0.5f).dp)
        )
    }
}

/**
 * The standard list card: gold icon badge, bold title + optional subtitle, and a trailing slot
 * (a forward arrow by default, or any [trailing] content — a status pill, a chip, a chevron).
 * Matches the Sensors/Sessions row anatomy from the design system.
 */
@Composable
fun AppListCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    showArrow: Boolean = onClick != null,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    AppCard(onClick = onClick, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                AppIconBadge(icon = icon)
                Spacer(Modifier.width(13.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(10.dp))
                trailing()
            }
            if (showArrow) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
