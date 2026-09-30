package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.StockTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransactionDao {
    @Query("SELECT * FROM stock_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId ORDER BY date DESC")
    fun getTransactionsForProduct(productId: String): Flow<List<StockTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: StockTransactionEntity): Long

    @Query("SELECT * FROM stock_transactions")
    suspend fun getAllTransactionsDirect(): List<StockTransactionEntity>
}
