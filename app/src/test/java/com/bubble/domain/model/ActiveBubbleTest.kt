package com.bubble.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests validating ActiveBubble domain model immutability and default state values.
 */
class ActiveBubbleTest {

    @Test
    fun activeBubble_createsWithExpectedDefaults() {
        val bubble = ActiveBubble(
            conversationId = "conv_123",
            senderName = "Sarah Connor",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            unreadCount = 2
        )

        assertEquals("conv_123", bubble.conversationId)
        assertEquals("Sarah Connor", bubble.senderName)
        assertEquals("WhatsApp", bubble.appName)
        assertEquals(2, bubble.unreadCount)
        assertNull(bubble.avatarUri)
        assertNull(bubble.appIconRes)
    }

    @Test
    fun activeBubble_supportsCopyTransformation() {
        val original = ActiveBubble(
            conversationId = "conv_123",
            senderName = "Sarah Connor",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            unreadCount = 1
        )

        val updated = original.copy(unreadCount = 5)

        assertEquals(1, original.unreadCount)
        assertEquals(5, updated.unreadCount)
        assertEquals(original.conversationId, updated.conversationId)
    }
}
