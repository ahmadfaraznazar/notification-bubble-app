package com.bubble.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room SQLite table tracking active conversation bubbles for restart persistence.
 */
@Entity(
    tableName = "active_conversations",
    foreignKeys = [
        ForeignKey(
            entity = AppRuleEntity::class,
            parentColumns = ["package_name"],
            childColumns = ["package_name"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["package_name"]),
        Index(value = ["updated_at"])
    ]
)
data class ActiveConversationEntity(
    @PrimaryKey
    @ColumnInfo(name = "conversation_key")
    val conversationKey: String,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "sender_name")
    val senderName: String,

    @ColumnInfo(name = "avatar_uri")
    val avatarUri: String?,

    @ColumnInfo(name = "last_message")
    val lastMessage: String,

    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 1,

    @ColumnInfo(name = "is_masked")
    val isMasked: Boolean = false,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
