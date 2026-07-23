package com.biocap.app.presentation.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.biocap.app.data.system.SessionPrerequisite
import com.biocap.app.ui.theme.CardBorder
import com.biocap.app.ui.theme.CardWhite
import com.biocap.app.ui.theme.GoldInk
import com.biocap.app.ui.theme.WarnBorder
import com.biocap.app.ui.theme.WarnContainer
import com.biocap.app.ui.theme.WarnInk

/**
 * Conditional warning card listing session prerequisites that are currently missing, each with a
 * one-tap **Fix**. Renders nothing when [missing] is empty, so it only appears when there's a
 * real problem (a silent revocation, or a never-granted permission). Hosted at the top of Home
 * and as a backup banner on the active-session screen.
 */
@Composable
fun ReadinessWarningCard(
    missing: Set<SessionPrerequisite>,
    onFix: (SessionPrerequisite) -> Unit,
    modifier: Modifier = Modifier
) {
    if (missing.isEmpty()) return

    // Stable display order regardless of set iteration order.
    val ordered = SessionPrerequisite.entries.filter { it in missing }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = WarnContainer,
        border = BorderStroke(1.dp, WarnBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = WarnInk,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = "Setup needed before sessions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarnInk
                )
            }

            ordered.forEach { prerequisite ->
                // Whole row is one clickable button; rows are spaced (10.dp above) so they read
                // as distinct tappable items rather than crowded markers.
                Surface(
                    onClick = { onFix(prerequisite) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardWhite,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 13.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = labelFor(prerequisite),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fix",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = GoldInk
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = GoldInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun labelFor(prerequisite: SessionPrerequisite): String = when (prerequisite) {
    SessionPrerequisite.NOTIFICATIONS ->
        "Notifications off — the recording status won't show"
    SessionPrerequisite.BATTERY_OPTIMIZATION ->
        "Battery limits active — sessions may stop when locked"
    SessionPrerequisite.BLUETOOTH ->
        "Bluetooth permission needed for the pulse sensor"
    SessionPrerequisite.MICROPHONE ->
        "Microphone permission needed for respiration"
}

/**
 * Handles a permission-request *result* with the permanently-denied fallback. The host launches
 * the permission normally; when the result is "denied", it calls this. If the system will no
 * longer show the dialog (permanently denied — [ActivityCompat.shouldShowRequestPermissionRationale]
 * is false), we open the app's settings page so the operator can grant it manually, instead of
 * the Fix button silently doing nothing.
 *
 * Note: a permanently-denied `launch(...)` returns "denied" immediately without UI, so routing
 * the fallback through the result callback is reliable — no pre-guessing required.
 */
fun onPermissionDenied(context: Context, permission: String) {
    val activity = context.findActivity()
    val willShowDialogNextTime =
        activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    if (!willShowDialogNextTime) {
        openAppSettings(context)
    }
    // Otherwise the user simply declined a real dialog; leave the card to prompt again on next tap.
}

/** Opens this app's system settings details page (for permanently-denied permissions). */
fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // No settings activity available; nothing more we can do from here.
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
