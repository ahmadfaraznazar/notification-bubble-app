package com.bubble.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bubble.data.local.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object managing persistent Google Play in-app purchase tokens and Pro status.
 */
@Dao
interface PurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(purchase: PurchaseEntity)

    @Query("SELECT * FROM purchase_records WHERE product_id = :productId LIMIT 1")
    suspend fun getPurchase(productId: String): PurchaseEntity?

    @Query("SELECT * FROM purchase_records WHERE product_id = :productId LIMIT 1")
    fun observePurchase(productId: String): Flow<PurchaseEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM purchase_records WHERE product_id = :productId AND is_pro_active = 1)")
    fun isProActiveFlow(productId: String = "pro_lifetime_unlock"): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM purchase_records WHERE product_id = :productId AND is_pro_active = 1)")
    suspend fun isProActive(productId: String = "pro_lifetime_unlock"): Boolean

    @Query("DELETE FROM purchase_records WHERE product_id = :productId")
    suspend fun deletePurchase(productId: String)

    @Query("DELETE FROM purchase_records")
    suspend fun clearAll()
}
