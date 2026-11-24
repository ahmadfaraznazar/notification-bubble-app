package com.bubble.domain.repository

import com.bubble.domain.model.NotificationMessage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface governing the reactive stream of intercepted notifications.
 */
interface NotificationRepository {
    /**
     * Observes the active queue of pending notification messages.
     */
    fun observeActiveNotifications(): Flow<List<NotificationMessage>>

    /**
     * Enqueues a newly intercepted or updated notification.
     */
    suspend fun enqueueNotification(notification: NotificationMessage)

    /**
     * Dismisses a notification message by ID.
     */
    suspend fun dismissNotification(id: String)

    /**
     * Clears all active notifications from the queue.
     */
    suspend fun clearAll()
}
