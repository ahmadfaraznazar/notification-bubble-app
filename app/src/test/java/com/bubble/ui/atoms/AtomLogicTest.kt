package com.bubble.ui.atoms

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests validating formatting rules for atomic visual primitives.
 */
class AtomLogicTest {

    @Test
    fun unreadCountThresholds_formatCorrectly() {
        assertEquals("1", formatUnreadCount(1))
        assertEquals("42", formatUnreadCount(42))
        assertEquals("99", formatUnreadCount(99))
        assertEquals("99+", formatUnreadCount(100))
        assertEquals("99+", formatUnreadCount(999))
    }

    private fun formatUnreadCount(count: Int): String {
        return if (count > 99) "99+" else count.toString()
    }
}
