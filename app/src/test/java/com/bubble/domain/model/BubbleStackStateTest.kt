package com.bubble.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating multi-conversation stack management, promotion, and unread rollups.
 */
class BubbleStackStateTest {

    private val bubble1 = ActiveBubble("c1", "Alex", null, "com.tg", "TG", unreadCount = 2)
    private val bubble2 = ActiveBubble("c2", "Sarah", null, "com.wa", "WA", unreadCount = 3)
    private val bubble3 = ActiveBubble("c3", "Marcus", null, "com.slack", "Slack", unreadCount = 1)
    private val bubble4 = ActiveBubble("c4", "Elena", null, "com.sms", "Messages", unreadCount = 5)

    @Test
    fun visibleStack_capsAtThreeVisibleHeads() {
        val state = BubbleStackState(bubbles = listOf(bubble1, bubble2, bubble3, bubble4))
        assertEquals(3, state.visibleStack.size)
        assertEquals("c1", state.visibleStack[0].conversationId)
        assertEquals("c2", state.visibleStack[1].conversationId)
        assertEquals("c3", state.visibleStack[2].conversationId)
    }

    @Test
    fun selectConversation_promotesUnderlyingHeadToTop() {
        val state = BubbleStackState(bubbles = listOf(bubble1, bubble2, bubble3))
        val promoted = state.selectConversation("c3")

        assertEquals("c3", promoted.activeBubble?.conversationId)
        assertEquals("c3", promoted.visibleStack[0].conversationId)
        assertEquals("c1", promoted.visibleStack[1].conversationId)
    }

    @Test
    fun totalUnreadCount_sumsAcrossAllConversations() {
        val state = BubbleStackState(bubbles = listOf(bubble1, bubble2, bubble3, bubble4))
        assertEquals(11, state.totalUnreadCount) // 2 + 3 + 1 + 5
    }

    @Test
    fun removeBubble_updatesStackAndActiveLeader() {
        val state = BubbleStackState(
            bubbles = listOf(bubble1, bubble2),
            activeConversationId = "c1"
        )
        val afterRemoval = state.removeBubble("c1")

        assertEquals(1, afterRemoval.bubbles.size)
        assertEquals("c2", afterRemoval.activeBubble?.conversationId)
    }

    @Test
    fun upsertBubble_prependsNewOrUpdatesExisting() {
        val state = BubbleStackState(bubbles = listOf(bubble1))
        val withNew = state.upsertBubble(bubble2)
        assertEquals(2, withNew.bubbles.size)
        assertEquals("c2", withNew.activeBubble?.conversationId)

        val updatedBubble1 = bubble1.copy(unreadCount = 10)
        val withUpdated = withNew.upsertBubble(updatedBubble1)
        assertEquals(2, withUpdated.bubbles.size)
        assertEquals(10, withUpdated.bubbles.find { it.conversationId == "c1" }?.unreadCount)
    }
}
