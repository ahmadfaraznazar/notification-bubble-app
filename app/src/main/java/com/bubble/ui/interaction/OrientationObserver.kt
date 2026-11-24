package com.bubble.ui.interaction

import android.content.res.Configuration

/**
 * Orientation observer and landscape docking calculation engine.
 * Governs auto-collapse into low-profile edge tabs during landscape gaming and video playback.
 */
object OrientationObserver {

    /**
     * Determines whether the given orientation is landscape.
     */
    fun isLandscape(orientation: Int): Boolean {
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    /**
     * Determines whether the overlay should collapse into a side-pill.
     */
    fun shouldAutoDock(
        isLandscape: Boolean,
        isTemporarilyExpanded: Boolean
    ): Boolean {
        return isLandscape && !isTemporarilyExpanded
    }

    /**
     * Calculates the horizontal X coordinate to dock the pill flush against the glass border.
     */
    fun calculateDockedX(
        isOnRightEdge: Boolean,
        screenWidthPx: Float,
        pillWidthPx: Float
    ): Float {
        return if (isOnRightEdge) {
            screenWidthPx - pillWidthPx
        } else {
            0f
        }
    }

    /**
     * Clamps the vertical Y coordinate so the side-pill stays within comfortable thumb reach.
     */
    fun clampPillY(
        currentY: Float,
        screenHeightPx: Float,
        pillHeightPx: Float,
        marginPx: Float = 64f
    ): Float {
        val minY = marginPx
        val maxY = screenHeightPx - pillHeightPx - marginPx
        return currentY.coerceIn(minY, maxY)
    }
}
