package com.bubble.ui.interaction

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset

/**
 * Standard spring physics animation configurations for floating overlays.
 * Conforms to 120Hz zero-jank tactile feedback and WCAG reduced motion standards.
 */
object SpringMotion {

    /**
     * Damped organic edge-snapping spring.
     * Subtle bounce without excessive oscillation.
     */
    val EdgeSnapSpring: AnimationSpec<Offset> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /**
     * Quick, snappy spring for scaling down on dismiss.
     */
    val DismissExitSpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Crisp spring for touch press / lift feedback (0.92f press scale).
     */
    val TouchPressSpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Accessible linear transition used when user enables "Remove animations"
     * in Android Accessibility settings (WCAG 2.2 / A11Y-05 compliance).
     */
    val ReducedMotionOffsetSpec: AnimationSpec<Offset> = tween(
        durationMillis = 100
    )

    val ReducedMotionFloatSpec: AnimationSpec<Float> = tween(
        durationMillis = 100
    )
}
