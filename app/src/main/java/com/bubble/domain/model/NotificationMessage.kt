package com.bubble.domain.model

/**
 * Pure Kotlin immutable domain representation of an intercepted device notification.
 * Conforms to Clean Architecture: zero android.* framework imports.
 */
data class NotificationMessage(
    val id: String,
    val senderName: String,
    val textSnippet: String,
    val packageName: String,
    val appName: String,
    val avatarUri: String? = null,
    val timestampMs: Long = System.currentTimeMillis(),
    val isMasked: Boolean = false,
    val hasQuickReply: Boolean = false
)
