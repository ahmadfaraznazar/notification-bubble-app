package com.bubble.ui.interaction

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

/**
 * Controller responsible for 120Hz gesture tracking, zero-allocation drag updates,
 * organic edge-snapping, and magnetic dismiss zone attraction.
 */
class BubblePhysicsController(
    private val density: Density,
    val screenWidthPx: Float,
    val screenHeightPx: Float,
    val statusBarHeightPx: Float = 0f,
    val navBarHeightPx: Float = 0f,
    var isReducedMotionEnabled: Boolean = false
) {
    val edgeMarginPx: Float = with(density) { 16.dp.toPx() }
    val bubbleDiameterPx: Float = with(density) { 56.dp.toPx() }
    val magneticRadiusPx: Float = with(density) { 100.dp.toPx() }

    // Reusable Animatable vector channels (zero heap allocation in 120Hz drag loop)
    val offset = Animatable(
        initialValue = Offset(edgeMarginPx, screenHeightPx * 0.3f),
        typeConverter = Offset.VectorConverter
    )

    val scale = Animatable(1.0f)

    // Clamping boundaries (enforcing 16dp safe margins from display edges)
    val minX: Float get() = edgeMarginPx
    val maxX: Float get() = (screenWidthPx - edgeMarginPx - bubbleDiameterPx).coerceAtLeast(minX)
    val minY: Float get() = statusBarHeightPx + edgeMarginPx
    val maxY: Float get() = (screenHeightPx - navBarHeightPx - bubbleDiameterPx - edgeMarginPx).coerceAtLeast(minY)

    // Bottom-center magnetic dismiss target
    val dismissTargetCenter: Offset
        get() = Offset(
            x = screenWidthPx / 2f,
            y = screenHeightPx - navBarHeightPx - with(density) { 48.dp.toPx() }
        )

    val dismissSnapOffset: Offset
        get() = Offset(
            x = dismissTargetCenter.x - bubbleDiameterPx / 2f,
            y = dismissTargetCenter.y - bubbleDiameterPx / 2f
        )

    /**
     * Determines whether the bubble center is within magnetic pull range of the dismiss target.
     */
    fun checkMagneticZone(currentOffset: Offset): Boolean {
        val bubbleCenter = Offset(
            x = currentOffset.x + bubbleDiameterPx / 2f,
            y = currentOffset.y + bubbleDiameterPx / 2f
        )
        val distance = hypot(
            bubbleCenter.x - dismissTargetCenter.x,
            bubbleCenter.y - dismissTargetCenter.y
        )
        return distance <= magneticRadiusPx
    }

    /**
     * In-place drag coordinate update without instantiating new objects in hot loop.
     * When near magnetic target, smoothly damps and pulls toward center.
     */
    suspend fun onDrag(dragAmount: Offset) {
        val rawX = offset.value.x + dragAmount.x
        val rawY = offset.value.y + dragAmount.y

        val targetOffset = Offset(rawX, rawY)
        val isMagnetic = checkMagneticZone(targetOffset)

        if (isMagnetic) {
            // Magnetic attraction: pull 35% toward target center
            val pullRatio = 0.35f
            val pulledX = rawX + (dismissSnapOffset.x - rawX) * pullRatio
            val pulledY = rawY + (dismissSnapOffset.y - rawY) * pullRatio
            offset.snapTo(Offset(pulledX, pulledY))
        } else {
            val clampedX = rawX.coerceIn(minX, maxX)
            val clampedY = rawY.coerceIn(minY, maxY)
            offset.snapTo(Offset(clampedX, clampedY))
        }
    }

    /**
     * Calculates nearest screen edge taking horizontal release velocity into account.
     */
    fun calculateTargetX(currentVelocityX: Float): Float {
        return when {
            currentVelocityX > VELOCITY_FLING_THRESHOLD -> maxX
            currentVelocityX < -VELOCITY_FLING_THRESHOLD -> minX
            offset.value.x > (screenWidthPx - bubbleDiameterPx) / 2f -> maxX
            else -> minX
        }
    }

    /**
     * Snaps bubble to nearest left or right display edge using damped spring physics.
     */
    suspend fun snapToNearestEdge(currentVelocityX: Float = 0f) {
        val targetX = calculateTargetX(currentVelocityX)
        val clampedY = offset.value.y.coerceIn(minY, maxY)
        val targetOffset = Offset(targetX, clampedY)

        val animationSpec = if (isReducedMotionEnabled) {
            SpringMotion.ReducedMotionOffsetSpec
        } else {
            SpringMotion.EdgeSnapSpring
        }

        offset.animateTo(
            targetValue = targetOffset,
            animationSpec = animationSpec
        )
    }

    /**
     * Handles finger lift: either executes magnetic dismiss or snaps to nearest edge.
     */
    suspend fun handleRelease(
        currentVelocityX: Float,
        onDismissed: () -> Unit
    ) {
        if (checkMagneticZone(offset.value)) {
            offset.snapTo(dismissSnapOffset)
            dismissWithExitAnimation()
            onDismissed()
            scale.snapTo(1.0f)
            offset.snapTo(Offset(minX, screenHeightPx * 0.3f))
        } else {
            snapToNearestEdge(currentVelocityX)
        }
    }

    /**
     * Animates bubble scale down to 0 on dismiss.
     */
    suspend fun dismissWithExitAnimation() {
        val animationSpec = if (isReducedMotionEnabled) {
            SpringMotion.ReducedMotionFloatSpec
        } else {
            SpringMotion.DismissExitSpring
        }

        scale.animateTo(
            targetValue = 0f,
            animationSpec = animationSpec
        )
    }

    companion object {
        const val VELOCITY_FLING_THRESHOLD = 500f // px/sec
    }
}
