package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["saleId"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val saleId: Long,
    val customerName: String,
    val customerPhone: String,
    val date: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val discount: Double,
    val grandTotal: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val paymentMethod: String,
    val pdfFilePath: String? = null
)
