package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    fun getInvoiceByNumber(invoiceNumber: String): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getInvoiceByNumberDirect(invoiceNumber: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE saleId = :saleId LIMIT 1")
    fun getInvoiceBySaleId(saleId: Long): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE saleId = :saleId LIMIT 1")
    suspend fun getInvoiceBySaleIdDirect(saleId: Long): InvoiceEntity?

    @Query("""
        SELECT * FROM invoices 
        WHERE invoiceNumber LIKE '%' || :query || '%' 
           OR customerName LIKE '%' || :query || '%' 
           OR customerPhone LIKE '%' || :query || '%'
        ORDER BY date DESC
    """)
    fun searchInvoices(query: String): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("SELECT * FROM invoices")
    suspend fun getAllInvoicesDirect(): List<InvoiceEntity>
}
