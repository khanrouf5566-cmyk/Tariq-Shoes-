package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerById(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerByIdDirect(id: Long): CustomerEntity?

    @Query("""
        SELECT * FROM customers 
        WHERE name LIKE '%' || :query || '%' 
           OR phone LIKE '%' || :query || '%' 
           OR address LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT COUNT(*) FROM customers")
    fun getTotalCustomersCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(remainingBalance), 0.0) FROM customers")
    fun getTotalReceivableCredit(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("UPDATE customers SET totalPurchases = totalPurchases + :saleAmount, remainingBalance = remainingBalance + :unpaidAmount WHERE id = :customerId")
    suspend fun addPurchaseToCustomer(customerId: Long, saleAmount: Double, unpaidAmount: Double)

    @Query("UPDATE customers SET totalPaid = totalPaid + :paymentAmount, remainingBalance = remainingBalance - :paymentAmount WHERE id = :customerId")
    suspend fun applyPaymentToCustomer(customerId: Long, paymentAmount: Double)

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomersDirect(): List<CustomerEntity>
}
