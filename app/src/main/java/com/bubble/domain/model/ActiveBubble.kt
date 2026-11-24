package com.bubble.domain.model

/**
 * Pure Kotlin immutable domain representation of an active floating conversation head.
 * In accordance with Clean Architecture, this file contains zero android.* framework imports.
 */
data class ActiveBubble(
    val conversationId: String,
    val senderName: String,
    val avatarUri: String? = null,
    val packageName: String,
    val appName: String,
    val appIconRes: Int? = null,
    val unreadCount: Int = 1,
    val timestampMs: Long = System.currentTimeMillis()
)
