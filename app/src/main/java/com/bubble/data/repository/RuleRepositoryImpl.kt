package com.bubble.data.repository

import com.bubble.data.local.dao.AppRuleDao
import com.bubble.data.local.entity.toDomain
import com.bubble.data.local.entity.toEntity
import com.bubble.domain.model.AppRule
import com.bubble.domain.repository.RuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of RuleRepository bridging Room SQLite AppRuleDao to Domain.
 */
class RuleRepositoryImpl(
    private val appRuleDao: AppRuleDao
) : RuleRepository {

    override fun observeAllRules(): Flow<List<AppRule>> {
        return appRuleDao.observeAllRules().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun isPackageAllowed(packageName: String, currentHour: Int): Boolean {
        val rule = appRuleDao.getRuleByPackage(packageName)?.toDomain() ?: return false
        return rule.isAllowedAtHour(currentHour)
    }

    override suspend fun setAppWhitelistStatus(packageName: String, isWhitelisted: Boolean) {
        appRuleDao.updateWhitelistStatus(packageName, isWhitelisted)
    }

    override suspend fun upsertRule(rule: AppRule) {
        appRuleDao.upsertRule(rule.toEntity())
    }

    override suspend fun initializeDefaultRules(defaultRules: List<AppRule>) {
        appRuleDao.insertDefaults(defaultRules.map { it.toEntity() })
    }
}
