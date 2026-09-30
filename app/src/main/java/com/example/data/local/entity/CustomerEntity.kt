package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["phone"]),
        Index(value = ["name"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: String, // e.g. "CUST-1001"
    val name: String,
    val phone: String,
    val address: String = "",
    val totalPurchases: Double = 0.0,
    val totalPaid: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
