package com.bubble.ui.overlay

import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Placeholder Compose Overlay hierarchy.
 * Renders the 56dp floating bubble and coordinates bounds with OverlayHostLayout.
 */
@Composable
fun BubbleOverlayContent(
    onBoundsUpdated: (String, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    var bubbleX by remember { mutableStateOf(48f) }
    var bubbleY by remember { mutableStateOf(200f) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 56dp Floating Bubble Atom
        Box(
            modifier = Modifier
                .offset { IntOffset(bubbleX.roundToInt(), bubbleY.roundToInt()) }
                .size(56.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF2563EB)) // Brand Primary Blue
                .clickable {
                    // Interaction hook for bubble expansion
                }
                .onGloballyPositioned { coordinates ->
                    val bounds = coordinates.boundsInWindow()
                    onBoundsUpdated(
                        "bubble_primary",
                        Rect(
                            bounds.left.toInt(),
                            bounds.top.toInt(),
                            bounds.right.toInt(),
                            bounds.bottom.toInt()
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💬",
                fontSize = 24.sp
            )
        }
    }
}
