package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY date DESC")
    fun getPaymentsByCustomer(customerId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments")
    fun getTotalCollectedPayments(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE date >= :start AND date <= :end")
    fun getPaymentsForPeriod(start: Long, end: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("SELECT * FROM payments")
    suspend fun getAllPaymentsDirect(): List<PaymentEntity>
}
