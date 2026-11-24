package com.bubble.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bubble.ui.theme.BubbleDimensions
import com.bubble.ui.theme.BubbleTheme

/**
 * 12dp x 36dp translucent side-pill visual primitive.
 * Docked flush against the display glass in landscape mode to minimize screen occlusion
 * during video playback and immersive gaming.
 */
@Composable
fun DockedSidePill(
    isOnRightEdge: Boolean,
    unreadCount: Int,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = BubbleDimensions.LandscapeSidePillWidth,
    height: Dp = BubbleDimensions.LandscapeSidePillHeight
) {
    // Round only the interior-facing corners
    val pillShape = if (isOnRightEdge) {
        RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 0.dp, bottomEnd = 0.dp)
    } else {
        RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp, topStart = 0.dp, bottomStart = 0.dp)
    }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .alpha(0.65f)
            .clip(pillShape)
            .background(BubbleTheme.colors.surfaceOverlay)
            .border(width = 1.dp, color = BubbleTheme.colors.borderStroke, shape = pillShape)
            .clickable(onClick = onExpandClick)
            .semantics {
                contentDescription = "$unreadCount unread notifications. Tap to expand bubble."
            },
        contentAlignment = Alignment.Center
    ) {
        // Notification dot indicator inside the pill
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(BubbleTheme.colors.unreadBadgeBg)
            )
        }
    }
}
