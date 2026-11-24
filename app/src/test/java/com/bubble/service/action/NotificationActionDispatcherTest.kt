package com.bubble.service.action

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/**
 * Unit tests validating NotificationActionDispatcher cache lifecycle and safe dispatch fallbacks.
 */
class NotificationActionDispatcherTest {

    @Before
    fun setUp() {
        NotificationActionDispatcher.clearAll()
    }

    @Test
    fun initialState_hasNoPendingActions() {
        assertFalse(NotificationActionDispatcher.hasReplyAction("msg_123"))
        assertEquals(0, NotificationActionDispatcher.pendingCount())
    }

    @Test
    fun removeAction_whenNotRegistered_doesNotThrow() {
        NotificationActionDispatcher.removeAction("non_existent_id")
        assertEquals(0, NotificationActionDispatcher.pendingCount())
    }

    @Test
    fun clearAll_resetsActionCount() {
        NotificationActionDispatcher.clearAll()
        assertEquals(0, NotificationActionDispatcher.pendingCount())
    }

    @Test
    fun hasReplyAction_whenUnregistered_returnsFalse() {
        assertFalse(NotificationActionDispatcher.hasReplyAction("unregistered_id"))
    }
}
