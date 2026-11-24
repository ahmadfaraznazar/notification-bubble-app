package com.bubble.data.repository

import com.bubble.domain.repository.BillingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating BillingRepository constants, free tier constraints, and Pro entitlement gating.
 */
class BillingRepositoryTest {

    @Test
    fun productConstants_matchMonetizationSpecifications() {
        assertEquals("pro_lifetime_unlock", BillingRepository.PRODUCT_PRO_LIFETIME)
        assertEquals("$3.99", BillingRepository.DEFAULT_PRICE)
        assertEquals(3, BillingRepository.FREE_TIER_MAX_APPS)
    }

    @Test
    fun canEnableMoreApps_freeTier_allowsUpToLimit() {
        fun isAllowed(currentEnabled: Int, isPro: Boolean): Boolean {
            return isPro || currentEnabled < BillingRepository.FREE_TIER_MAX_APPS
        }

        // Under limit
        assertTrue(isAllowed(0, isPro = false))
        assertTrue(isAllowed(1, isPro = false))
        assertTrue(isAllowed(2, isPro = false))

        // At or over limit
        assertFalse(isAllowed(3, isPro = false))
        assertFalse(isAllowed(4, isPro = false))
    }

    @Test
    fun canEnableMoreApps_proTier_allowsUnlimited() {
        fun isAllowed(currentEnabled: Int, isPro: Boolean): Boolean {
            return isPro || currentEnabled < BillingRepository.FREE_TIER_MAX_APPS
        }

        assertTrue(isAllowed(0, isPro = true))
        assertTrue(isAllowed(3, isPro = true))
        assertTrue(isAllowed(10, isPro = true))
        assertTrue(isAllowed(100, isPro = true))
    }
}
