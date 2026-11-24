package com.bubble.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bubble.data.local.dao.ActiveConversationDao
import com.bubble.data.local.dao.AppRuleDao
import com.bubble.data.local.dao.PurchaseDao
import com.bubble.data.local.entity.ActiveConversationEntity
import com.bubble.data.local.entity.AppRuleEntity
import com.bubble.data.local.entity.PurchaseEntity

/**
 * Android Room SQLite Database.
 * Holds app whitelists, quiet hours rules, offline conversation state, and billing tokens.
 */
@Database(
    entities = [
        AppRuleEntity::class,
        ActiveConversationEntity::class,
        PurchaseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appRuleDao(): AppRuleDao
    abstract fun activeConversationDao(): ActiveConversationDao
    abstract fun purchaseDao(): PurchaseDao

    companion object {
        private const val DATABASE_NAME = "notification_bubble.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
