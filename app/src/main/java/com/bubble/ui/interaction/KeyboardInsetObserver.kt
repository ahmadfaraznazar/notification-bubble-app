package com.bubble.ui.interaction

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Real-time software keyboard (IME) inset observer and avoidance engine.
 * Ensures floating bubbles and expanded preview cards smoothly translate above
 * active software keyboards (Gboard, SwiftKey, etc.) with zero keyboard occlusion.
 */
object KeyboardInsetObserver {

    /**
     * Calculates the vertical upward displacement required to keep the bubble
     * above the on-screen keyboard top edge.
     *
     * @param bubbleY Current top Y coordinate of the bubble in px.
     * @param bubbleHeight Height of the bubble or preview card in px.
     * @param keyboardHeight Height of the active soft keyboard in px.
     * @param screenHeight Total screen display height in px.
     * @param margin Safe clearance margin above the keyboard in px.
     * @return Positive displacement amount in px (how much to shift upward).
     */
    fun calculateImeOverlap(
        bubbleY: Float,
        bubbleHeight: Float,
        keyboardHeight: Float,
        screenHeight: Float,
        margin: Float = 48f
    ): Float {
        if (keyboardHeight <= 0f) return 0f

        val keyboardTop = screenHeight - keyboardHeight
        val bubbleBottom = bubbleY + bubbleHeight
        val overlap = bubbleBottom - (keyboardTop - margin)

        return if (overlap > 0f) overlap else 0f
    }
}

/**
 * Compose modifier that animates the component vertically upward whenever
 * the system IME keyboard opens beneath it.
 */
@Composable
fun Modifier.avoidKeyboard(
    currentYPx: Float,
    componentHeightPx: Float,
    screenHeightPx: Float,
    marginDp: Dp = 16.dp
): Modifier {
    val density = LocalDensity.current
    val marginPx = with(density) { marginDp.toPx() }
    val imeInsets = WindowInsets.ime.asPaddingValues()
    val imeHeightPx = with(density) { imeInsets.calculateBottomPadding().toPx() }

    val requiredShift = KeyboardInsetObserver.calculateImeOverlap(
        bubbleY = currentYPx,
        bubbleHeight = componentHeightPx,
        keyboardHeight = imeHeightPx,
        screenHeight = screenHeightPx,
        margin = marginPx
    )

    val animatedShiftY by animateFloatAsState(
        targetValue = -requiredShift,
        animationSpec = SpringMotion.EdgeSnapSpring,
        label = "ImeAvoidanceTranslation"
    )

    return this.then(
        Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                placeable.placeRelative(0, animatedShiftY.roundToInt())
            }
        }
    )
}
