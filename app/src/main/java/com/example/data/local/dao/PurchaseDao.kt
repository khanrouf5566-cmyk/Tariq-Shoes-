package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY date DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<PurchaseEntity>>

    @Query("SELECT COALESCE(SUM(totalCost), 0.0) FROM purchases")
    fun getTotalPurchaseCost(): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalCost), 0.0) FROM purchases WHERE date >= :start AND date <= :end")
    fun getPurchaseCostForPeriod(start: Long, end: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)

    @Query("SELECT * FROM purchases")
    suspend fun getAllPurchasesDirect(): List<PurchaseEntity>
}
