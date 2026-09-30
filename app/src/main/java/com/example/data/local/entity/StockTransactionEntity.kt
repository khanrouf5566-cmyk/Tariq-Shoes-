package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_transactions",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["date"])
    ]
)
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: String,
    val productName: String,
    val size: String = "",
    val color: String = "",
    val quantityChange: Int, // e.g. -2 for sale, +10 for purchase, +5 for adjustment
    val remainingStockAfter: Int = 0,
    val type: String, // "SALE", "PURCHASE", "ADJUSTMENT", "RETURN", "INITIAL"
    val referenceId: String = "", // invoiceNumber or purchaseNumber
    val reason: String = "",
    val date: Long = System.currentTimeMillis()
)
