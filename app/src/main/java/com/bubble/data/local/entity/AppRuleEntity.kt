package com.bubble.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bubble.domain.model.AppRule

/**
 * Room SQLite table storing package whitelisting and quiet hours scheduling.
 */
@Entity(
    tableName = "app_rules",
    indices = [
        Index(value = ["is_whitelisted"])
    ]
)
data class AppRuleEntity(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "app_name")
    val appName: String,

    @ColumnInfo(name = "is_whitelisted")
    val isWhitelisted: Boolean = false,

    @ColumnInfo(name = "is_custom_rule")
    val isCustomRule: Boolean = false,

    @ColumnInfo(name = "is_quiet_hours_enabled")
    val isQuietHoursEnabled: Boolean = false,

    @ColumnInfo(name = "quiet_start_hour")
    val quietStartHour: Int = 22,

    @ColumnInfo(name = "quiet_end_hour")
    val quietEndHour: Int = 7,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

fun AppRuleEntity.toDomain(): AppRule = AppRule(
    packageName = this.packageName,
    appName = this.appName,
    isWhitelisted = this.isWhitelisted,
    isCustomRule = this.isCustomRule,
    isQuietHoursEnabled = this.isQuietHoursEnabled,
    quietStartHour = this.quietStartHour,
    quietEndHour = this.quietEndHour
)

fun AppRule.toEntity(): AppRuleEntity = AppRuleEntity(
    packageName = this.packageName,
    appName = this.appName,
    isWhitelisted = this.isWhitelisted,
    isCustomRule = this.isCustomRule,
    isQuietHoursEnabled = this.isQuietHoursEnabled,
    quietStartHour = this.quietStartHour,
    quietEndHour = this.quietEndHour
)
