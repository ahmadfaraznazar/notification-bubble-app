package com.bubble.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bubble.data.local.entity.AppRuleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for package whitelist and quiet hours querying.
 */
@Dao
interface AppRuleDao {

    @Query("SELECT * FROM app_rules ORDER BY app_name ASC")
    fun observeAllRules(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE package_name = :pkg LIMIT 1")
    suspend fun getRuleByPackage(pkg: String): AppRuleEntity?

    @Query("SELECT is_whitelisted FROM app_rules WHERE package_name = :pkg LIMIT 1")
    suspend fun isAppWhitelisted(pkg: String): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: AppRuleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(rules: List<AppRuleEntity>)

    @Query("UPDATE app_rules SET is_whitelisted = :whitelisted WHERE package_name = :pkg")
    suspend fun updateWhitelistStatus(pkg: String, whitelisted: Boolean)
}
