package com.bubble.data.repository

import com.bubble.domain.model.NotificationMessage
import com.bubble.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory reactive implementation of NotificationRepository.
 * Manages active notification queue with FIFO backpressure.
 */
class NotificationRepositoryImpl private constructor() : NotificationRepository {

    private val mutex = Mutex()
    private val _notifications = MutableStateFlow<List<NotificationMessage>>(emptyList())

    override fun observeActiveNotifications(): Flow<List<NotificationMessage>> {
        return _notifications.asStateFlow()
    }

    override suspend fun enqueueNotification(notification: NotificationMessage) {
        mutex.withLock {
            val current = _notifications.value.toMutableList()
            // Replace if existing ID exists, else prepend to top
            val existingIndex = current.indexOfFirst { it.id == notification.id }
            if (existingIndex >= 0) {
                current[existingIndex] = notification
            } else {
                current.add(0, notification)
            }

            // Enforce backpressure queue limit (max 10 active notifications)
            val bounded = if (current.size > MAX_QUEUE_CAPACITY) {
                current.take(MAX_QUEUE_CAPACITY)
            } else {
                current
            }

            _notifications.value = bounded
        }
    }

    override suspend fun dismissNotification(id: String) {
        mutex.withLock {
            val current = _notifications.value
            _notifications.value = current.filter { it.id != id }
        }
    }

    override suspend fun clearAll() {
        mutex.withLock {
            _notifications.value = emptyList()
        }
    }

    companion object {
        const val MAX_QUEUE_CAPACITY = 10

        @Volatile
        private var instance: NotificationRepositoryImpl? = null

        fun getInstance(): NotificationRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: NotificationRepositoryImpl().also { instance = it }
            }
        }
    }
}
