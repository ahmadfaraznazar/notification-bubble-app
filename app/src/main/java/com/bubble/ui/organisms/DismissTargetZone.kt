package com.bubble.ui.organisms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bubble.ui.interaction.SpringMotion
import com.bubble.ui.theme.BubbleDimensions
import com.bubble.ui.theme.BubbleTheme

/**
 * Organism representing the bottom-center magnetic dismiss target ("The Black Hole").
 * Expands from 64dp to 88dp and turns urgent red when the bubble enters the magnetic pull radius.
 */
@Composable
fun DismissTargetZone(
    isVisible: Boolean,
    isMagneticActive: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(initialScale = 0.6f, animationSpec = SpringMotion.TouchPressSpring),
        exit = fadeOut() + scaleOut(targetScale = 0.6f),
        modifier = modifier
    ) {
        // Dynamic target size: 64dp resting -> 88dp magnetic active
        val targetSize by animateDpAsState(
            targetValue = if (isMagneticActive) {
                BubbleDimensions.DismissTargetActiveSize
            } else {
                BubbleDimensions.DismissTargetRestingSize
            },
            animationSpec = SpringMotion.EdgeSnapSpring,
            label = "DismissTargetSize"
        )

        // Dynamic background color: translucent dark -> urgent warning red
        val targetBgColor by animateColorAsState(
            targetValue = if (isMagneticActive) {
                BubbleTheme.colors.dismissTargetActiveBg
            } else {
                BubbleTheme.colors.dismissTargetBg
            },
            label = "DismissTargetBgColor"
        )

        Box(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .size(targetSize)
                .shadow(elevation = if (isMagneticActive) 12.dp else 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(targetBgColor)
                .border(
                    width = if (isMagneticActive) 2.dp else 1.dp,
                    color = if (isMagneticActive) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .semantics {
                    contentDescription = if (isMagneticActive) {
                        "Release to dismiss notification"
                    } else {
                        "Drag here to dismiss"
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                color = BubbleTheme.colors.dismissTargetIcon,
                fontSize = if (isMagneticActive) 28.sp else 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
