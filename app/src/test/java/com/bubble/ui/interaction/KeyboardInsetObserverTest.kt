package com.bubble.ui.interaction

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests validating software keyboard (IME) overlap calculations and avoidance math.
 */
class KeyboardInsetObserverTest {

    private val screenHeight = 2400f
    private val bubbleHeight = 112f // 56dp * 2
    private val margin = 48f

    @Test
    fun calculateImeOverlap_whenKeyboardClosed_returnsZero() {
        val overlap = KeyboardInsetObserver.calculateImeOverlap(
            bubbleY = 1800f,
            bubbleHeight = bubbleHeight,
            keyboardHeight = 0f,
            screenHeight = screenHeight,
            margin = margin
        )
        assertEquals(0f, overlap, 0.01f)
    }

    @Test
    fun calculateImeOverlap_whenBubbleIsHighAboveKeyboard_returnsZero() {
        // Keyboard height 800px (keyboard top at 2400 - 800 = 1600px)
        // Bubble is at Y = 500px, bottom at 612px -> well above 1600px - 48px
        val overlap = KeyboardInsetObserver.calculateImeOverlap(
            bubbleY = 500f,
            bubbleHeight = bubbleHeight,
            keyboardHeight = 800f,
            screenHeight = screenHeight,
            margin = margin
        )
        assertEquals(0f, overlap, 0.01f)
    }

    @Test
    fun calculateImeOverlap_whenBubbleCollidesWithKeyboard_returnsExactDisplacement() {
        // Keyboard top = 2400 - 800 = 1600px; safe ceiling = 1600 - 48 = 1552px
        // Bubble top at 1600px, bottom at 1712px
        // Overlap = 1712 - 1552 = 160px
        val overlap = KeyboardInsetObserver.calculateImeOverlap(
            bubbleY = 1600f,
            bubbleHeight = bubbleHeight,
            keyboardHeight = 800f,
            screenHeight = screenHeight,
            margin = margin
        )
        assertEquals(160f, overlap, 0.01f)
    }

    @Test
    fun calculateImeOverlap_whenBubbleIsNearScreenBottom_shiftsEntirelyAboveKeyboard() {
        // Bubble top at 2200px, bottom at 2312px
        // Safe ceiling = 1552px
        // Overlap = 2312 - 1552 = 760px
        val overlap = KeyboardInsetObserver.calculateImeOverlap(
            bubbleY = 2200f,
            bubbleHeight = bubbleHeight,
            keyboardHeight = 800f,
            screenHeight = screenHeight,
            margin = margin
        )
        assertEquals(760f, overlap, 0.01f)
    }
}
