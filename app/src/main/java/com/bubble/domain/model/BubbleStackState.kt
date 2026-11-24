package com.bubble.domain.model

/**
 * Overlay visibility modes across system interaction lifecycle.
 */
enum class OverlayDisplayMode {
    DOCKED_SIDE_PILL,   // Low-profile 12dp side pill during fullscreen games/video
    FLOATING_ACTIVE,    // Standard floating bubble heads
    EXPANDED_PREVIEW    // Floating 300dp notification card with quick-reply actions
}

/**
 * Pure Kotlin immutable domain representation of the multi-bubble conversation stack.
 * In accordance with Clean Architecture, contains zero android.* framework imports.
 */
data class BubbleStackState(
    val bubbles: List<ActiveBubble> = emptyList(),
    val activeConversationId: String? = null,
    val displayMode: OverlayDisplayMode = OverlayDisplayMode.FLOATING_ACTIVE
) {
    /**
     * The primary active head currently leading the stack.
     */
    val activeBubble: ActiveBubble?
        get() = bubbles.firstOrNull { it.conversationId == activeConversationId } ?: bubbles.firstOrNull()

    /**
     * Up to 3 visible conversation heads for depth staggering.
     */
    val visibleStack: List<ActiveBubble>
        get() {
            val top = activeBubble ?: return emptyList()
            val remaining = bubbles.filter { it.conversationId != top.conversationId }
            return (listOf(top) + remaining).take(MAX_VISIBLE_HEADS)
        }

    /**
     * Aggregated unread count across all active conversations.
     */
    val totalUnreadCount: Int
        get() = bubbles.sumOf { it.unreadCount }

    /**
     * Re-orders the stack promoting a selected conversation to the top.
     */
    fun selectConversation(conversationId: String): BubbleStackState {
        val selected = bubbles.find { it.conversationId == conversationId } ?: return this
        val reordered = listOf(selected) + bubbles.filter { it.conversationId != conversationId }
        return copy(
            bubbles = reordered,
            activeConversationId = conversationId
        )
    }

    /**
     * Adds or updates a conversation in the stack.
     */
    fun upsertBubble(bubble: ActiveBubble): BubbleStackState {
        val existingIndex = bubbles.indexOfFirst { it.conversationId == bubble.conversationId }
        val updatedList = if (existingIndex >= 0) {
            bubbles.toMutableList().apply { set(existingIndex, bubble) }
        } else {
            listOf(bubble) + bubbles
        }
        return copy(
            bubbles = updatedList,
            activeConversationId = bubble.conversationId
        )
    }

    /**
     * Removes a dismissed conversation from the stack.
     */
    fun removeBubble(conversationId: String): BubbleStackState {
        val remaining = bubbles.filter { it.conversationId != conversationId }
        val nextActiveId = if (activeConversationId == conversationId) {
            remaining.firstOrNull()?.conversationId
        } else {
            activeConversationId
        }
        return copy(
            bubbles = remaining,
            activeConversationId = nextActiveId
        )
    }

    companion object {
        const val MAX_VISIBLE_HEADS = 3
    }
}
