package com.bubble.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Pure Kotlin domain contract for Google Play in-app billing entitlements.
 * Zero android.* dependencies to enforce Clean Architecture boundaries.
 */
interface BillingRepository {

    /**
     * Reactive stream indicating whether the user possesses lifetime Pro status.
     */
    val isProActive: StateFlow<Boolean>

    /**
     * Localized price string retrieved from Google Play Store (e.g. "$3.99").
     */
    val productPrice: StateFlow<String>

    /**
     * Initializes the underlying connection to Google Play Billing services.
     */
    fun startConnection()

    /**
     * Queries Google Play for existing active purchases and refreshes local Room entitlement cache.
     */
    fun restorePurchases(onResult: (Boolean) -> Unit = {})

    /**
     * Closes the billing service connection.
     */
    fun endConnection()

    companion object {
        const val PRODUCT_PRO_LIFETIME = "pro_lifetime_unlock"
        const val DEFAULT_PRICE = "$3.99"
        const val FREE_TIER_MAX_APPS = 3
    }
}
