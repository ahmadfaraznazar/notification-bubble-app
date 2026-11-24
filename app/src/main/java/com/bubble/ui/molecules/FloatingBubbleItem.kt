package com.bubble.ui.molecules

import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.bubble.domain.model.ActiveBubble
import com.bubble.ui.atoms.AppBadgeIcon
import com.bubble.ui.atoms.AvatarCircle
import com.bubble.ui.atoms.UnreadCounterBadge
import com.bubble.ui.interaction.SpringMotion
import com.bubble.ui.theme.BubbleDimensions

/**
 * Interactive Molecule combining AvatarCircle, AppBadgeIcon, and UnreadCounterBadge.
 * Delivers 0.92f tactile spring depress and dynamic elevation under touch.
 */
@Composable
fun FloatingBubbleItem(
    bubble: ActiveBubble,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile spring press depress (0.92f scale recoil)
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = SpringMotion.TouchPressSpring,
        label = "BubblePressScale"
    )

    // Dynamic elevation: 6dp resting -> 12dp active dragging
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 12.dp else 6.dp,
        label = "BubbleElevation"
    )

    // Trigger subtle tactile feedback on touch press
    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Box(
        modifier = modifier
            .size(BubbleDimensions.BubbleSizeDefault)
            .scale(pressScale)
            .shadow(elevation = elevation, shape = CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Atom 1: 56dp Circular Avatar
        AvatarCircle(
            senderName = bubble.senderName,
            avatarUri = bubble.avatarUri?.let { Uri.parse(it) },
            diameter = BubbleDimensions.BubbleSizeDefault,
            modifier = Modifier.matchParentSize()
        )

        // Atom 2: Bottom-Right App Source Badge (18dp with 2dp cutout)
        AppBadgeIcon(
            appName = bubble.appName,
            iconRes = bubble.appIconRes,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
        )

        // Atom 3: Top-Right Unread Counter Pill Badge
        if (bubble.unreadCount > 0) {
            UnreadCounterBadge(
                unreadCount = bubble.unreadCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
            )
        }
    }
}
