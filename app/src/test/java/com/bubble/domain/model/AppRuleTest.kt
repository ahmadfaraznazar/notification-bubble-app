package com.bubble.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating package whitelisting and quiet hours scheduling math.
 */
class AppRuleTest {

    @Test
    fun isAllowedAtHour_whenWhitelistedAndQuietHoursDisabled_returnsTrue() {
        val rule = AppRule(
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            isWhitelisted = true,
            isQuietHoursEnabled = false
        )

        assertTrue(rule.isAllowedAtHour(currentHour = 14))
        assertTrue(rule.isAllowedAtHour(currentHour = 23))
        assertTrue(rule.isAllowedAtHour(currentHour = 3))
    }

    @Test
    fun isAllowedAtHour_whenNotWhitelisted_returnsFalseAlways() {
        val rule = AppRule(
            packageName = "com.spam.app",
            appName = "Spam",
            isWhitelisted = false,
            isQuietHoursEnabled = false
        )

        assertFalse(rule.isAllowedAtHour(currentHour = 12))
    }

    @Test
    fun isQuietHourActive_withMidnightWrapAround_detectsNightHoursCorrectly() {
        // Quiet window: 22:00 (10 PM) to 07:00 (7 AM)
        val rule = AppRule(
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            isWhitelisted = true,
            isQuietHoursEnabled = true,
            quietStartHour = 22,
            quietEndHour = 7
        )

        // Inside quiet window
        assertTrue(rule.isQuietHourActive(22))
        assertTrue(rule.isQuietHourActive(23))
        assertTrue(rule.isQuietHourActive(0))
        assertTrue(rule.isQuietHourActive(4))
        assertTrue(rule.isQuietHourActive(6))

        // Outside quiet window
        assertFalse(rule.isQuietHourActive(7))
        assertFalse(rule.isQuietHourActive(12))
        assertFalse(rule.isQuietHourActive(18))
        assertFalse(rule.isQuietHourActive(21))

        // Complete permission evaluation
        assertFalse(rule.isAllowedAtHour(23))
        assertTrue(rule.isAllowedAtHour(14))
    }

    @Test
    fun isQuietHourActive_withoutMidnightWrapAround_detectsDayHoursCorrectly() {
        // Afternoon focus window: 13:00 to 17:00
        val rule = AppRule(
            packageName = "com.slack",
            appName = "Slack",
            isWhitelisted = true,
            isQuietHoursEnabled = true,
            quietStartHour = 13,
            quietEndHour = 17
        )

        assertTrue(rule.isQuietHourActive(13))
        assertTrue(rule.isQuietHourActive(15))
        assertFalse(rule.isQuietHourActive(17))
        assertFalse(rule.isQuietHourActive(12))
        assertFalse(rule.isQuietHourActive(20))
    }
}
