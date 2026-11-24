package com.bubble.ui.interaction

import android.content.res.Configuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating orientation detection, landscape auto-docking conditions,
 * and edge coordinate calculations.
 */
class OrientationObserverTest {

    @Test
    fun isLandscape_identifiesOrientationsCorrectly() {
        assertTrue(OrientationObserver.isLandscape(Configuration.ORIENTATION_LANDSCAPE))
        assertFalse(OrientationObserver.isLandscape(Configuration.ORIENTATION_PORTRAIT))
        assertFalse(OrientationObserver.isLandscape(Configuration.ORIENTATION_UNDEFINED))
    }

    @Test
    fun shouldAutoDock_whenLandscapeAndNotExpanded_returnsTrue() {
        assertTrue(
            OrientationObserver.shouldAutoDock(
                isLandscape = true,
                isTemporarilyExpanded = false
            )
        )
    }

    @Test
    fun shouldAutoDock_whenLandscapeAndTemporarilyExpanded_returnsFalse() {
        assertFalse(
            OrientationObserver.shouldAutoDock(
                isLandscape = true,
                isTemporarilyExpanded = true
            )
        )
    }

    @Test
    fun shouldAutoDock_whenPortrait_returnsFalseAlways() {
        assertFalse(
            OrientationObserver.shouldAutoDock(
                isLandscape = false,
                isTemporarilyExpanded = false
            )
        )
    }

    @Test
    fun calculateDockedX_placesPillFlushAgainstBorder() {
        val screenWidth = 2400f
        val pillWidth = 24f

        val rightX = OrientationObserver.calculateDockedX(
            isOnRightEdge = true,
            screenWidthPx = screenWidth,
            pillWidthPx = pillWidth
        )
        assertEquals(2376f, rightX, 0.01f)

        val leftX = OrientationObserver.calculateDockedX(
            isOnRightEdge = false,
            screenWidthPx = screenWidth,
            pillWidthPx = pillWidth
        )
        assertEquals(0f, leftX, 0.01f)
    }

    @Test
    fun clampPillY_constrainsPillWithinSafeVerticalGutters() {
        val screenHeight = 1080f
        val pillHeight = 72f
        val margin = 64f

        val tooHigh = OrientationObserver.clampPillY(10f, screenHeight, pillHeight, margin)
        assertEquals(64f, tooHigh, 0.01f)

        val tooLow = OrientationObserver.clampPillY(1050f, screenHeight, pillHeight, margin)
        assertEquals(1080f - 72f - 64f, tooLow, 0.01f)

        val middle = OrientationObserver.clampPillY(500f, screenHeight, pillHeight, margin)
        assertEquals(500f, middle, 0.01f)
    }
}
