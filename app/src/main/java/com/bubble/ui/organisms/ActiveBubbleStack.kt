package com.bubble.ui.organisms

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.bubble.domain.model.ActiveBubble
import com.bubble.domain.model.BubbleStackState
import com.bubble.ui.interaction.SpringMotion
import com.bubble.ui.molecules.FloatingBubbleItem
import com.bubble.ui.theme.BubbleDimensions

/**
 * Organism rendering up to 3 stacked conversation heads with directional depth staggering,
 * follower spring lag, and background head promotion.
 */
@Composable
fun ActiveBubbleStack(
    stackState: BubbleStackState,
    onBubbleClick: (ActiveBubble) -> Unit,
    onSelectBubble: (String) -> Unit,
    modifier: Modifier = Modifier,
    isOnRightEdge: Boolean = false,
    isDragging: Boolean = false
) {
    val visibleBubbles = stackState.visibleStack
    if (visibleBubbles.isEmpty()) return

    // Direction multiplier: if anchored to right edge, stagger inward (negative X)
    val directionX = if (isOnRightEdge) -1f else 1f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart
    ) {
        // Render back-to-front so index 0 (the leader) renders on top
        visibleBubbles.withIndex().reversed().forEach { (depthIndex, bubble) ->
            val isPrimary = depthIndex == 0

            // Base spatial tokens per depth tier
            val targetOffsetX = (BubbleDimensions.StackOffsetHorizontal * depthIndex * directionX)
            val targetOffsetY = (BubbleDimensions.StackOffsetVertical * depthIndex)
            val targetScale = 1.0f - (0.05f * depthIndex)
            val targetAlpha = 1.0f - (0.10f * depthIndex)

            // Animated follower lag when dragging the stack leader
            val animatedOffsetX by animateDpAsState(
                targetValue = if (isDragging && !isPrimary) targetOffsetX * 1.3f else targetOffsetX,
                animationSpec = SpringMotion.EdgeSnapSpring,
                label = "StackOffsetX_$depthIndex"
            )

            val animatedOffsetY by animateDpAsState(
                targetValue = if (isDragging && !isPrimary) targetOffsetY * 1.3f else targetOffsetY,
                animationSpec = SpringMotion.EdgeSnapSpring,
                label = "StackOffsetY_$depthIndex"
            )

            val animatedScale by animateFloatAsState(
                targetValue = targetScale,
                label = "StackScale_$depthIndex"
            )

            Box(
                modifier = Modifier
                    .offset(x = animatedOffsetX, y = animatedOffsetY)
                    .scale(animatedScale)
                    .alpha(targetAlpha)
            ) {
                FloatingBubbleItem(
                    bubble = bubble,
                    isDragging = isDragging && isPrimary,
                    onClick = {
                        if (isPrimary) {
                            onBubbleClick(bubble)
                        } else {
                            onSelectBubble(bubble.conversationId)
                        }
                    }
                )
            }
        }
    }
}
