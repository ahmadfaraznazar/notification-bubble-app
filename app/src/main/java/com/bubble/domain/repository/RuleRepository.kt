package com.bubble.domain.repository

import com.bubble.domain.model.AppRule
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for app whitelisting and quiet hours scheduling.
 */
interface RuleRepository {
    /**
     * Observes the complete list of configured app rules.
     */
    fun observeAllRules(): Flow<List<AppRule>>

    /**
     * Rapidly checks if an app is whitelisted and not silenced by quiet hours.
     * Guaranteed sub-millisecond execution.
     */
    suspend fun isPackageAllowed(packageName: String, currentHour: Int): Boolean

    /**
     * Toggles whitelist status for an app.
     */
    suspend fun setAppWhitelistStatus(packageName: String, isWhitelisted: Boolean)

    /**
     * Updates or inserts a comprehensive app rule.
     */
    suspend fun upsertRule(rule: AppRule)

    /**
     * Seeds initial default messaging apps into the database.
     */
    suspend fun initializeDefaultRules(defaultRules: List<AppRule>)
}
