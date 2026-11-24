package com.bubble.data.repository

import com.bubble.domain.model.NotificationMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests validating NotificationRepository queue management and backpressure ceilings.
 */
class NotificationRepositoryImplTest {

    private lateinit var repository: NotificationRepositoryImpl

    @Before
    fun setUp() = runTest {
        repository = NotificationRepositoryImpl.getInstance()
        repository.clearAll()
    }

    @Test
    fun enqueueNotification_prependsToQueue() = runTest {
        val msg1 = NotificationMessage("id1", "Alice", "Hello", "com.wa", "WhatsApp")
        val msg2 = NotificationMessage("id2", "Bob", "Hi", "com.tg", "Telegram")

        repository.enqueueNotification(msg1)
        repository.enqueueNotification(msg2)

        val queue = repository.observeActiveNotifications().first()
        assertEquals(2, queue.size)
        assertEquals("id2", queue[0].id)
        assertEquals("id1", queue[1].id)
    }

    @Test
    fun enqueueNotification_updatesExistingMessageWithoutDuplicating() = runTest {
        val original = NotificationMessage("id1", "Alice", "Hello", "com.wa", "WhatsApp")
        val updated = NotificationMessage("id1", "Alice", "Hello again", "com.wa", "WhatsApp")

        repository.enqueueNotification(original)
        repository.enqueueNotification(updated)

        val queue = repository.observeActiveNotifications().first()
        assertEquals(1, queue.size)
        assertEquals("Hello again", queue[0].textSnippet)
    }

    @Test
    fun enqueueNotification_enforces10ItemBackpressureCeiling() = runTest {
        // Enqueue 15 notifications
        for (i in 1..15) {
            repository.enqueueNotification(
                NotificationMessage("id_$i", "User $i", "Message $i", "com.app", "App")
            )
        }

        val queue = repository.observeActiveNotifications().first()
        assertEquals(NotificationRepositoryImpl.MAX_QUEUE_CAPACITY, queue.size)
        assertEquals(10, queue.size)
        // Most recent message should be at top
        assertEquals("id_15", queue[0].id)
    }

    @Test
    fun dismissNotification_removesMatchingMessage() = runTest {
        val msg1 = NotificationMessage("id1", "Alice", "Hello", "com.wa", "WhatsApp")
        val msg2 = NotificationMessage("id2", "Bob", "Hi", "com.tg", "Telegram")

        repository.enqueueNotification(msg1)
        repository.enqueueNotification(msg2)

        repository.dismissNotification("id1")

        val queue = repository.observeActiveNotifications().first()
        assertEquals(1, queue.size)
        assertEquals("id2", queue[0].id)
    }

    @Test
    fun clearAll_emptiesEntireQueue() = runTest {
        repository.enqueueNotification(NotificationMessage("id1", "Alice", "Hi", "com.wa", "WA"))
        repository.clearAll()

        val queue = repository.observeActiveNotifications().first()
        assertTrue(queue.isEmpty())
    }
}
