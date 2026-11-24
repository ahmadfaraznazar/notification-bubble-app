package com.bubble.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spatial and dimension tokens conforming to persistent design system specifications.
 */
object BubbleDimensions {
    // Bubble Diameter Variants
    val BubbleSizeSmall: Dp = 48.dp
    val BubbleSizeDefault: Dp = 56.dp
    val BubbleSizeLarge: Dp = 64.dp

    // Touch Bounds (Strict Accessibility Minimum Floor)
    val MinTouchTarget: Dp = 48.dp

    // Sub-Element Anchors
    val AppBadgeSize: Dp = 18.dp
    val AppBadgeCutoutPadding: Dp = 2.dp
    val UnreadBadgeHeight: Dp = 20.dp
    val UnreadBadgeMinWidth: Dp = 20.dp
    val UnreadBadgePaddingHorizontal: Dp = 6.dp

    // Multi-Head Stacking Offsets
    val StackOffsetHorizontal: Dp = 8.dp
    val StackOffsetVertical: Dp = 4.dp
    const val MaxVisibleStackHeads: Int = 3

    // System Navigation & Display Insets
    val GestureSafeEdgeMargin: Dp = 16.dp
    val KeyboardAvoidancePadding: Dp = 16.dp

    // Expanded Preview Floating Card
    val PreviewCardMinWidth: Dp = 280.dp
    val PreviewCardMaxWidth: Dp = 320.dp
    val PreviewCardElevation: Dp = 8.dp
    val PreviewCardCornerRadius: Dp = 16.dp

    // Dismiss Target Zone
    val DismissTargetRestingSize: Dp = 64.dp
    val DismissTargetActiveSize: Dp = 88.dp
    val DismissMagneticPullRadius: Dp = 100.dp
}
