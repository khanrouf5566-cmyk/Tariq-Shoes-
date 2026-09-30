package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "suppliers",
    indices = [
        Index(value = ["phone"]),
        Index(value = ["name"])
    ]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierId: String, // e.g. "SUP-101"
    val name: String,
    val phone: String,
    val address: String = "",
    val productsSupplied: String = "",
    val totalPurchased: Double = 0.0,
    val totalPaid: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
