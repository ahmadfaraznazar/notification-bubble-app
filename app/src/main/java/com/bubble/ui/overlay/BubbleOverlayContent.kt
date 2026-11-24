package com.bubble.ui.overlay

import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bubble.ui.interaction.BubblePhysicsController
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Compose Overlay hierarchy with spring physics and zero-allocation drag tracking.
 * Coordinates 120Hz gesture tracking with OverlayHostLayout.
 */
@Composable
fun BubbleOverlayContent(
    onBoundsUpdated: (String, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val physicsController = remember(screenWidthPx, screenHeightPx) {
            BubblePhysicsController(
                density = density,
                screenWidthPx = screenWidthPx,
                screenHeightPx = screenHeightPx
            )
        }

        val velocityTracker = remember { VelocityTracker() }

        // 56dp Floating Bubble with Zero-Allocation Drag & Edge Snapping
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        physicsController.offset.value.x.roundToInt(),
                        physicsController.offset.value.y.roundToInt()
                    )
                }
                .scale(physicsController.scale.value)
                .size(56.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF2563EB)) // Brand Primary Blue
                .clickable {
                    // Interaction hook for bubble expansion
                }
                .pointerInput(physicsController) {
                    detectDragGestures(
                        onDragStart = {
                            velocityTracker.resetTracking()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            coroutineScope.launch {
                                physicsController.onDrag(dragAmount)
                            }
                        },
                        onDragEnd = {
                            val velocity = velocityTracker.calculateVelocity()
                            coroutineScope.launch {
                                physicsController.snapToNearestEdge(velocity.x)
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                physicsController.snapToNearestEdge(0f)
                            }
                        }
                    )
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
