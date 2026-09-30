package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    indices = [
        Index(value = ["purchaseNumber"], unique = true),
        Index(value = ["supplierId"]),
        Index(value = ["date"])
    ]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseNumber: String, // e.g. "PUR-1001"
    val supplierId: Long? = null,
    val supplierName: String = "",
    val productId: String,
    val productName: String,
    val size: String = "",
    val color: String = "",
    val quantity: Int,
    val purchasePrice: Double,
    val totalCost: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
