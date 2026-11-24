package com.bubble.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.bubble.core.logging.BubbleLog
import com.bubble.data.local.AppDatabase
import com.bubble.data.local.dao.PurchaseDao
import com.bubble.data.local.entity.PurchaseEntity
import com.bubble.domain.repository.BillingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Concrete implementation of BillingRepository orchestrating Google Play Billing Client v6
 * and offline Room entitlement caching.
 */
class BillingRepositoryImpl(
    private val context: Context,
    private val purchaseDao: PurchaseDao,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : BillingRepository, PurchasesUpdatedListener, BillingClientStateListener {

    private val _isProActive = MutableStateFlow(false)
    override val isProActive: StateFlow<Boolean> = _isProActive.asStateFlow()

    private val _productPrice = MutableStateFlow(BillingRepository.DEFAULT_PRICE)
    override val productPrice: StateFlow<String> = _productPrice.asStateFlow()

    private var productDetails: ProductDetails? = null

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    init {
        // Hydrate Pro entitlement state from offline Room cache immediately
        coroutineScope.launch {
            purchaseDao.isProActiveFlow(BillingRepository.PRODUCT_PRO_LIFETIME).collect { active ->
                _isProActive.value = active
            }
        }
    }

    override fun startConnection() {
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            BubbleLog.i(TAG, "Billing client successfully connected to Google Play")
            queryProductDetails()
            restorePurchases()
        } else {
            BubbleLog.w(TAG, "Billing setup failed with response code: ${billingResult.responseCode} (${billingResult.debugMessage})")
        }
    }

    override fun onBillingServiceDisconnected() {
        BubbleLog.w(TAG, "Billing service disconnected. Will reconnect on next request.")
    }

    private fun queryProductDetails() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(BillingRepository.PRODUCT_PRO_LIFETIME)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = productDetailsList.firstOrNull { it.productId == BillingRepository.PRODUCT_PRO_LIFETIME }
                if (details != null) {
                    productDetails = details
                    val formattedPrice = details.oneTimePurchaseOfferDetails?.formattedPrice
                    if (!formattedPrice.isNullOrBlank()) {
                        _productPrice.value = formattedPrice
                    }
                    BubbleLog.i(TAG, "Loaded product details: price=${_productPrice.value}")
                }
            } else {
                BubbleLog.w(TAG, "Failed to query product details: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Launches the Google Play billing bottom-sheet purchase flow.
     */
    fun launchBillingFlow(activity: Activity): Boolean {
        val details = productDetails
        if (details == null) {
            BubbleLog.w(TAG, "Cannot launch purchase flow: Product details not loaded yet.")
            startConnection()
            return false
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val response = billingClient.launchBillingFlow(activity, flowParams)
        return response.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            BubbleLog.i(TAG, "User canceled Google Play billing purchase flow")
        } else {
            BubbleLog.w(TAG, "onPurchasesUpdated failed: code=${billingResult.responseCode}, msg=${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.products.contains(BillingRepository.PRODUCT_PRO_LIFETIME) &&
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        ) {
            coroutineScope.launch {
                // Acknowledge non-consumable purchase if needed
                if (!purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(ackParams) { ackResult ->
                        BubbleLog.i(TAG, "Purchase acknowledged: code=${ackResult.responseCode}")
                    }
                }

                // Persist lifetime entitlement into Room database
                val entity = PurchaseEntity(
                    productId = BillingRepository.PRODUCT_PRO_LIFETIME,
                    purchaseToken = purchase.purchaseToken,
                    isAcknowledged = true,
                    isProActive = true,
                    purchasedAt = purchase.purchaseTime
                )
                purchaseDao.insertOrUpdate(entity)
                _isProActive.value = true
                BubbleLog.i(TAG, "Pro status successfully granted and persisted in local Room database!")
            }
        }
    }

    override fun restorePurchases(onResult: (Boolean) -> Unit) {
        val queryParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(queryParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var foundPro = false
                for (purchase in purchases) {
                    if (purchase.products.contains(BillingRepository.PRODUCT_PRO_LIFETIME) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        foundPro = true
                        handlePurchase(purchase)
                    }
                }
                onResult(foundPro)
                BubbleLog.i(TAG, "Purchase restoration completed. foundPro=$foundPro")
            } else {
                BubbleLog.w(TAG, "Failed to restore purchases: ${billingResult.debugMessage}")
                onResult(false)
            }
        }
    }

    override fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    companion object {
        private const val TAG = "BillingRepository"

        @Volatile
        private var instance: BillingRepositoryImpl? = null

        fun getInstance(context: Context): BillingRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val db = AppDatabase.getInstance(context)
                    BillingRepositoryImpl(context.applicationContext, db.purchaseDao()).also {
                        instance = it
                    }
                }
            }
        }
    }
}
