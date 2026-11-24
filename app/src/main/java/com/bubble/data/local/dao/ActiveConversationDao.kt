package com.bubble.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bubble.data.local.entity.ActiveConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for persisting active on-screen conversation bubbles.
 */
@Dao
interface ActiveConversationDao {

    @Query("SELECT * FROM active_conversations ORDER BY updated_at DESC")
    fun observeActiveQueue(): Flow<List<ActiveConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ActiveConversationEntity)

    @Query("DELETE FROM active_conversations WHERE conversation_key = :key")
    suspend fun dismissConversation(key: String)

    @Query("DELETE FROM active_conversations")
    suspend fun clearAll()

    @Query("DELETE FROM active_conversations WHERE updated_at < :threshold")
    suspend fun pruneStaleConversations(threshold: Long)
}
