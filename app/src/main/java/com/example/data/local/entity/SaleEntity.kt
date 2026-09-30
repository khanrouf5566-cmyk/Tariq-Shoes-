package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["date"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String, // e.g. "INV-1001"
    val customerId: Long? = null,
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val subtotal: Double,
    val discount: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val profit: Double,
    val paymentMethod: String, // "Cash", "Credit", "Partial Payment"
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isCancelled: Boolean = false
)
