package com.bubble.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room SQLite table caching offline Google Play Billing Pro unlock entitlement.
 */
@Entity(tableName = "purchase_records")
data class PurchaseEntity(
    @PrimaryKey
    @ColumnInfo(name = "product_id")
    val productId: String,

    @ColumnInfo(name = "purchase_token")
    val purchaseToken: String,

    @ColumnInfo(name = "is_acknowledged")
    val isAcknowledged: Boolean = false,

    @ColumnInfo(name = "is_pro_active")
    val isProActive: Boolean = false,

    @ColumnInfo(name = "purchased_at")
    val purchasedAt: Long = System.currentTimeMillis()
)
