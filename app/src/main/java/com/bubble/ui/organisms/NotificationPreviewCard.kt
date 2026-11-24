package com.bubble.ui.organisms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bubble.domain.model.NotificationMessage
import com.bubble.ui.interaction.SpringMotion
import com.bubble.ui.theme.BubbleDimensions
import com.bubble.ui.theme.BubbleTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Organism representing the expanded floating notification preview card.
 * Anchored adjacent to the resting bubble, offering glanceable reading,
 * tap-to-unmask privacy protection, and immediate action buttons.
 */
@Composable
fun NotificationPreviewCard(
    notification: NotificationMessage?,
    isVisible: Boolean,
    isUnmasked: Boolean,
    onReplyClick: () -> Unit,
    onMarkAsReadClick: () -> Unit,
    onDismissClick: () -> Unit,
    onToggleUnmask: () -> Unit,
    modifier: Modifier = Modifier,
    isAnchoredToRight: Boolean = false
) {
    AnimatedVisibility(
        visible = isVisible && notification != null,
        enter = fadeIn() + scaleIn(
            initialScale = 0.85f,
            animationSpec = SpringMotion.EdgeSnapSpring
        ),
        exit = fadeOut() + scaleOut(
            targetScale = 0.85f,
            animationSpec = SpringMotion.DismissExitSpring
        ),
        modifier = modifier
    ) {
        if (notification == null) return@AnimatedVisibility

        val timeString = formatRelativeTimestamp(notification.timestampMs)
        val displaySnippet = if (notification.isMasked && !isUnmasked) {
            "•••••• (Tap to view)"
        } else {
            notification.textSnippet
        }

        Box(
            modifier = Modifier
                .width(BubbleDimensions.PreviewCardMaxWidth)
                .shadow(
                    elevation = BubbleDimensions.PreviewCardElevation,
                    shape = RoundedCornerShape(BubbleDimensions.PreviewCardCornerRadius)
                )
                .clip(RoundedCornerShape(BubbleDimensions.PreviewCardCornerRadius))
                .background(BubbleTheme.colors.surfaceOverlay)
                .border(
                    width = 1.dp,
                    color = BubbleTheme.colors.borderStroke,
                    shape = RoundedCornerShape(BubbleDimensions.PreviewCardCornerRadius)
                )
                .padding(14.dp)
        ) {
            Column {
                // 1. Header: Mini App Icon + App Label + Relative Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mini App Source Indicator
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = notification.appName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = notification.appName,
                        style = BubbleTheme.typography.labelMedium,
                        color = BubbleTheme.colors.onSurfaceOverlaySubtle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = timeString,
                        style = BubbleTheme.typography.labelMedium,
                        color = BubbleTheme.colors.onSurfaceOverlaySubtle
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Body: Sender Name (bold)
                Text(
                    text = notification.senderName,
                    style = BubbleTheme.typography.titleMedium,
                    color = BubbleTheme.colors.onSurfaceOverlay,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // 3. Body: Message Snippet (clamped with true ellipsis '…')
                if (notification.isMasked && !isUnmasked) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BubbleTheme.colors.surfaceOverlayVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onToggleUnmask)
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = displaySnippet,
                            style = BubbleTheme.typography.bodyMedium,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Text(
                        text = displaySnippet,
                        style = BubbleTheme.typography.bodyMedium,
                        color = BubbleTheme.colors.onSurfaceOverlaySubtle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Footer Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (notification.hasQuickReply) {
                        CardActionButton(
                            label = "Reply",
                            icon = "💬",
                            onClick = onReplyClick,
                            isPrimary = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    CardActionButton(
                        label = "Read",
                        icon = "✓",
                        onClick = onMarkAsReadClick,
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )

                    CardActionButton(
                        label = "Dismiss",
                        icon = "✕",
                        onClick = onDismissClick,
                        isPrimary = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardActionButton(
    label: String,
    icon: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isPrimary) Color(0xFF2563EB) else BubbleTheme.colors.actionButtonBg
    val textColor = if (isPrimary) Color.White else BubbleTheme.colors.actionButtonText

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = modifier
            .height(32.dp)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "$label notification"
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

private fun formatRelativeTimestamp(timestampMs: Long): String {
    val diff = System.currentTimeMillis() - timestampMs
    return when {
        diff < 60_000L -> "Just now"
        diff < 3600_000L -> "${diff / 60_000L}m ago"
        diff < 86400_000L -> "${diff / 3600_000L}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestampMs))
    }
}
