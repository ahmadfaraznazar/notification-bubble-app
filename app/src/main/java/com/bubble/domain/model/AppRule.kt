package com.bubble.domain.model

/**
 * Pure Kotlin immutable domain representation of an app whitelist and quiet hours rule.
 * Conforms to Clean Architecture: zero android.* framework imports.
 */
data class AppRule(
    val packageName: String,
    val appName: String,
    val isWhitelisted: Boolean = false,
    val isCustomRule: Boolean = false,
    val isQuietHoursEnabled: Boolean = false,
    val quietStartHour: Int = 22, // 10 PM
    val quietEndHour: Int = 7     // 7 AM
) {
    /**
     * Determines whether the given hour falls inside the quiet window.
     * Supports midnight wrap-around (e.g. 22 -> 7).
     */
    fun isQuietHourActive(currentHour: Int): Boolean {
        if (!isQuietHoursEnabled) return false
        return if (quietStartHour <= quietEndHour) {
            currentHour in quietStartHour until quietEndHour
        } else {
            currentHour >= quietStartHour || currentHour < quietEndHour
        }
    }

    /**
     * Determines if incoming alerts from this app may spawn a bubble at the current hour.
     */
    fun isAllowedAtHour(currentHour: Int): Boolean {
        if (!isWhitelisted) return false
        if (isQuietHourActive(currentHour)) return false
        return true
    }
}
