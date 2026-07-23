package com.biocap.app.presentation.screens.sessions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biocap.app.data.db.SessionEntity
import com.biocap.app.data.db.SessionStatus
import com.biocap.app.presentation.components.AppCard
import com.biocap.app.ui.theme.GoldInk
import com.biocap.app.ui.theme.GoldSoft
import com.biocap.app.ui.theme.Navy
import com.biocap.app.ui.theme.StatusGreen
import com.biocap.app.ui.theme.StatusGreenInk
import com.biocap.app.util.TimeFormats
import com.biocap.app.util.formatDuration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun SessionCard(
    session: SessionEntity,
    onClick: () -> Unit,
    isUploading: Boolean = false,
    onUpload: (() -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        .apply { timeZone = TimeFormats.UTC }

    AppCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = session.sessionCode,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    SessionStatusChip(status = session.status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Meta line: date · duration · sample counts, compact and tabular.
                val meta = buildString {
                    append(dateFormat.format(Date(session.startedAt)))
                    val durationMs = session.endedAt?.let { it - session.startedAt } ?: 0L
                    if (durationMs > 0) append(" · ${formatDuration(durationMs)}")
                    if (session.hrSampleCount > 0) append(" · HR ${session.hrSampleCount}")
                    if (session.respirationSampleCount > 0) append(" · Resp ${session.respirationSampleCount}")
                }
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Manual "Upload to server" for sessions not yet on the server (pending state):
            // a gold circle button matching the design system.
            if (session.status == SessionStatus.COMPLETED && onUpload != null) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = GoldInk
                    )
                } else {
                    IconButton(onClick = onUpload) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(GoldSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload to server",
                                tint = GoldInk,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Session status chip: gold tint for ACTIVE/COMPLETED (pending states), green for UPLOADED.
 */
@Composable
internal fun SessionStatusChip(status: SessionStatus) {
    val (bg, ink, text) = when (status) {
        SessionStatus.ACTIVE -> Triple(GoldSoft, GoldInk, "ACTIVE")
        SessionStatus.COMPLETED -> Triple(GoldSoft, GoldInk, "COMPLETED")
        SessionStatus.UPLOADED -> Triple(StatusGreen.copy(alpha = 0.16f), StatusGreenInk, "UPLOADED")
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = ink,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}
