package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    fun getSaleById(id: Long): Flow<SaleEntity?>

    @Query("SELECT * FROM sales WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    fun getSaleByInvoiceNumber(invoiceNumber: String): Flow<SaleEntity?>

    @Query("SELECT * FROM sales WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getSaleByInvoiceNumberDirect(invoiceNumber: String): SaleEntity?

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY date DESC")
    fun getSalesByCustomer(customerId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE date >= :startOfDay AND date <= :endOfDay ORDER BY date DESC")
    fun getSalesForPeriod(startOfDay: Long, endOfDay: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales ORDER BY date DESC LIMIT :limit")
    fun getRecentSales(limit: Int = 10): Flow<List<SaleEntity>>

    @Query("SELECT COALESCE(SUM(grandTotal), 0.0) FROM sales WHERE date >= :startOfDay AND date <= :endOfDay AND isCancelled = 0")
    fun getSalesAmountForPeriod(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM sales WHERE date >= :startOfDay AND date <= :endOfDay AND isCancelled = 0")
    fun getProfitForPeriod(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM sales WHERE date >= :startOfDay AND date <= :endOfDay AND isCancelled = 0")
    fun getOrdersCountForPeriod(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(grandTotal), 0.0) FROM sales WHERE isCancelled = 0")
    fun getTotalSalesAmount(): Flow<Double>

    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM sales WHERE isCancelled = 0")
    fun getTotalProfitAmount(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getItemsForSale(saleId: Long): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSaleDirect(saleId: Long): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items WHERE invoiceNumber = :invoiceNumber")
    fun getItemsForInvoice(invoiceNumber: String): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sale_items WHERE invoiceNumber = :invoiceNumber")
    suspend fun getItemsForInvoiceDirect(invoiceNumber: String): List<SaleItemEntity>

    @Query("UPDATE sales SET isCancelled = 1 WHERE id = :saleId")
    suspend fun cancelSale(saleId: Long)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Query("SELECT * FROM sales")
    suspend fun getAllSalesDirect(): List<SaleEntity>

    @Query("SELECT * FROM sale_items")
    suspend fun getAllSaleItemsDirect(): List<SaleItemEntity>
}
