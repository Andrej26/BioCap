package com.biocap.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biocap.app.ui.theme.CardBorder
import com.biocap.app.ui.theme.GoldInk
import com.biocap.app.ui.theme.GoldSoft
import com.biocap.app.ui.theme.Ivory

/**
 * A compact status pill: a small colored dot (or spinner) + label on a white rounded chip, so the
 * same status reads identically on any card. Used for connection state, sensor state, etc.
 */
@Composable
fun StatusPill(
    label: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
    spinning: Boolean = false,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = Ivory,
    borderColor: Color = CardBorder
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (spinning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(9.dp),
                    strokeWidth = 2.dp,
                    color = dotColor
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = labelColor
            )
        }
    }
}

/**
 * A gold-tinted chip for a status label or a top-bar action (e.g. "Export", "● REC").
 */
@Composable
fun GoldChip(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = GoldSoft,
    contentColor: Color = GoldInk
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
