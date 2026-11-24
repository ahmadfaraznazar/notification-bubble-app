package com.bubble.ui.overlay

import android.graphics.Rect
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.bubble.domain.model.ActiveBubble
import com.bubble.ui.interaction.BubblePhysicsController
import com.bubble.ui.molecules.FloatingBubbleItem
import com.bubble.ui.theme.BubbleTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Compose Overlay hierarchy with spring physics, 120Hz gesture tracking,
 * and the FloatingBubbleItem interactive molecule.
 */
@Composable
fun BubbleOverlayContent(
    onBoundsUpdated: (String, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Sample active bubble state (will be replaced by StateFlow in Sprint 2)
    val sampleBubble = remember {
        ActiveBubble(
            conversationId = "demo_chat_1",
            senderName = "Alex Rivera",
            avatarUri = null,
            packageName = "org.telegram.messenger",
            appName = "Telegram",
            unreadCount = 3
        )
    }

    var isDragging by remember { mutableStateOf(false) }

    BubbleTheme {
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

            // Interactive Floating Bubble Molecule
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            physicsController.offset.value.x.roundToInt(),
                            physicsController.offset.value.y.roundToInt()
                        )
                    }
                    .scale(physicsController.scale.value)
                    .pointerInput(physicsController) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
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
                                isDragging = false
                                val velocity = velocityTracker.calculateVelocity()
                                coroutineScope.launch {
                                    physicsController.snapToNearestEdge(velocity.x)
                                }
                            },
                            onDragCancel = {
                                isDragging = false
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
                    }
            ) {
                FloatingBubbleItem(
                    bubble = sampleBubble,
                    isDragging = isDragging,
                    onClick = {
                        // Expansion toggle hook for Task 07 / Sprint 2
                    }
                )
            }
        }
    }
}
