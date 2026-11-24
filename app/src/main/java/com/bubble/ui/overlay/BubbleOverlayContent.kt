package com.bubble.ui.overlay

import android.graphics.Rect
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bubble.domain.model.ActiveBubble
import com.bubble.domain.model.BubbleStackState
import com.bubble.domain.model.NotificationMessage
import com.bubble.ui.interaction.BubblePhysicsController
import com.bubble.ui.organisms.ActiveBubbleStack
import com.bubble.ui.organisms.DismissTargetZone
import com.bubble.ui.organisms.NotificationPreviewCard
import com.bubble.ui.theme.BubbleDimensions
import com.bubble.ui.theme.BubbleTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Compose Overlay hierarchy managing spring physics, multi-conversation stacks,
 * magnetic dismiss target, and the floating expandable notification preview card.
 */
@Composable
fun BubbleOverlayContent(
    onBoundsUpdated: (String, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Multi-conversation stack state
    var stackState by remember {
        mutableStateOf(
            BubbleStackState(
                bubbles = listOf(
                    ActiveBubble(
                        conversationId = "chat_tg_1",
                        senderName = "Alex Rivera",
                        avatarUri = null,
                        packageName = "org.telegram.messenger",
                        appName = "Telegram",
                        unreadCount = 2
                    ),
                    ActiveBubble(
                        conversationId = "chat_wa_2",
                        senderName = "Sarah Connor",
                        avatarUri = null,
                        packageName = "com.whatsapp",
                        appName = "WhatsApp",
                        unreadCount = 1
                    ),
                    ActiveBubble(
                        conversationId = "chat_slack_3",
                        senderName = "Marcus Vance",
                        avatarUri = null,
                        packageName = "com.Slack",
                        appName = "Slack",
                        unreadCount = 4
                    )
                )
            )
        )
    }

    var isDragging by remember { mutableStateOf(false) }
    var isPreviewExpanded by remember { mutableStateOf(false) }
    var isUnmasked by remember { mutableStateOf(false) }

    BubbleTheme {
        BoxWithConstraints(
            modifier = modifier.fillMaxSize()
        ) {
            val screenWidthPx = with(density) { maxWidth.toPx() }
            val screenHeightPx = with(density) { maxHeight.toPx() }
            val cardWidthPx = with(density) { BubbleDimensions.PreviewCardMaxWidth.toPx() }
            val spacingPx = with(density) { 12.dp.toPx() }

            val physicsController = remember(screenWidthPx, screenHeightPx) {
                BubblePhysicsController(
                    density = density,
                    screenWidthPx = screenWidthPx,
                    screenHeightPx = screenHeightPx
                )
            }

            val velocityTracker = remember { VelocityTracker() }

            val isOnRightEdge = physicsController.offset.value.x >
                (screenWidthPx - physicsController.bubbleDiameterPx) / 2f

            val isMagneticActive = isDragging && physicsController.checkMagneticZone(physicsController.offset.value)

            // Tactile tick on magnetic pull lock
            LaunchedEffect(isMagneticActive) {
                if (isMagneticActive) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }

            // Organism: Magnetic Dismiss Target Zone ("The Black Hole")
            DismissTargetZone(
                isVisible = isDragging,
                isMagneticActive = isMagneticActive,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Active Leader Bubble & Message
            val activeBubble = stackState.activeBubble
            val activeMessage = remember(activeBubble) {
                activeBubble?.let {
                    NotificationMessage(
                        id = it.conversationId,
                        senderName = it.senderName,
                        textSnippet = "Hey! Let's review the release checklist before deploying the build.",
                        packageName = it.packageName,
                        appName = it.appName,
                        avatarUri = it.avatarUri,
                        timestampMs = it.timestampMs,
                        isMasked = false,
                        hasQuickReply = true
                    )
                }
            }

            // Expanded Notification Preview Card
            if (activeMessage != null && isPreviewExpanded && !isDragging) {
                val cardX = if (isOnRightEdge) {
                    physicsController.offset.value.x - cardWidthPx - spacingPx
                } else {
                    physicsController.offset.value.x + physicsController.bubbleDiameterPx + spacingPx
                }
                val cardY = physicsController.offset.value.y.coerceIn(
                    physicsController.minY,
                    screenHeightPx - with(density) { 220.dp.toPx() }
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(cardX.roundToInt(), cardY.roundToInt()) }
                        .onGloballyPositioned { coordinates ->
                            val bounds = coordinates.boundsInWindow()
                            onBoundsUpdated(
                                "preview_card",
                                Rect(
                                    bounds.left.toInt(),
                                    bounds.top.toInt(),
                                    bounds.right.toInt(),
                                    bounds.bottom.toInt()
                                )
                            )
                        }
                ) {
                    NotificationPreviewCard(
                        notification = activeMessage,
                        isVisible = isPreviewExpanded,
                        isUnmasked = isUnmasked,
                        isAnchoredToRight = isOnRightEdge,
                        onReplyClick = {
                            // Quick reply inline field trigger in Task 14
                        },
                        onMarkAsReadClick = {
                            isPreviewExpanded = false
                        },
                        onDismissClick = {
                            val activeId = activeBubble.conversationId
                            stackState = stackState.removeBubble(activeId)
                            isPreviewExpanded = false
                        },
                        onToggleUnmask = {
                            isUnmasked = !isUnmasked
                        }
                    )
                }
            }

            // Multi-Conversation Bubble Stack
            if (stackState.bubbles.isNotEmpty()) {
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
                                    isPreviewExpanded = false // Collapse card during drag
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
                                    val activeId = stackState.activeBubble?.conversationId
                                    coroutineScope.launch {
                                        physicsController.handleRelease(
                                            currentVelocityX = velocity.x,
                                            onDismissed = {
                                                if (activeId != null) {
                                                    stackState = stackState.removeBubble(activeId)
                                                }
                                            }
                                        )
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
                                "bubble_stack_primary",
                                Rect(
                                    bounds.left.toInt(),
                                    bounds.top.toInt(),
                                    bounds.right.toInt(),
                                    bounds.bottom.toInt()
                                )
                            )
                        }
                ) {
                    ActiveBubbleStack(
                        stackState = stackState,
                        isOnRightEdge = isOnRightEdge,
                        isDragging = isDragging,
                        onBubbleClick = { clickedBubble ->
                            // Tapping primary head toggles preview card
                            isPreviewExpanded = !isPreviewExpanded
                        },
                        onSelectBubble = { conversationId ->
                            stackState = stackState.selectConversation(conversationId)
                            isPreviewExpanded = true
                        }
                    )
                }
            }
        }
    }
}
