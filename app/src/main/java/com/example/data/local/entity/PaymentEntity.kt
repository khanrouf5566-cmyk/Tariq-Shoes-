package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["date"])
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String, // e.g. "REC-1001"
    val customerId: Long? = null,
    val customerName: String = "",
    val saleId: Long? = null,
    val invoiceNumber: String? = null,
    val amount: Double,
    val paymentMethod: String = "Cash",
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
